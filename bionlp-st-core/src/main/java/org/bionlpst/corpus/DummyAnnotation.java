package org.bionlpst.corpus;

import org.bionlpst.util.Location;
import org.bionlpst.util.message.CheckLogger;

/**
 * Dummy annotations are created as place holders if the parser could not determine the kind of an annotation.
 * If there are no parsing errors, then there is no dummy annotations in a document.
 * @author rbossy
 *
 */
public class DummyAnnotation extends Annotation {
	/**
	 * Dummy type if the type cannot be determined.
	 */
	public static final String DUMMY_TYPE = "DUMMY";
	
	DummyAnnotation(CheckLogger logger, AnnotationSet annotationCollection, Location location, String id, String type) {
		super(logger, annotationCollection, location, id, type);
	}

	DummyAnnotation(CheckLogger logger, AnnotationSet annotationCollection, Location location, String id) {
		this(logger, annotationCollection, location, id, DUMMY_TYPE);
	}

	@Override
	public void resolveReferences(CheckLogger logger) {
	}

	@Override
	public <R,P> R accept(AnnotationVisitor<R,P> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public AnnotationKind getKind() {
		return AnnotationKind.DUMMY;
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
		return null;
	}

	@Override
	public Modifier asModifier() {
		return null;
	}

	@Override
	public Normalization asNormalization() {
		return null;
	}
}
