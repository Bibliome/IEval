package org.bionlpst.evaluation;

import java.util.Collection;

import org.bionlpst.evaluation.similarity.Similarity;

/**
 * A measure computes a number for a pairing.
 * @author rbossy
 *
 */
public interface Measure {
	String getName();
	MeasureDirection getMeasureDirection();
	<T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs);
}
