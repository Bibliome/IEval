package org.bionlpst.evaluation.similarity;


public class Identity<T> implements Similarity<T> {
	public Identity() {
		super();
	}

	@Override
	public double compute(T a, T b) {
		return a.equals(b) ? 1 : 0;
	}

	@Override
	public void explain(StringBuilder sb, T a, T b) {
		sb.append(a.equals(b) ? "eq = 1" : "neq = 0");
	}
}
