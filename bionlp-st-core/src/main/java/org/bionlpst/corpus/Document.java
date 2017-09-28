package org.bionlpst.corpus;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.util.Util;
import org.bionlpst.util.message.CheckLogger;

/**
 * A document object represent an annotated document.
 * @author rbossy
 *
 */
public class Document implements DocumentCollection {
	private final Corpus corpus;
	private final String id;
	private final String contents;
	private final AnnotationSet inputAnnotationSet = new AnnotationSet(this, AnnotationSetSelector.INPUT, null);
	private final AnnotationSet referenceAnnotationSet = new AnnotationSet(this, AnnotationSetSelector.REFERENCE, inputAnnotationSet);
	private final AnnotationSet predictionAnnotationSet = new AnnotationSet(this, AnnotationSetSelector.PREDICTION, inputAnnotationSet);
	private final Collection<Equivalence> equivalences = new ArrayList<Equivalence>();

	/**
	 * Creates a new document.
	 * @param corpus collection to which belongs this document.
	 * @param id identifier of this document.
	 * @param contents text contents of this document.
	 * @throws BioNLPSTException if the specified corpus already contains a document with the same identifier as this document.
	 * @throws NullPointerException if one of the specified parameters is null.
	 */
	public Document(Corpus corpus, String id, String contents) throws BioNLPSTException, NullPointerException {
		super();
		this.corpus = Util.notnull(corpus);
		this.id = Util.notnull(id);
		this.contents = Util.notnull(contents);
		corpus.addDocument(this);
	}

	/**
	 * Creates a new document. The text contents is read from the specified stream.
	 * @param corpus collection to which belongs this document.
	 * @param id identifier of this document.
	 * @param reader stream where the text contents is read.
	 * @throws BioNLPSTException if the specified corpus already contains a document with the same identifier as this document.
	 * @throws IOException if there is an I/O error reading the stream.
	 */
	public Document(Corpus corpus, String id, Reader reader) throws IOException, BioNLPSTException {
		this(corpus, id, Util.readWholeStream(reader));
	}

	/**
	 * Creates a new document. The text contents is read from the specified stream.
	 * @param corpus collection to which belongs this document.
	 * @param id identifier of this document.
	 * @param is stream where the text contents is read.
	 * @throws BioNLPSTException if the specified corpus already contains a document with the same identifier as this document.
	 * @throws IOException if there is an I/O error reading the stream.
	 */
	public Document(Corpus corpus, String id, InputStream is) throws IOException, BioNLPSTException
	{
		this(corpus, id, Util.readWholeStream(new InputStreamReader(is)));
	}
	
	/**
	 * Creates a new document. The text contents is read from the specified file. The identifier is determined by the name of the specified file that must follow BioNLP-ST file name conventions.
	 * @param corpus collection to which belongs this document.
	 * @param file file containing the text contents.
	 * @throws BioNLPSTException if the specified corpus already contains a document with the same identifier as this document.
	 * @throws IOException if there is an I/O error reading the file.
	 */
	public Document(Corpus corpus, File file) throws IOException, BioNLPSTException {
		try (Reader reader = new FileReader(file)) {
			this.corpus = corpus;
			this.id = Corpus.getDocumentIdFromPath(file);
			this.contents = Util.readWholeStream(reader);
		}
		corpus.addDocument(this);
	}

	/**
	 * Returns the corpus to which this document belongs.
	 * @return the corpus to which this document belongs.
	 */
	public Corpus getCorpus() {
		return corpus;
	}

	/**
	 * Returns the identifier of this document.
	 * @return the identifier of this document.
	 */
	public String getId() {
		return id;
	}

	/**
	 * Returns the text contents of this document.
	 * @return the text contents of this document.
	 */
	public String getContents() {
		return contents;
	}

	/**
	 * Returns the annotation set containing input annotations.
	 * @return the annotation set containing input annotations. If there are no input annotations the returned object is empty.
	 */
	public AnnotationSet getInputAnnotationSet() {
		return inputAnnotationSet;
	}

	/**
	 * Returns the annotation set containing reference annotations.
	 * @return the annotation set containing reference annotations. If there are no input annotations the returned object is empty.
	 */
	public AnnotationSet getReferenceAnnotationSet() {
		return referenceAnnotationSet;
	}

	/**
	 * Returns the annotation set containing predicted annotations.
	 * @return the annotation set containing predicted annotations. If there are no input annotations the returned object is empty.
	 */
	public AnnotationSet getPredictionAnnotationSet() {
		return predictionAnnotationSet;
	}

	/**
	 * Returns all annotation equivalences in this document.
	 * @return all annotation equivalences in this document. If there are no equivalences, then the returned collection is empty.
	 */
	public Collection<Equivalence> getEquivalences() {
		return Collections.unmodifiableCollection(equivalences);
	}

	/**
	 * Adds an equivalence in this document. This method is called by the Equivalence constructor, it is not meant to be called directly. This method merges equivalences already in this document that share a reference with the specified equivalence.
	 * @param logger message container where to store warnings and errors.
	 * @param equivalence equivalence to add
	 */
	void addEquivalence(CheckLogger logger, Equivalence equivalence) {
		Iterator<Equivalence> it = this.equivalences.iterator();
		while (it.hasNext()) {
			Equivalence equiv = it.next();
			if (equiv.hasIntersection(equivalence)) {
				equivalence.merge(equiv);
				it.remove();
				logger.suspicious(equivalence.getLocation(), "overlapping equivalences (" + equiv.getLocation().getMessage("") + ")");
			}
		}
		if (equivalence.isEmpty()) {
			logger.suspicious(equivalence.getLocation(), "empty equivalence (" + equivalence.getLocation().getMessage("") + ")");
		}
		else {
			equivalences.add(equivalence);
		}
	}

	/**
	 * Resolve all references of all annotations and equivalences in this document.
	 * @param logger message container where to store warnings and errors.
	 * @see Corpus#resolveReferences(CheckLogger)
	 */
	public void resolveReferences(CheckLogger logger) {
		inputAnnotationSet.resolveReferences(logger);
		referenceAnnotationSet.resolveReferences(logger);
		predictionAnnotationSet.resolveReferences(logger);
		for (Equivalence equiv : equivalences) {
			equiv.resolveReferences(logger);
		}
	}

	@Override
	public Collection<Document> getDocuments() {
		return Collections.singleton(this);
	}
}
