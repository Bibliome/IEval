package org.bionlpst.evaluation.similarity;

import java.util.Collection;
import java.util.Iterator;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.util.fragment.ImmutableFragment;

/**
 * Jaccard index between two text bound annotations.
 * @author rbossy
 *
 */
public enum TextBoundJaccard implements Similarity<Annotation> {
	INSTANCE;
	
	private static int intersection(TextBound t1, TextBound t2) {
		int result = 0;
		Collection<ImmutableFragment> c1 = t1.getFragments();
		Collection<ImmutableFragment> c2 = t2.getFragments();
		Iterator<ImmutableFragment> i1 = c1.iterator();
		Iterator<ImmutableFragment> i2 = c2.iterator();
		ImmutableFragment f1 = i1.next();
		ImmutableFragment f2 = i2.next();
		while (true) {
			result += intersection(f1, f2);
			if (f1.getEnd() >= f2.getEnd()) {
				if (i2.hasNext())
					f2 = i2.next();
				else
					break;
			}
			if (f1.getEnd() <= f2.getEnd()) {
				if (i1.hasNext())
					f1 = i1.next();
				else
					break;
			}
		}
		return result;
	}

	private static int intersection(ImmutableFragment f1, ImmutableFragment f2) {
		if (f1.getStart() >= f2.getEnd())
			return 0;
		if (f2.getStart() >= f1.getEnd())
			return 0;
		return Math.min(f1.getEnd(), f2.getEnd()) - Math.max(f1.getStart(), f2.getStart());
	}

	@Override
	public double compute(Annotation aa, Annotation ba) {
		TextBound a = aa.asTextBound();
		TextBound b = ba.asTextBound();
		if (a == null || b == null) {
			return 0;
		}
		double intersection = intersection(a, b);
		double union = a.getLength() + b.getLength() - intersection;
		return intersection / union;
	}

	@Override
	public void explain(StringBuilder sb, Annotation a, Annotation b) {
		sb.append("jaccard = ");
		sb.append(compute(a, b));
	}
}
