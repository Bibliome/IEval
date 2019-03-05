package org.bionlpst.corpus.parser.bionlpst;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.DummyAnnotation;
import org.bionlpst.corpus.Equivalence;
import org.bionlpst.corpus.Modifier;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.Relation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.corpus.parser.ContentAndReferenceSource;
import org.bionlpst.corpus.parser.PredictionSource;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.fragment.ImmutableFragment;
import org.bionlpst.util.message.CheckLogger;

public class BioNLPSTSource implements ContentAndReferenceSource, PredictionSource {
	public static final String EXT_CONTENTS = ".txt";
	public static final String EXT_INPUT = ".a1";
	public static final String EXT_OUTPUT = ".a2";
	
	private static final String[] EXTS_ALL = { EXT_CONTENTS, EXT_INPUT, EXT_OUTPUT };
	private static final String[] EXTS_OUTPUT_ONLY = { EXT_CONTENTS, EXT_INPUT, EXT_OUTPUT };

	private final InputStreamCollection inputStreamCollection;
	
	public BioNLPSTSource(InputStreamCollection inputStreamCollection) {
		super();
		this.inputStreamCollection = inputStreamCollection;
	}

	private static String getDocumentIdFromPath(String path) {
		int dot = path.lastIndexOf('.');
		if (dot == -1) {
			return path;
		}
		int slash = path.lastIndexOf(File.separatorChar);
		return path.substring(slash + 1, dot);
	}

	/**
	 * Parses a stream in the BioNLP-ST annotation format. The source name must follow BioNLP-ST file naming conventions. This corpus must already contain a document with the identifier corresponding to the specified source name.
	 * @param logger message container where to store parse warnings and errors.
	 * @param asetSelect specification of the annotation set.
	 * @param source name of the source.
	 * @param r stream to parse.
	 * @throws IOException if there's a I/O error reading the specified file.
	 */
	private static void parseAnnotations(CheckLogger logger, Corpus corpus, AnnotationSetSelector asetSelect, String source, Reader r) throws IOException {
		String docId = getDocumentIdFromPath(source);
		if (!corpus.hasDocument(docId)) {
			logger.serious(new Location(source, -1), "unknown document " + docId);
			return;
		}
		Document doc = corpus.getDocument(docId);
		AnnotationSet aset = asetSelect.getAnnotationSet(doc);
		BufferedReader reader = new BufferedReader(r);
		parseAnnotations(logger, aset, source, reader);
	}

	@Override
	public void getContentAndReference(CheckLogger logger, Corpus corpus, boolean loadOutput) throws BioNLPSTException, IOException {
		Collection<InputStreamEntry> records = collectEntries(EXTS_ALL);
		loadDocuments(corpus, records);
		loadInputAnnotations(logger, corpus, records);
		if (loadOutput) {
			loadOutputAnnotations(logger, corpus, records, AnnotationSetSelector.REFERENCE);
		}
	}

	@Override
	public Corpus getContentAndReference(CheckLogger logger, boolean loadOutput) throws BioNLPSTException, IOException {
		Corpus result = new Corpus();
		getContentAndReference(logger, result, loadOutput);
		return result;
	}
	
	@Override
	public void getPredictions(CheckLogger logger, Corpus corpus) throws BioNLPSTException, IOException {
		Collection<InputStreamEntry> entries = collectEntries(EXTS_OUTPUT_ONLY);
		loadOutputAnnotations(logger, corpus, entries, AnnotationSetSelector.PREDICTION);
	}

	@Override
	public String getName() {
		return inputStreamCollection.getName();
	}

	private Collection<InputStreamEntry> collectEntries(String... exts) throws IOException {
		Collection<InputStreamEntry> result = new ArrayList<InputStreamEntry>();
		InputStreamIterator it = inputStreamCollection.getIterator();
		while (it.next()) {
			String name = it.getName();
			if (matchExt(name, exts)) {
				InputStream contents = it.getContents();
				InputStreamEntry z = new InputStreamEntry(name, contents);
				result.add(z);
			}
		}
		return result;
	}
	
	private static boolean matchExt(String name, String... exts) {
		for (String ext : exts) {
			if (name.endsWith(ext)) {
				return true;
			}
		}
		return false;
	}

