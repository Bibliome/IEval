package org.bionlpst.app.source;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.message.CheckLogger;

public abstract class CorpusSource {
	public static final String EXT_CONTENTS = ".txt";
	public static final String EXT_INPUT = ".a1";
	public static final String EXT_OUTPUT = ".a2";
	
	private static final String[] EXTS_ALL = { EXT_CONTENTS, EXT_INPUT, EXT_OUTPUT };
	private static final String[] EXTS_OUTPUT_ONLY = { EXT_CONTENTS, EXT_INPUT, EXT_OUTPUT };
	
	public void getCorpusAndReference(CheckLogger logger, Corpus corpus, boolean loadOutput) throws BioNLPSTException, IOException {
		Location location = new Location("<<getCorpusAndReference>>", 0);
		Collection<EntryRecord> records = collectEntries(logger, EXTS_ALL);
		logger.information(location, "records.size() = " + records.size());
		loadDocuments(corpus, records);
		loadInputAnnotations(logger, corpus, records);
		if (loadOutput) {
			loadOutputAnnotations(logger, corpus, records, AnnotationSetSelector.REFERENCE);
		}
	}
	
	public Corpus getCorpusAndReference(CheckLogger logger, boolean loadOutput) throws BioNLPSTException, IOException {
		Corpus result = new Corpus();
		getCorpusAndReference(logger, result, loadOutput);
		return result;
	}
	
	public void getPredictions(CheckLogger logger, Corpus corpus) throws BioNLPSTException, IOException {
		Collection<EntryRecord> records = collectEntries(logger, EXTS_OUTPUT_ONLY);
		loadOutputAnnotations(logger, corpus, records, AnnotationSetSelector.PREDICTION);
	}
	
	protected abstract EntryIterator getEntries() throws IOException;

	private Collection<EntryRecord> collectEntries(CheckLogger logger, String... exts) throws IOException {
		Location location = new Location("<<collectEntries>>", 0);
		Collection<EntryRecord> result = new ArrayList<EntryRecord>();
		EntryIterator it = getEntries();
		while (it.next()) {
			String name = it.getName();
			logger.information(location, "name = " + name);
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
			String docId = Corpus.getDocumentIdFromPath(name);
			new Document(corpus, docId, contents);
		}
		
		private boolean isInputAnnotationSet() {
			return name.endsWith(CorpusSource.EXT_INPUT);
		}
		
		private void createInputAnnotations(CheckLogger logger, Corpus corpus) throws IOException {
			Reader reader = new StringReader(contents);
			corpus.parseAnnotations(logger, AnnotationSetSelector.INPUT, name, reader);
		}
		
		private boolean isOutputAnnotationSet() {
			return name.endsWith(CorpusSource.EXT_OUTPUT);
		}
		
		private void createOutputAnnotations(CheckLogger logger, Corpus corpus, AnnotationSetSelector loadOutput) throws IOException {
			Reader reader = new StringReader(contents);
			corpus.parseAnnotations(logger, loadOutput, name, reader);
		}
	}
	
	public abstract String getName();
}
