package org.bionlpst.evaluation.similarity;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.TextBound;

public class TextBoundOverlap implements Similarity<Annotation> {
	private final double overlapSimilarity;

	public TextBoundOverlap(double overlapSimilarity) {
		super();
		this.overlapSimilarity = overlapSimilarity;
	}
	
	public TextBoundOverlap() {
		this(0.0);
	}

	@Override
	public double compute(Annotation aa, Annotation ba) {
		TextBound a = aa.asTextBound();
		TextBound b = ba.asTextBound();
		if (a == null || b == null) {
			return 0;
		}
		if ((a.getStart() == b.getStart()) && (a.getEnd() == b.getEnd())) {
			return 1;
		}
		if ((a.getStart() >= b.getEnd()) || (b.getStart() >= a.getEnd())) {
			return 0;
		}
		return overlapSimilarity;
	}

	@Override
	public void explain(StringBuilder sb, Annotation a, Annotation b) {
		sb.append("overlap = ");
		sb.append(compute(a, b));
	}
}
