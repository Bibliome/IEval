package org.bionlpst.app;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.Relation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.util.Location;
import org.bionlpst.util.ResourceSourceStream;
import org.bionlpst.util.SourceStream;
import org.bionlpst.util.Util;
import org.bionlpst.util.fragment.ImmutableFragment;
import org.bionlpst.util.message.CheckLogger;

public class BB19KBPostprocessing implements CorpusPostprocessing {
	public static final String KNOWLEDGE_BASE_DOCUMENT = "knowledge-base";
	
	private static final List<ImmutableFragment> EMPTY_FRAGMENT = Arrays.asList(new ImmutableFragment(0, 0));
	
	private final Map<String,String> referentMap = new HashMap<String,String>();
	
	public BB19KBPostprocessing() throws IOException {
		super();
		SourceStream source = new ResourceSourceStream(getClass().getClassLoader(), "org/bionlpst/data/EMPTY");
		try (BufferedReader r = source.openBufferedReader()) {
			while (true) {
				String line = r.readLine();
				if (line == null) {
					break;
				}
				List<String> column = Util.split(line, '\t');
				String from = column.get(0);
				String to = column.get(1);
				referentMap.put(from, to);
			}
		}
	}

	@Override
	public void postprocess(Corpus corpus) {
		Map<KBRelation,KBRelation> refKB = new HashMap<KBRelation,KBRelation>();
		Map<KBRelation,KBRelation> predKB = new HashMap<KBRelation,KBRelation>();
		for (Document doc : corpus.getDocuments()) {
			populateKB(refKB, doc.getReferenceAnnotationSet());
			populateKB(predKB, doc.getPredictionAnnotationSet());
		}
		Document kbDoc = new Document(corpus, KNOWLEDGE_BASE_DOCUMENT, "");
		AnnotationSet refAS = kbDoc.getReferenceAnnotationSet();
		AnnotationSet predAS = kbDoc.getPredictionAnnotationSet();
		CheckLogger logger = new CheckLogger();
		AtomicInteger tbIds = new AtomicInteger(0);
		AtomicInteger nIds = new AtomicInteger(0);
		convertToAnnotation(logger, refAS, refKB.keySet(), tbIds, nIds, "KB_Relation");
		convertToAnnotation(logger, predAS, predKB.keySet(), tbIds, nIds, "KB_Relation");
		convertToAnnotation(logger, refAS, capToSpecies(refKB.keySet()), tbIds, nIds, "KB_Relation_Cap_To_Species");
		convertToAnnotation(logger, predAS, capToSpecies(predKB.keySet()), tbIds, nIds, "KB_Relation_Cap_To_Species");
		refAS.resolveReferences(logger);
		predAS.resolveReferences(logger);
		if (logger.getHighestLevel() != null) {
			throw new BioNLPSTException(logger.getMessages().iterator().next().getCompleteMessage());
		}
	}
	
	private Collection<KBRelation> capToSpecies(Collection<KBRelation> kb) {
		Map<KBRelation,KBRelation> result = new HashMap<KBRelation,KBRelation>();
		for (KBRelation kbli : kb) {
			String ontobiotope = kbli.ontobiotope;
			String ncbi = referentMap.containsKey(kbli.ncbi) ? referentMap.get(kbli.ncbi) : kbli.ncbi;
			KBRelation kbli2 = ensure(result, ncbi, ontobiotope);
			kbli2.sources.addAll(kbli.sources);
		}
		return result.keySet();
	}
	
	private static void convertToAnnotation(CheckLogger logger, AnnotationSet aset, Collection<KBRelation> kb, AtomicInteger tbIds, AtomicInteger nIds, String annotationType) {
		for (KBRelation kbli : kb) {
			convertToAnnotation(logger, aset, kbli, tbIds, nIds, annotationType);
		}
	}
	
