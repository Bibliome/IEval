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

public class BBKBPostprocessing implements CorpusPostprocessing {
	public static final String KNOWLEDGE_BASE_DOCUMENT = "knowledge-base";
	
	private static final List<ImmutableFragment> EMPTY_FRAGMENT = Arrays.asList(new ImmutableFragment(0, 0));
	
	private final Map<String,String> referentMap = new HashMap<String,String>();
	
	public BBKBPostprocessing() throws IOException {
		super();
		SourceStream source = new ResourceSourceStream(getClass().getClassLoader(), "org/bionlpst/data/Bacteria_species.txt");
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
		Map<KBLivesIn,KBLivesIn> refKB = new HashMap<KBLivesIn,KBLivesIn>();
		Map<KBLivesIn,KBLivesIn> predKB = new HashMap<KBLivesIn,KBLivesIn>();
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
		convertToAnnotation(logger, refAS, refKB.keySet(), tbIds, nIds, "KB_Lives_In");
		convertToAnnotation(logger, predAS, predKB.keySet(), tbIds, nIds, "KB_Lives_In");
		convertToAnnotation(logger, refAS, capToSpecies(refKB.keySet()), tbIds, nIds, "KB_Lives_In_Cap_To_Species");
		convertToAnnotation(logger, predAS, capToSpecies(predKB.keySet()), tbIds, nIds, "KB_Lives_In_Cap_To_Species");
		refAS.resolveReferences(logger);
		predAS.resolveReferences(logger);
		if (logger.getHighestLevel() != null) {
			throw new BioNLPSTException(logger.getMessages().iterator().next().getCompleteMessage());
		}
	}
	
	private Collection<KBLivesIn> capToSpecies(Collection<KBLivesIn> kb) {
		Map<KBLivesIn,KBLivesIn> result = new HashMap<KBLivesIn,KBLivesIn>();
		for (KBLivesIn kbli : kb) {
			String ontobiotope = kbli.ontobiotope;
			String ncbi = referentMap.containsKey(kbli.ncbi) ? referentMap.get(kbli.ncbi) : kbli.ncbi;
			KBLivesIn kbli2 = ensure(result, ncbi, ontobiotope);
			kbli2.sources.addAll(kbli.sources);
		}
		return result.keySet();
	}
	
	private static void convertToAnnotation(CheckLogger logger, AnnotationSet aset, Collection<KBLivesIn> kb, AtomicInteger tbIds, AtomicInteger nIds, String annotationType) {
		for (KBLivesIn kbli : kb) {
			convertToAnnotation(logger, aset, kbli, tbIds, nIds, annotationType);
		}
	}
	
	private static void convertToAnnotation(CheckLogger logger, AnnotationSet aset, KBLivesIn kbli, AtomicInteger tbIds, AtomicInteger nIds, String annotationType) {
		Location loc = kbli.sources.get(0).getLocation();
		String tid = "T" + tbIds.incrementAndGet();
		new TextBound(logger, aset, loc, tid, annotationType, EMPTY_FRAGMENT);
		
		String ncbiId = "N" + nIds.incrementAndGet();
		new Normalization(logger, aset, loc, ncbiId, "NCBI_Taxonomy", tid, kbli.ncbi);
		
		String ontobiotopeId = "N" + nIds.incrementAndGet();
		new Normalization(logger, aset, loc, ontobiotopeId, "OntoBiotope", tid, kbli.ontobiotope);
		
		for (Relation rel : kbli.sources) {
			String bacteriaForm = getForm(rel, "Bacteria");
			String bfid = "N" + nIds.incrementAndGet();
			new Normalization(logger, aset, rel.getLocation(), bfid, "Bacteria_Form", tid, bacteriaForm);
			String habitatForm = getForm(rel, "Location");
			String hfid = "N" + nIds.incrementAndGet();
			new Normalization(logger, aset, rel.getLocation(), hfid, "Habitat_Form", tid, habitatForm);
		}
	}
	
	private static String getForm(Relation rel, String role) {
		Annotation a = rel.getArgument(role);
		TextBound tb = a.asTextBound();
		if (tb != null) {
			return tb.getForm();
		}
		return "??? " + a;
	}

	private static void populateKB(Map<KBLivesIn,KBLivesIn> kb, AnnotationSet aset) {
		for (Annotation a : aset.getAnnotations("Lives_In")) {
			populateKB(kb, a);
		}
	}
	
	private static void populateKB(Map<KBLivesIn,KBLivesIn> kb, Annotation a) {
		Relation rel = getLivesIn(a);
		if (rel == null) {
			return;
		}
		for (String ncbi : getNormalizations(rel, "Bacteria", "NCBI_Taxonomy")) {
			for (String ontobiotope : getNormalizations(rel, "Location", "OntoBiotope")) {
				KBLivesIn kbli = ensure(kb, ncbi, ontobiotope);
				kbli.sources.add(rel);
			}
		}
	}
	
	private static KBLivesIn ensure(Map<KBLivesIn,KBLivesIn> kb, String ncbi, String ontobiotope) {
		KBLivesIn key = new KBLivesIn(ncbi, ontobiotope);
		if (kb.containsKey(key)) {
			return kb.get(key);
		}
		kb.put(key, key);
		return key;
	}
	
	private static Collection<String> getNormalizations(Relation rel, String role, String normalizationType) {
		Collection<String> result = new HashSet<String>();
		Annotation bacteria = rel.getArgument(role);
		for (Annotation bref : bacteria.getBackReferences()) {
			Normalization norm = bref.asNormalization();
			if (norm != null && norm.getType().equals(normalizationType)) {
				String referent = norm.getReferent();
				result.add(referent);
			}
		}
		return result;
	}
	
	private static Relation getLivesIn(Annotation a) {
		Relation result = a.asRelation();
		if (result == null) {
			return null;
		}
		if (!result.getType().equals("Lives_In")) {
			return null;
		}
		if (!result.hasArgument("Bacteria")) {
			return null;
		}
		if (!result.hasArgument("Location")) {
			return null;
		}
		return result;
	}

	private static class KBLivesIn {
		private final String ncbi;
		private final String ontobiotope;
		private final List<Relation> sources = new ArrayList<Relation>();

		private KBLivesIn(String ncbi, String ontobiotope) {
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
			KBLivesIn other = (KBLivesIn) obj;
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
