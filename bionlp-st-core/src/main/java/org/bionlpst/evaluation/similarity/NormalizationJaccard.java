package org.bionlpst.evaluation.similarity;

import java.util.Collection;

public enum NormalizationJaccard implements Similarity<Collection<String>> {
	INSTANCE;
	
	@Override
	public double compute(Collection<String> as, Collection<String> bs) {
		double inter = 0;
		double union = as.size();
		for (String a : as) {
			if (bs.contains(a)) {
				inter++;
			}
			else {
				union++;
			}
		}
		return inter / union;
	}

	@Override
	public void explain(StringBuilder sb, Collection<String> a, Collection<String> b) {
		sb.append("norm jaccard = ");
		sb.append(compute(a, b));
	}
}
