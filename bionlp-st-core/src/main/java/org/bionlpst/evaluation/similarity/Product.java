package org.bionlpst.evaluation.similarity;

import java.util.Collection;

/**
 * The product between several similarities.
 * @author rbossy
 *
 * @param <T>
 */
public class Product<T> extends CompositeSimilarity<T> {
	public Product() {
		super();
	}

	public Product(Collection<Similarity<T>> similarities) {
		super(similarities);
	}

	@Override
	public double compute(T a, T b) {
		double result = 1;
		for (Similarity<T> sim : getSimilarities()) {
			double s = sim.compute(a, b);
			if (s == 0) {
				return 0;
			}
			result *= s;
		}
		return result;
	}

	@Override
	public void explain(StringBuilder sb, T a, T b) {
		sb.append('(');
		explainSimilarities(sb, a, b, " . ");
		sb.append(") = ");
		sb.append(compute(a, b));
	}
}