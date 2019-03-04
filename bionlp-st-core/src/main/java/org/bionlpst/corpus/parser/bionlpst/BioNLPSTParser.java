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

import org.bionlpst.BioNLPSTException;
import org.bionlpst.app.source.CorpusSource;
import org.bionlpst.app.source.EntryIterator;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.message.CheckLogger;

public class BioNLPSTParser {
	public static final String EXT_CONTENTS = ".txt";
	public static final String EXT_INPUT = ".a1";
	public static final String EXT_OUTPUT = ".a2";
	
	private static final String[] EXTS_ALL = { EXT_CONTENTS, EXT_INPUT, EXT_OUTPUT };
	private static final String[] EXTS_OUTPUT_ONLY = { EXT_CONTENTS, EXT_INPUT, EXT_OUTPUT };

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
		aset.parseAnnotations(logger, source, reader);
	}

	public static void getCorpusAndReference(CheckLogger logger, CorpusSource corpusSource, Corpus corpus, boolean loadOutput) throws BioNLPSTException, IOException {
		Collection<EntryRecord> records = collectEntries(corpusSource, EXTS_ALL);
		loadDocuments(corpus, records);
		loadInputAnnotations(logger, corpus, records);
		if (loadOutput) {
			loadOutputAnnotations(logger, corpus, records, AnnotationSetSelector.REFERENCE);
		}
	}

	public static Corpus getCorpusAndReference(CheckLogger logger, CorpusSource corpusSource, boolean loadOutput) throws BioNLPSTException, IOException {
		Corpus result = new Corpus();
		getCorpusAndReference(logger, corpusSource, result, loadOutput);
		return result;
	}
	
	public static void getPredictions(CheckLogger logger, CorpusSource corpusSource, Corpus corpus) throws BioNLPSTException, IOException {
		Collection<EntryRecord> records = collectEntries(corpusSource, EXTS_OUTPUT_ONLY);
		loadOutputAnnotations(logger, corpus, records, AnnotationSetSelector.PREDICTION);
	}

	private static Collection<EntryRecord> collectEntries(CorpusSource corpusSource, String... exts) throws IOException {
		Collection<EntryRecord> result = new ArrayList<EntryRecord>();
		EntryIterator it = corpusSource.getEntries();
		while (it.next()) {
			String name = it.getName();
			if (matchExt(name, exts)) {
				InputStream contents = it.getContents();
				EntryRecord z = new EntryRecord(name, contents);
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

	private static void loadDocuments(Corpus corpus, Collection<EntryRecord> records) {
		for (EntryRecord u : records) {
			if (u.isDocumentContents()) {
				u.createDocument(corpus);
			}
		}
	}
	
	private static void loadInputAnnotations(CheckLogger logger, Corpus corpus, Collection<EntryRecord> record) throws IOException {
		for (EntryRecord u : record) {
			if (u.isInputAnnotationSet()) {
				u.createInputAnnotations(logger, corpus);
			}
		}
	}
	
	private static void loadOutputAnnotations(CheckLogger logger, Corpus corpus, Collection<EntryRecord> units, AnnotationSetSelector loadOutput) throws IOException {
		for (EntryRecord u : units) {
			if (u.isOutputAnnotationSet()) {
				u.createOutputAnnotations(logger, corpus, loadOutput);
			}
		}
	}

	private static class EntryRecord {
		private final String name;
		private final String contents;

		private EntryRecord(String name, InputStream contents) throws IOException {
			this.name = name;
			this.contents = Util.readWholeStream(new InputStreamReader(contents));
		}
		
		private boolean isDocumentContents() {
			return name.endsWith(EXT_CONTENTS);
		}
		
		private void createDocument(Corpus corpus) {
			String docId = BioNLPSTParser.getDocumentIdFromPath(name);
			new Document(corpus, docId, contents);
		}
		
		private boolean isInputAnnotationSet() {
			return name.endsWith(EXT_INPUT);
		}
		
		private void createInputAnnotations(CheckLogger logger, Corpus corpus) throws IOException {
			Reader reader = new StringReader(contents);
			BioNLPSTParser.parseAnnotations(logger, corpus, AnnotationSetSelector.INPUT, name, reader);
		}
		
		private boolean isOutputAnnotationSet() {
			return name.endsWith(EXT_OUTPUT);
		}
		
		private void createOutputAnnotations(CheckLogger logger, Corpus corpus, AnnotationSetSelector loadOutput) throws IOException {
			Reader reader = new StringReader(contents);
			BioNLPSTParser.parseAnnotations(logger, corpus, loadOutput, name, reader);
		}
	}
}
