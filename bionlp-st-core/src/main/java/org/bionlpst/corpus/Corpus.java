package org.bionlpst.corpus;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.TreeMap;

import org.bionlpst.BioNLPSTException;
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
}