	private static void loadDocuments(Corpus corpus, Collection<InputStreamEntry> records) {
		for (InputStreamEntry u : records) {
			if (u.isDocumentContents()) {
				u.createDocument(corpus);
			}
		}
	}
	
	private static void loadInputAnnotations(CheckLogger logger, Corpus corpus, Collection<InputStreamEntry> record) throws IOException {
		for (InputStreamEntry u : record) {
			if (u.isInputAnnotationSet()) {
				u.createInputAnnotations(logger, corpus);
			}
		}
	}
	
	private static void loadOutputAnnotations(CheckLogger logger, Corpus corpus, Collection<InputStreamEntry> entries, AnnotationSetSelector loadOutput) throws IOException {
		for (InputStreamEntry u : entries) {
			if (u.isOutputAnnotationSet()) {
				u.createOutputAnnotations(logger, corpus, loadOutput);
			}
		}
	}

	private static class InputStreamEntry {
		private final String name;
		private final String contents;

		private InputStreamEntry(String name, InputStream contents) throws IOException {
			this.name = name;
			this.contents = Util.readWholeStream(new InputStreamReader(contents));
		}
		
		private boolean isDocumentContents() {
			return name.endsWith(EXT_CONTENTS);
		}
		
		private void createDocument(Corpus corpus) {
			String docId = BioNLPSTSource.getDocumentIdFromPath(name);
			new Document(corpus, docId, contents);
		}
		
		private boolean isInputAnnotationSet() {
			return name.endsWith(EXT_INPUT);
		}
		
		private void createInputAnnotations(CheckLogger logger, Corpus corpus) throws IOException {
			Reader reader = new StringReader(contents);
			BioNLPSTSource.parseAnnotations(logger, corpus, AnnotationSetSelector.INPUT, name, reader);
		}
		
		private boolean isOutputAnnotationSet() {
			return name.endsWith(EXT_OUTPUT);
		}
		
		private void createOutputAnnotations(CheckLogger logger, Corpus corpus, AnnotationSetSelector loadOutput) throws IOException {
			Reader reader = new StringReader(contents);
			BioNLPSTSource.parseAnnotations(logger, corpus, loadOutput, name, reader);
		}
	}

