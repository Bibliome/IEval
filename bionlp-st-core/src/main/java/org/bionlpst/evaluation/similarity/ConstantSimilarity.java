package org.bionlpst.evaluation.similarity;

/**
 * Similarity that always yields the same value.
 * @author rbossy
 *
 * @param <T>
 */
public class ConstantSimilarity<T> implements Similarity<T> {
	private final double value;

	public ConstantSimilarity(double value) {
		super();
		this.value = value;
	}
	
	/**
	 * Always yields 1.
	 */
	public ConstantSimilarity() {
		this(1);
	}

	@Override
	public double compute(T a, T b) {
		return value;
	}

	@Override
	public void explain(StringBuilder sb, T a, T b) {
		sb.append(value);
	}
}