package org.bionlpst.corpus;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.util.Location;
import org.bionlpst.util.message.CheckLogger;

/**
 * Abstract class for annotations that have a single annotation reference, i.e. normalizations and modifiers.
 * @author rbossy
 *
 */
public abstract class SingleReferenceAnnotation extends Annotation {
	private final String annotationReference;
	private Annotation annotation;

	/**
	 * Creates a new single reference annotation.
	 * @param annotationSet annotation set to which belongs this annotation.
	 * @param location location where this annotation has been read.
	 * @param id identifier of this annotation.
	 * @param type type of this annotation.
	 * @param annotationReference reference to the annotation.
	 * @throws BioNLPSTException if either the specified annotation set or its parent already contain an annotation with the same identifier as this annotation.
	 * @throws NullPointerException if one of the specified parameters is null.
	 */
	protected SingleReferenceAnnotation(CheckLogger logger, AnnotationSet annotationSet, Location location, String id, String type, String annotationReference) throws BioNLPSTException, NullPointerException {
		super(logger, annotationSet, location, id, type);
		this.annotationReference = annotationReference;
	}

	/**
	 * Returns the referenced annotation.
	 * @return the referenced annotation.
	 * @throws IllegalStateException if references have not been resolved.
	 */
	public Annotation getAnnotation() throws IllegalStateException {
		if (annotation == null) {
			throw new IllegalStateException("references were not resolved");
		}
		return annotation;
	}

	@Override
	public void resolveReferences(CheckLogger logger) throws BioNLPSTException {
		annotation = resolveReference(logger, annotationReference);
	}
}