	private static void parseAnnotations(CheckLogger logger, AnnotationSet aset, String source, BufferedReader reader) throws IOException {
		aset.setParsed();
		int lineno = 0;
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			lineno++;
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			parseAnnotation(logger, aset, new Location(source, lineno), line);
		}
	}
	
	private static void parseAnnotation(CheckLogger logger, AnnotationSet aset, Location location, String line) {
		int tab = line.indexOf('\t');
		if (tab == -1) {
			logger.serious(location, "expected tab character");
			return;
		}
		String id = line.substring(0, tab);
		String rest = line.substring(tab+1);
		if (id.equals("*")) {
			if (aset.getSelector() == AnnotationSetSelector.PREDICTION) {
				logger.suspicious(location, "predictions are not supposed to provide equivalences");
			}
			else {
				parseEquivalence(logger, aset, location, rest);
			}
			return;
		}
		char idType = id.charAt(0);
		switch (idType) {
			case 'T':
			case 'W':
				parseTextBound(logger, aset, location, id, rest);
				break;
			case 'R':
			case 'E':
				parseRelation(logger, aset, location, id, rest);
				break;
			case 'N':
				parseNormalization(logger, aset, location, id, rest);
				break;
			case 'M':
				parseModifier(logger, aset, location, id, rest);
				break;
			default:
				logger.serious(location, "unknown annotation kind " + idType);
				new DummyAnnotation(logger, aset, location, id, getDummyType(rest));
		}
	}

	private static void parseEquivalence(CheckLogger logger, AnnotationSet aset, Location location, String rest) {
		if (!rest.startsWith("Equiv ")) {
			logger.serious(location, "ill formed equivalence, expected 'Equiv'");
			return;
		}
		Collection<String> annotationReferences = Util.split(rest.substring(6), ' ');
		new Equivalence(logger, aset.getDocument(), location, annotationReferences);
	}

	private static Pattern TEXT_BOUND_PARSER = Pattern.compile("(?<type>\\S+) (?<fragments>\\d+ \\d+(?:;\\d+ \\d+)*)\t(?<control>.*)");

	private static void parseTextBound(CheckLogger logger, AnnotationSet aset, Location location, String id, String rest) {
		Matcher m = TEXT_BOUND_PARSER.matcher(rest);
		if (!m.matches()) {
			logger.serious(location, "ill formed text-bound annotation: '" + rest + "'");
			new DummyAnnotation(logger, aset, location, id, getDummyType(rest));
			return;
		}
		String type = m.group("type");
		String sFragments = m.group("fragments");
		List<ImmutableFragment> fragments = parseFragments(sFragments);
		new TextBound(logger, aset, location, id, type, fragments);
	}

	private static List<ImmutableFragment> parseFragments(String sFragments) {
		List<ImmutableFragment> result = new ArrayList<ImmutableFragment>();
		for (String sFrag : Util.split(sFragments, ';')) {
			ImmutableFragment frag = parseFragment(sFrag);
			result.add(frag);
		}
		return result;
	}

	private static ImmutableFragment parseFragment(String sFrag) {
		int space = sFrag.indexOf(' ');
		int start = Integer.parseInt(sFrag.substring(0, space));
		int end = Integer.parseInt(sFrag.substring(space+1));
		return new ImmutableFragment(start, end);
	}

	private static Pattern RELATION_PARSER = Pattern.compile("(?<type>\\S+) (?<args>.+)");
	
	private static void parseRelation(CheckLogger logger, AnnotationSet aset, Location location, String id, String rest) {
		Matcher m = RELATION_PARSER.matcher(rest);
		if (!m.matches()) {
			logger.serious(location, "ill formed relation, expected type and arguments: '" + rest + "'");
			new DummyAnnotation(logger, aset, location, id, getDummyType(rest));
			return;
		}
		String type = m.group("type");
		String sArgs = m.group("args");
		Map<String,String> args = parseArgs(logger, location, sArgs);
		new Relation(logger, aset, location, id, type, args);
	}

	private static Pattern ARGUMENT_PARSER = Pattern.compile("\\s*(?<role>[^:]+):(?<ref>\\S+)\\s*");
 
	private static Map<String,String> parseArgs(CheckLogger logger, Location location, String sArgs) {
		Map<String,String> result = new LinkedHashMap<String,String>();
		Matcher m = ARGUMENT_PARSER.matcher(sArgs);
		while (m.find()) {
			String role = m.group("role");
			String ref = m.group("ref");
			if (result.containsKey(role)) {
				logger.suspicious(location, "duplicate argument: " + role);
			}
			result.put(role, ref);
		}
		if (result.isEmpty()) {
			logger.serious(location, "no arguments");
		}
		return result;
	}

	private static final Pattern NORMALIZATION_PARSER = Pattern.compile("(?<type>\\S+) Annotation:(?<ann>\\S+) Referent:(?<ref>\\S+)");
	
	private static void parseNormalization(CheckLogger logger, AnnotationSet aset, Location location, String id, String rest) {
		Matcher m = NORMALIZATION_PARSER.matcher(rest);
		if (!m.matches()) {
			logger.serious(location, "ill formed normalization, expected type, Annotation and Referent");
			new DummyAnnotation(logger, aset, location, id, getDummyType(rest));
			return;
		}
		String type = m.group("type");
		String annRef = m.group("ann");
		String dbRef = m.group("ref");
		new Normalization(logger, aset, location, id, type, annRef, dbRef);
	}

	private static final Pattern MODIFIER_PARSER = Pattern.compile("(?<type>\\S+) Annotation:(?<ann>\\S+)");

	private static void parseModifier(CheckLogger logger, AnnotationSet aset, Location location, String id, String rest) {
		Matcher m = MODIFIER_PARSER.matcher(rest);
		if (!m.matches()) {
			logger.serious(location, "ill formed modfier, expected type and Annotation");
			new DummyAnnotation(logger, aset, location, id, getDummyType(rest));
			return;
		}
		String type = m.group("type");
		String annRef = m.group("ann");
		new Modifier(logger, aset, location, id, type, annRef);
	}
	
	private static final Pattern DUMMY_TYPE_PARSER = Pattern.compile("(?<type>\\S+)\\s*.*");

	private static String getDummyType(String rest) {
		Matcher m = DUMMY_TYPE_PARSER.matcher(rest);
		if (m.matches()) {
			return m.group("type");
		}
		return DummyAnnotation.DUMMY_TYPE;
	}
}