	private static void convertToAnnotation(CheckLogger logger, AnnotationSet aset, KBRelation kbli, AtomicInteger tbIds, AtomicInteger nIds, String annotationType) {
		Location loc = kbli.sources.get(0).getLocation();
		String tid = "T" + tbIds.incrementAndGet();
		new TextBound(logger, aset, loc, tid, annotationType, EMPTY_FRAGMENT);
		
		String ncbiId = "N" + nIds.incrementAndGet();
		new Normalization(logger, aset, loc, ncbiId, "NCBI_Taxonomy", tid, kbli.ncbi);
		
		String ontobiotopeId = "N" + nIds.incrementAndGet();
		new Normalization(logger, aset, loc, ontobiotopeId, "OntoBiotope", tid, kbli.ontobiotope);
		
		for (Relation rel : kbli.sources) {
			String microorganismForm = getForm(rel, "Microorganism");
			String mfid = "N" + nIds.incrementAndGet();
			new Normalization(logger, aset, rel.getLocation(), mfid, "Microorganism_Form", tid, microorganismForm);
			String obtForm = getForm(rel, "Location", "Property");
			String ofid = "N" + nIds.incrementAndGet();
			new Normalization(logger, aset, rel.getLocation(), ofid, "OntoBiotope_Form", tid, obtForm);
		}
	}

	private static String getForm(Relation rel, String... roles) {
		Annotation a = null;
		for (String role : roles) {
			if (rel.hasArgument(role)) {
				a = rel.getArgument(role);
				TextBound tb = a.asTextBound();
				if (tb != null) {
					return tb.getForm();
				}
			}
		}
		return "??? " + a;
	}

	private static void populateKB(Map<KBRelation,KBRelation> kb, AnnotationSet aset) {
		for (Annotation a : aset.getAnnotations("Lives_In")) {
			populateKB(kb, a);
		}
		for (Annotation a : aset.getAnnotations("Exhibits")) {
			populateKB(kb, a);
		}
	}
	
	private static void populateKB(Map<KBRelation,KBRelation> kb, Annotation a) {
		Relation rel = getRelation(a);
		if (rel == null) {
			return;
		}
		for (String ncbi : getNormalizations(rel, "Microorganism", "NCBI_Taxonomy")) {
			switch (rel.getType()) {
				case "Lives_In":
					for (String ontobiotope : getNormalizations(rel, "Location", "OntoBiotope")) {
						KBRelation kbli = ensure(kb, ncbi, ontobiotope);
						kbli.sources.add(rel);
					}
					break;
				case "Exhibits":
					for (String ontobiotope : getNormalizations(rel, "Property", "OntoBiotope")) {
						KBRelation kbli = ensure(kb, ncbi, ontobiotope);
						kbli.sources.add(rel);
					}
					break;
			}
		}
	}
	
	private static KBRelation ensure(Map<KBRelation,KBRelation> kb, String ncbi, String ontobiotope) {
		KBRelation key = new KBRelation(ncbi, ontobiotope);
		if (kb.containsKey(key)) {
			return kb.get(key);
		}
		kb.put(key, key);
		return key;
	}
	
	private static Collection<String> getNormalizations(Relation rel, String role, String normalizationType) {
		Collection<String> result = new HashSet<String>();
		Annotation a = rel.getArgument(role);
		for (Annotation bref : a.getBackReferences()) {
			Normalization norm = bref.asNormalization();
			if ((norm != null) && (norm.getType().equals(normalizationType)) && (norm.getAnnotationSet() == rel.getAnnotationSet())) {
				String referent = norm.getReferent();
				result.add(referent);
			}
		}
		return result;
	}
	
	private static Relation getRelation(Annotation a) {
		Relation result = a.asRelation();
		if (result == null) {
			return null;
		}
		if (!result.hasArgument("Microorganism")) {
			return null;
		}
		switch (result.getType()) {
			case "Lives_In": {
				if (!result.hasArgument("Location")) {
					return null;
				}
				break;
			}
			case "Exhibits": {
				if (!result.hasArgument("Property")) {
					return null;
				}
				break;
			}
			default:
				return null;
		}
		return result;
	}

	private static class KBRelation {
		private final String ncbi;
		private final String ontobiotope;
		private final List<Relation> sources = new ArrayList<Relation>();

		private KBRelation(String ncbi, String ontobiotope) {
			super();
			this.ncbi = ncbi;
			this.ontobiotope = ontobiotope;
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + ((ncbi == null) ? 0 : ncbi.hashCode());
			result = prime * result + ((ontobiotope == null) ? 0 : ontobiotope.hashCode());
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			KBRelation other = (KBRelation) obj;
			if (ncbi == null) {
				if (other.ncbi != null)
					return false;
			} else if (!ncbi.equals(other.ncbi))
				return false;
			if (ontobiotope == null) {
				if (other.ontobiotope != null)
					return false;
			} else if (!ontobiotope.equals(other.ontobiotope))
				return false;
			return true;
		}
	}
}
