package org.bionlpst.evaluation.similarity;

/**
 * A similarity computes a score between 0 (complete mismatch) and 1 (perfect match).
 * @author rbossy
 *
 * @param <T>
 */
public interface Similarity<T> {
	double compute(T a, T b);
	void explain(StringBuilder sb, T a, T b);
}
