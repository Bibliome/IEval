package org.bionlpst.evaluation.similarity;

import java.util.Collection;

public class Max<T> extends CompositeSimilarity<T> {
	public Max() {
		super();
	}

	public Max(Collection<Similarity<T>> similarities) {
		super(similarities);
	}

	@Override
	public double compute(T a, T b) {
		double result = 0;
		for (Similarity<T> sim : getSimilarities()) {
			double s = sim.compute(a, b);
			if (s == 1) {
				return 1;
			}
			result = Math.max(result, s);
		}
		return result;
	}

	@Override
	public void explain(StringBuilder sb, T a, T b) {
		sb.append("MAX(");
		explainSimilarities(sb, a, b, ", ");
		sb.append(") = ");
		sb.append(compute(a, b));
	}
}
