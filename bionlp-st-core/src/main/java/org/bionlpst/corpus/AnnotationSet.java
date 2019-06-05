package org.bionlpst.corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.util.Filter;
import org.bionlpst.util.Util;
import org.bionlpst.util.message.CheckLogger;

/**
 * An annotation set object is a container for annotations.
 * @author rbossy
 *
 */
public class AnnotationSet {
	private final Document document;
	private final AnnotationSetSelector selector;
	private final AnnotationSet parent;
	private final Map<String,Annotation> annotations = new HashMap<String,Annotation>();
	private boolean parsed = false;
	
	/**
	 * Creates a new annotation set.
	 * @param document document to which all annotations in this object belong.
	 * @param parent parent annotation set. Output annotation sets have the input annotation set as parent.
	 * @throws NullPointerException if the specified document is null.
	 */
	public AnnotationSet(Document document, AnnotationSetSelector selector, AnnotationSet parent) throws NullPointerException {
		super();
		if (document == null) {
			throw new NullPointerException();
		}
		this.document = document;
		this.selector = selector;
		this.parent = parent;
		if (parent != null && parent.document != document) {
			throw new BioNLPSTException();
		}
	}

	/**
	 * Returns the document to which belongs this annotation set.
	 * @return the document to which belongs this annotation set.
	 */
	public Document getDocument() {
		return document;
	}

	public AnnotationSetSelector getSelector() {
		return selector;
	}

	/**
	 * Returns the parent annotation set.
	 * @return the parent annotation set. If this annotation set has no parent, then returns null.
	 */
	public AnnotationSet getParent() {
		return parent;
	}
	
	/**
	 * Returns all annotations in this annotation set.
	 * @return all annotations in this annotation set. If there are no annotations in this annotation set then the returned collection is empty. The returned collection is an unmodifiable view.
	 */
	public Collection<Annotation> getAnnotations() {
		return Collections.unmodifiableCollection(annotations.values());
	}

	/**
	 * Collects all annotations in this annotation set that satisfy the specified filter in the specified collection.
	 * @param filter annotation filter.
	 * @param target collection where to store annotations.
	 * @return target.
	 */
	public Collection<Annotation> collectAnnotations(Filter<Annotation> filter, Collection<Annotation> target) {
		return Util.filter(filter, annotations.values(), target);
	}

	/**
	 * Returns all annotations in this annotation set that satisfy the specified filter in the specified collection.
	 * @param filter annotation filter.
	 * @param target collection where to store annotations.
	 * @return all annotations that satisfy the specified filter in the specified collection.
	 */
	public Collection<Annotation> getAnnotations(Filter<Annotation> filter) {
		return Util.filter(filter, annotations.values());
	}
	
	/**
	 * Returns all annotations of the specified types in this annotation set.
	 * @param types types of annotations.
	 * @return all annotations of the specified types in this annotation set.
	 */
	public Collection<Annotation> getAnnotations(String... types) {
		return getAnnotations(new AnnotationTypeFilter<Annotation>(types));
	}
	
	/**
	 * Returns all annotations of the specified types in this annotation set.
	 * @param types types of annotations.
	 * @return all annotations of the specified types in this annotation set.
	 */
	public Collection<Annotation> getAnnotations(Collection<String> types) {
		return getAnnotations(new AnnotationTypeFilter<Annotation>(types));
	}

	/**
	 * Returns true if this annotation set contains an annotation with the specified identifier. This method searches for annotations in this annotation set, then in the parent annotation set.
	 * @param id annotation identifier.
	 * @return true if this annotation set or its parent contains an annotation with the specified identifier.
	 */
	public boolean hasAnnotation(String id) {
		if (annotations.containsKey(id)) {
			return true;
		}
		if (parent == null) {
			return false;
		}
		return parent.hasAnnotation(id);
	}
	
	/**
	 * Returns the annotation with the specified identifier in this annotation set or the parent annotation set.
	 * @param id annotation identifier.
	 * @return the annotation with the specified identifier in this annotation set or the parent annotation set.
	 * @throws BioNLPSTException if neither this annotation set or the parent contains an annotation with the specified identifier.
	 */
	public Annotation getAnnotation(String id) throws BioNLPSTException {
		if (annotations.containsKey(id)) {
			return annotations.get(id);
		}
		if (parent == null) {
			throw new BioNLPSTException("unknown annotation with id: " + id);
		}
		return parent.getAnnotation(id);
	}

	public boolean isParsed() {
		return parsed;
	}

	public void setParsed() {
		parsed = true;
	}
	/**
	 * Adds an annotation to this annotation set. This method is called by the Annotation constructor, it is not meant to be called directly.
	 * @param ann the annotation to add to this annotation set.
	 * @throws BioNLPSTException if either this annotation set or the parent annotation set already contain an annotation with the same identifier as the specified annotation.
	 */
	void addAnnotation(CheckLogger logger, Annotation ann) throws BioNLPSTException {
		String id = ann.getId();
		if (hasAnnotation(id)) {
			Annotation prev = getAnnotation(id);
			logger.serious(ann.getLocation(), "duplicate annotation identifier: " + id + " (" + ann.getLocation().getMessage("") + "/" + ann.getAnnotationSet().selector + ", " + prev.getLocation().getMessage("") + "/" + prev.getAnnotationSet().selector + ") -- we assign a random identifier");
			id = id + "-" + UUID.randomUUID();
			ann.setId(id);
		}
		annotations.put(id, ann);
	}

	/**
	 * Resolve all references of all annotations contained in this annotation set.
	 * @param logger message container where to store warnings and errors.
	 */
	public void resolveReferences(CheckLogger logger) {
		Collection<Annotation> annotations = new ArrayList<Annotation>(this.annotations.values());
		for (Annotation ann : annotations) {
			ann.resolveReferences(logger);
		}
	}
	
	public void removeAnnotation(String id) {
		annotations.remove(id);
	}
}
