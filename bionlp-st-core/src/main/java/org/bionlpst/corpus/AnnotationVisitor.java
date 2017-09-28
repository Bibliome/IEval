package org.bionlpst.corpus;

/**
 * Annotation visitor.
 * @author rbossy
 *
 * @param <R> return type (use {@link Void} if none).
 * @param <P> parameter type (use {@link Void} if none).
 */
public interface AnnotationVisitor<R,P> {
	R visit(TextBound textBound, P param);
	R visit(Relation relation, P param);
	R visit(Normalization normalization, P param);
	R visit(Modifier modifier, P param);
	R visit(DummyAnnotation dummy, P param);
}
