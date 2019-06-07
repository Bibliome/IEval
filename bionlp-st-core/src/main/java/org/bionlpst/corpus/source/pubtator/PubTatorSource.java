package org.bionlpst.corpus.source.pubtator;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.Relation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.corpus.source.ContentAndReferenceSource;
import org.bionlpst.corpus.source.bionlpst.InputStreamCollection;
import org.bionlpst.corpus.source.bionlpst.InputStreamIterator;
import org.bionlpst.util.Location;
import org.bionlpst.util.fragment.ImmutableFragment;
import org.bionlpst.util.message.CheckLogger;

public class PubTatorSource implements ContentAndReferenceSource {
	private static final Pattern CONTENT_LINE = Pattern.compile("(?<pmid>\\d+)\\|(?<sec>.+)\\|(?<txt>.+)");
	private static final Pattern TEXT_BOUND_LINE = Pattern.compile("(?<pmid>\\d*)\t(?<start>\\d+)\t(?<end>\\d+)\t(?<form>[^\t]+)\t(?<type>[^\t]+)\t(?<ref>[^\t]+)?");
	private static final Pattern RELATION_LINE = Pattern.compile("(?<pmid>\\d*)\t(?<type>[^\t]+)\t(?<left>[^\t]+)\\t(?<right>[^\\t]+)");
	private static final String[] DEFAULT_ROLES = new String[] { "Left", "Right" };

	private final InputStreamCollection inputStreamCollection;
	private final Map<String,String> sectionNames = new LinkedHashMap<String,String>();
	private final Map<String,AnnotationSetSelector> annotationTypeSelectors = new LinkedHashMap<String,AnnotationSetSelector>();
	private final Map<String,String> normalizationTypes = new LinkedHashMap<String,String>();
	private final Map<String,String[]> relationRoles = new LinkedHashMap<String,String[]>();
	private final boolean ignoreRelations;
	
	public PubTatorSource(InputStreamCollection inputStreamCollection, boolean ignoreRelations) {
		super();
		this.inputStreamCollection = inputStreamCollection;
		this.ignoreRelations = ignoreRelations;
	}
	
	public void addSectionName(String shortName, String type) {
		sectionNames.put(shortName, type);
	}
	
	public void addAnnotationTypeSelector(String type, AnnotationSetSelector selector) {
		annotationTypeSelectors.put(type, selector);
	}
	
	public void addInputType(String type) {
		annotationTypeSelectors.put(type, AnnotationSetSelector.INPUT);
	}

	public void addOutputType(String type) {
		annotationTypeSelectors.put(type, AnnotationSetSelector.REFERENCE);
	}
	
	public void addNormalizationType(String tbType, String normType) {
		normalizationTypes.put(tbType, normType);
	}
	
	public void addRelationRoles(String relType, String leftRole, String rightRole) {
		relationRoles.put(relType, new String[] { leftRole, rightRole });
	}

	@Override
	public String getName() {
		return inputStreamCollection.getName();
	}

	@Override
	public void fillContentAndReference(CheckLogger logger, Corpus corpus, boolean loadOutput) throws BioNLPSTException, IOException {
		PubTatorParser parser = new PubTatorParser(logger, corpus, loadOutput);
		InputStreamIterator it = inputStreamCollection.getIterator();
		while (it.next()) {
			parser.parse(it.getName(), it.getContents());
		}
	}
	
	@Override
	public Corpus fillContentAndReference(CheckLogger logger, boolean loadOutput) throws BioNLPSTException, IOException {
		Corpus result = new Corpus();
		fillContentAndReference(logger, result, loadOutput);
		return result;
	}
	
	private static class Section {
		private final String type;
		private final ImmutableFragment fragment;
		private final Location location;

		private Section(String type, ImmutableFragment fragment, Location location) {
			super();
			this.type = type;
			this.fragment = fragment;
			this.location = location;
		}
		
		private void createAnnotation(CheckLogger logger, Document currentDoc, String id) {
			AnnotationSet aset = currentDoc.getInputAnnotationSet();
			new TextBound(logger, aset, location, id, type, Collections.singletonList(fragment));
		}
	}

	private class PubTatorParser {
		private final CheckLogger logger;
		private final Corpus corpus;
		private final boolean loadOutput;
		private String sourceName = null;
		private Document currentDoc = null;
		private String currentPMID = null;
		private final StringBuilder txt = new StringBuilder();
		private final AtomicInteger tbID = new AtomicInteger(1);
		private final AtomicInteger normID = new AtomicInteger(1);
		private final AtomicInteger relID = new AtomicInteger(1);
		private final Collection<Section> sections = new ArrayList<Section>();
		private int lineno = 0;
		private Location location = null;

		private PubTatorParser(CheckLogger logger, Corpus corpus, boolean loadOutput) {
			super();
			this.logger = logger;
			this.corpus = corpus;
			this.loadOutput = loadOutput;
		}
		
		private void parse(String sourceName, InputStream is) throws IOException {
			Reader r = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(r);
			parse(sourceName, br);
		}
		
		private void parse(String sourceName, BufferedReader br) throws IOException {
			this.sourceName = sourceName;
			lineno = 0;
			reset();
			while (true) {
				String line = br.readLine();
				if (line == null) {
					break;
				}
				parseLine(line);
			}
		}
		
