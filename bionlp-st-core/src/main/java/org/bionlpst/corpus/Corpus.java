package org.bionlpst.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.util.Location;
import org.bionlpst.util.message.CheckLogger;

/**
 * The Corpus object represents a collection of documents.
 * @author rbossy
 *
 */
public class Corpus implements DocumentCollection {
	private final Map<String,Document> documents = new TreeMap<String,Document>();
	
	/**
	 * Create a corpus.
	 */
	public Corpus() {
		super();
	}

	/**
	 * Returns either this corpus contains a document with the specified identifier.
	 * @param id the identifier of the document.
	 * @return true if this corpus contains a document with the specified identifier, false otherwise.
	 */
	public boolean hasDocument(String id) {
		return documents.containsKey(id);
	}
	
	/**
	 * Retrieves the document with the specified identifier in this corpus.
	 * @param id identifier of the document.
	 * @return the document in this corpus with the specified identifier.
	 * @throws BioNLPSTException if this corpus has no document with the specified identifier.
	 */
	public Document getDocument(String id) throws BioNLPSTException {
		if (documents.containsKey(id)) {
			return documents.get(id);
		}
		throw new BioNLPSTException("unknown document identifier: " + id);
	}
	
	/**
	 * Adds a document to this corpus. This method is called by Document constructor and should not be called directly.
	 * @param doc the document to add to this corpus.
	 * @throws BioNLPSTException if this corpus already has a document with the same identifier as the specified document.
	 */
	void addDocument(Document doc) throws BioNLPSTException {
		String id = doc.getId();
		if (documents.containsKey(id)) {
			throw new BioNLPSTException("duplicate document identifier: " + id);
		}
		documents.put(id, doc);
	}
	
	/**
	 * Returns all the documents in this corpus. The result is an unmodifiable collection.
	 * @return an unmodifiable collection containing all documents in this corpus.
	 */
	@Override
	public Collection<Document> getDocuments() {
		return Collections.unmodifiableCollection(documents.values());
	}

	/**
	 * Resolve all references in all annotations in this corpus. This method should be called after the corpus is fully constructed (e.g. parsed) and before any further processing (e.g. schema checking).
	 * @param logger message container where to store warnings and errors.
	 */
	public void resolveReferences(CheckLogger logger) {
		for (Document doc : documents.values()) {
			doc.resolveReferences(logger);
		}
	}

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
	public void parseAnnotations(CheckLogger logger, AnnotationSetSelector asetSelect, File file) throws IOException {
		try (Reader r = new FileReader(file)) {
			parseAnnotations(logger, asetSelect, file.getPath(), r);
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
	public void parseAnnotations(CheckLogger logger, AnnotationSetSelector asetSelect, String source, Reader r) throws IOException {
		String docId = getDocumentIdFromPath(source);
		if (!hasDocument(docId)) {
			logger.serious(new Location(source, -1), "unknown document " + docId);
			return;
		}
		Document doc = documents.get(docId);
		AnnotationSet aset = asetSelect.getAnnotationSet(doc);
		BufferedReader reader = new BufferedReader(r);
		aset.parseAnnotations(logger, source, reader);
	}
}
