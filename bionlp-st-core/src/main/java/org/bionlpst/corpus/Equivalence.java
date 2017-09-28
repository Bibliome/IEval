package org.bionlpst.corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.bionlpst.util.Location;
import org.bionlpst.util.message.CheckLogger;

/**
 * Annotation equivalence sets.
 * @author rbossy
 *
 */
public class Equivalence {
	private final Document document;
	private final Location location;
	private final Collection<String> annotationReferences = new HashSet<String>();
	private final Collection<Annotation> annotations = new ArrayList<Annotation>();

	/**
	 * Creates a new equivalence set.
	 * @param logger message container where to store warnings and errors.
	 * @param document document to which belongs this equivalence.
	 * @param location location where this location was read.
	 * @param annotationReferences references of equivalent annotations.
	 * @throws NullPointerException if one of the specified parameters is null.
	 */
	public Equivalence(CheckLogger logger, Document document, Location location, Collection<String> annotationReferences) {
		super();
		if (logger == null) {
			throw new NullPointerException();
		}
		if (document == null) {
			throw new NullPointerException();
		}
		if (location == null) {
			throw new NullPointerException();
		}
		if (annotationReferences == null) {
			throw new NullPointerException();
		}
		this.document = document;
		this.location = location;
		this.annotationReferences.addAll(annotationReferences);
		document.addEquivalence(logger, this);
	}

	/**
	 * Returns the document to which belongs this equivalence.
	 * @return the document to which belongs this equivalence.
	 */
	public Document getDocument() {
		return document;
	}

	/**
	 * Returns the location where this equivalence was read.
	 * @return the location where this equivalence was read.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Returns the equivalent annotations. If the references were not resolved, then the returned collection is empty. The returned collection is an unmodifiable view.
	 * @return the equivalent annotations.
	 */
	public Collection<Annotation> getAnnotations() {
		return Collections.unmodifiableCollection(annotations);
	}
	
	/**
	 * Returns either this equivalence contains no annotation.
	 * @return either this equivalence contains no annotation.
	 */
	boolean isEmpty() {
		return annotationReferences.isEmpty();
	}
	
	/**
	 * Resolve the annotations references in this equivalence set.
	 * @param logger message container where to store warnings and errors.
	 */
	public void resolveReferences(CheckLogger logger) {
		for (String id : annotationReferences) {
			AnnotationSet referenceAnnotationSet = document.getReferenceAnnotationSet();
			if (referenceAnnotationSet.hasAnnotation(id)) {
				Annotation result = referenceAnnotationSet.getAnnotation(id);
				annotations.add(result);
				result.setEquivalence(this);
			}
			else {
				logger.serious(location, "cannot resolve annotation reference " + id);
			}
		}
	}

	/**
	 * Returns either this equivalence contains the specified annotation.
	 * @param ann the annotation.
	 * @return either this equivalence contains the specified annotation. If this equivalence reference have not been resolved, then this method returns false.
	 */
	public boolean hasAnnotation(Annotation ann) {
		return annotations.contains(ann);
	}

	/**
	 * Returns either this equivalence and the specified equivalence have common references.
	 * @param equiv the other equivalence.
	 * @return either this equivalence and the specified equivalence have common references.
	 */
	boolean hasIntersection(Equivalence equiv) {
		for (String ref : annotationReferences) {
			if (equiv.annotationReferences.contains(ref)) {
				return true;
			}
		}
		return false;
	}
	
	/**
	 * Adds all references in the specified equivalence in this equivalence.
	 * @param equiv the other equivalence.
	 */
	void merge(Equivalence equiv) {
		annotationReferences.addAll(equiv.annotationReferences);
		annotations.addAll(equiv.annotations);
	}
}