		private void reset() {
			currentDoc = null;
			currentPMID = null;
			txt.setLength(0);
			tbID.set(1);
			normID.set(1);
			relID.set(1);
			sections.clear();
			location = null;
		}

		private void parseLine(String line) {
			lineno++;
			location = new Location(sourceName, lineno);
			line = line.trim();
			if (line.isEmpty()) {
				reset();
				return;
			}
			if (parseContent(line)) {
				return;
			}
			if (!buildDocument()) {
				return;
			}
			if (parseTextBound(line)) {
				return;
			}
			if (parseRelation(line)) {
				return;
			}
			logger.serious(location, "malformed line, ignoring: '" + line + "'");
		}
		
		private boolean parseContent(String line) {
			Matcher m = CONTENT_LINE.matcher(line);
			if (m.matches()) {
				String pmid = m.group("pmid");
				if (currentPMID == null) {
					currentPMID = pmid;
				}
				else {
					if (!checkPMID(pmid)) {
						return true;
					}
				}
				String secType = m.group("sec");
				String t = m.group("txt");
				ImmutableFragment secFrag = new ImmutableFragment(txt.length(), txt.length() + t.length());
				txt.append(t);
				txt.append('\n');
				if (!sectionNames.containsKey(secType)) {
					logger.suspicious(location, "no type for section " + secType);
				}
				else {
					secType = sectionNames.get(secType);
				}
				sections.add(new Section(secType, secFrag, location));
				return true;
			}
			return false;
		}

		private boolean buildDocument() {
			if (currentDoc != null) {
				return true;
			}
			if (currentPMID == null) {
				logger.suspicious(location, "no PMID at this point, ignoring line");
				return false;
			}
			if (txt.length() == 0) {
				logger.suspicious(location, "no content at this point, ignoring line");
				return false;
			}
			currentDoc = new Document(corpus, currentPMID, txt.toString());
			for (Section sec : sections) {
				sec.createAnnotation(logger, currentDoc, nextTextBoundId());
			}
			return true;
		}
		
		private String nextTextBoundId() {
			return "T" + tbID.getAndIncrement();
		}
		
		private boolean parseTextBound(String line) {
			Matcher m = TEXT_BOUND_LINE.matcher(line);
			if (m.matches()) {
				String pmid = m.group("pmid");
				if (!checkPMID(pmid)) {
					return true;
				}
				int start = Integer.parseInt(m.group("start"));
				int end = Integer.parseInt(m.group("end"));
				String type = m.group("type");
				AnnotationSet aset = getAnnotationSet(type);
				if (aset == null) {
					return true;
				}
				TextBound tb = new TextBound(logger, aset, location, nextTextBoundId(), type, Collections.singletonList(new ImmutableFragment(start, end)));
				String ref = m.group("ref");
				if (ref.isEmpty()) {
					return true;
				}
				if (!normalizationTypes.containsKey(type)) {
					logger.serious(location, "unknown normalization type for " + type + ", ignoring normalization");
					return true;
				}
				String normType = normalizationTypes.get(type);
				aset = getAnnotationSet(normType);
				if (aset == null) {
					return true;
				}
				new Normalization(logger, aset, location, nextNormalizationId(), normType, tb.getId(), ref);
				return true;
			}
			return false;
		}
		
		private String nextNormalizationId() {
			return "N" + normID.getAndIncrement();
		}

		private boolean parseRelation(String line) {
			Matcher m = RELATION_LINE.matcher(line);
			if (m.matches()) {
				if (ignoreRelations) {
					return true;
				}
				String pmid = m.group("pmid");
				if (!checkPMID(pmid)) {
					return true;
				}
				String type = m.group("type");
				String left = m.group("left");
				String right = m.group("right");
				AnnotationSet aset = getAnnotationSet(type);
				if (aset == null) {
					return true;
				}
				Map<String,String> args = new LinkedHashMap<String,String>();
				String[] roles = getRelationRoles(type);
				args.put(roles[0], left);
				args.put(roles[1], right);
				new Relation(logger, aset, location, nextRelationId(), type, args);
				return true;
			}
			return false;
		}
		
		private String nextRelationId() {
			return "R" + relID.getAndIncrement();
		}

		private String[] getRelationRoles(String type) {
			if (relationRoles.containsKey(type)) {
				return relationRoles.get(type);
			}
			logger.serious(location, "unknown role names for " + type + ", default to Left/Right");
			return DEFAULT_ROLES;
		}
		
		private boolean checkPMID(String pmid) {
			if (pmid.equals(currentPMID)) {
				return true;
			}
			logger.serious(location, "PMID differ " + currentPMID + " / " + pmid + ", ignoring line");
			return false;
		}
		
		private AnnotationSetSelector getAnnotationSetSelector(String type) {
			if (annotationTypeSelectors.containsKey(type)) {
				return annotationTypeSelectors.get(type);
			}
			logger.serious(location, "assuming " + type + " as input annotation");
			return AnnotationSetSelector.INPUT;
		}
		
		private AnnotationSet getAnnotationSet(String type) {
			AnnotationSetSelector selector = getAnnotationSetSelector(type);
			if (selector.equals(AnnotationSetSelector.INPUT) || loadOutput) {
				return selector.getAnnotationSet(currentDoc);
			}
			return null;
		}
	}
}
