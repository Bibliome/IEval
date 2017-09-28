package org.bionlpst.evaluation.similarity;

import java.util.Collection;

public class Min<T> extends CompositeSimilarity<T> {
	public Min() {
		super();
	}

	public Min(Collection<Similarity<T>> similarities) {
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
			result = Math.min(result, s);
		}
		return result;
	}

	@Override
	public void explain(StringBuilder sb, T a, T b) {
		sb.append("(");
		explainSimilarities(sb, a, b, ", ");
		sb.append(") = ");
		sb.append(compute(a, b));
	}
}
