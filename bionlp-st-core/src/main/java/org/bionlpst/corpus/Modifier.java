package org.bionlpst.corpus;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.util.Location;
import org.bionlpst.util.message.CheckLogger;

/**
 * Modifier annotations.
 * @author rbossy
 *
 */
public class Modifier extends SingleReferenceAnnotation {
	/**
	 * Creates a new single modifier annotation.
	 * @param annotationSet annotation set to which belongs this annotation.
	 * @param location location where this annotation has been read.
	 * @param id identifier of this annotation.
	 * @param type type of this annotation.
	 * @param annotationReference reference to the annotation.
	 * @throws BioNLPSTException if either the specified annotation set or its parent already contain an annotation with the same identifier as this annotation.
	 * @throws NullPointerException if one of the specified parameters is null.
	 */
	public Modifier(CheckLogger logger, AnnotationSet annotationCollection, Location location, String id, String type, String annotationReference) throws BioNLPSTException, NullPointerException {
		super(logger, annotationCollection, location, id, type, annotationReference);
	}

	@Override
	public <R,P> R accept(AnnotationVisitor<R,P> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public AnnotationKind getKind() {
		return AnnotationKind.MODIFIER;
	}

	@Override
	public TextBound asTextBound() {
		return null;
	}

	@Override
	public Relation asRelation() {
		return null;
	}

	@Override
	public SingleReferenceAnnotation asSingleReferenceAnnotation() {
		return this;
	}

	@Override
	public Modifier asModifier() {
		return this;
	}

	@Override
	public Normalization asNormalization() {
		return null;
	}
}
