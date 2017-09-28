package org.bionlpst.evaluation.similarity;

import org.bionlpst.corpus.Annotation;

/**
 * Yields 1 if both annotations are of the same kind. Otherwise 0.
 * @author rbossy
 *
 * @param <T>
 */
public enum AnnotationKindSimilarity implements Similarity<Annotation> {
	INSTANCE;
	
	@Override
	public double compute(Annotation a, Annotation b) {
		if (a.getKind() == b.getKind()) {
			return 1;
		}
		return 0;
	}

	@Override
	public void explain(StringBuilder sb, Annotation a, Annotation b) {
		sb.append(a.getKind());
		sb.append('/');
		sb.append(b.getKind());
		sb.append(" = ");
		sb.append(compute(a, b));
	}
}
