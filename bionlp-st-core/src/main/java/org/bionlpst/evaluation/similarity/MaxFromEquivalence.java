package org.bionlpst.evaluation.similarity;

import org.bionlpst.corpus.Annotation;

public class MaxFromEquivalence implements Similarity<Annotation> {
	private final Similarity<Annotation> similarity;
	
	public MaxFromEquivalence(Similarity<Annotation> similarity) {
		super();
		this.similarity = similarity;
	}

	@Override
	public double compute(Annotation a, Annotation b) {
		double result = 0;
		for (Annotation aq : a.getEquivalents()) {
			double s = similarity.compute(aq, b);
			if (s == 1) {
				return 1;
			}
			if (s > result) {
				result = s;
			}
		}
		return result;
	}

	@Override
	public void explain(StringBuilder sb, Annotation a, Annotation b) {
		sb.append("MAX EQUIV ");
		double result = 0;
		Annotation best = null;
		for (Annotation aq : a.getEquivalents()) {
			double s = similarity.compute(aq, b);
			if (s == 1) {
				best = aq;
				result = s;
				break;
			}
			if (s > result) {
				result = s;
				best = aq;
			}
		}
		sb.append(best.getId());
		sb.append(": ");
		similarity.explain(sb, best, b);
	}
}
