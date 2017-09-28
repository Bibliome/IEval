package org.bionlpst.evaluation.similarity;

/**
 * A decorator class that discretizes the similarity provided in the constructor. 
 * If the similarity is below the threshold, then yields 0, otherwise 1.
 * @author rbossy
 *
 * @param <T>
 */
public class SimilarityCutoff<T> implements Similarity<T> {
	private final Similarity<T> similarity;
	private final double threshold;
	private final boolean strict;

	/**
	 * 
	 * @param similarity similarity to discretize.
	 * @param threshold threshold.
	 * @param strict either a value equal to the threshold yields 0.
	 */
	public SimilarityCutoff(Similarity<T> similarity, double threshold, boolean strict) {
		super();
		this.similarity = similarity;
		this.threshold = threshold;
		this.strict = strict;
	}
	
	/**
	 * If the value equals 0, then 0. Otherwise 1.
	 * @param similarity
	 */
	public SimilarityCutoff(Similarity<T> similarity) {
		this(similarity, 0, true);
	}

	@Override
	public double compute(T a, T b) {
		double s = similarity.compute(a, b);
		if (s > threshold) {
			return 1;
		}
		if (s < threshold) {
			return 0;
		}
		return strict ? 0 : 1;
	}

	@Override
	public void explain(StringBuilder sb, T a, T b) {
		similarity.explain(sb, a, b);
		double s = similarity.compute(a, b);
		int r;
		if (s > threshold) {
			sb.append('>');
			r = 1;
		}
		else if (s < threshold) {
			sb.append('<');
			r = 0;
		}
		else {
			r = strict ? 0 : 1;
		}
		if (!strict) {
			sb.append('=');
		}
		sb.append(" -> ");
		sb.append(r);
	}
}