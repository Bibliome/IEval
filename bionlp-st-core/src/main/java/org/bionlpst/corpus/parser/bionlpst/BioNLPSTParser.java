package org.bionlpst.corpus.parser.bionlpst;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.message.CheckLogger;

public class BioNLPSTParser {

	/**
	 * Computes a document identifier from the specified file according to the BioNLP-ST file naming conventions.
	 * @param path file.
	 * @return the document identifier corresponding to the specified file.
	 */
	public static String getDocumentIdFromPath(File path) {
		return getDocumentIdFromPath(path.getName());
	}

	/**
	 * Computes a document identifier from the specified path according to the BioNLP-ST file naming conventions.
	 * @param path path.
	 * @return the document identifier corresponding to the specified path.
	 */
	public static String getDocumentIdFromPath(String path) {
		int dot = path.lastIndexOf('.');
		if (dot == -1) {
			return path;
		}
		int slash = path.lastIndexOf(File.separatorChar);
		return path.substring(slash + 1, dot);
	}

	/**
	 * Parses a file in the BioNLP-ST annotation format. The file name must follow BioNLP-ST file naming conventions. This corpus must already contain a document with the identifier corresponding to the specified file name.
	 * @param logger message container where to store parse warnings and errors.
	 * @param asetSelect specification of the annotation set.
	 * @param file file to parse.
	 * @throws IOException if there's a I/O error reading the specified file.
	 */
	public static void parseAnnotations(CheckLogger logger, Corpus corpus, AnnotationSetSelector asetSelect, File file) throws IOException {
		try (Reader r = new FileReader(file)) {
			parseAnnotations(logger, corpus, asetSelect, file.getPath(), r);
		}
	}

	/**
	 * Parses a stream in the BioNLP-ST annotation format. The source name must follow BioNLP-ST file naming conventions. This corpus must already contain a document with the identifier corresponding to the specified source name.
	 * @param logger message container where to store parse warnings and errors.
	 * @param asetSelect specification of the annotation set.
	 * @param source name of the source.
	 * @param r stream to parse.
	 * @throws IOException if there's a I/O error reading the specified file.
	 */
	public static void parseAnnotations(CheckLogger logger, Corpus corpus, AnnotationSetSelector asetSelect, String source, Reader r) throws IOException {
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

	public static Document newDocument(Corpus corpus, File file) throws IOException, BioNLPSTException {
		try (Reader reader = new FileReader(file)) {
			String id  = getDocumentIdFromPath(file);
			String contents = Util.readWholeStream(reader);
			return new Document(corpus, id, contents);
		}
	}
}
