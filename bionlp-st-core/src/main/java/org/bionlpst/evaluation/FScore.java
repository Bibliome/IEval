package org.bionlpst.evaluation;

import java.util.Collection;

import org.bionlpst.evaluation.similarity.Similarity;

/**
 * Computes F-beta score.
 * @author rbossy
 *
 */
public class FScore implements Measure {
	private final double beta;
	private final double betaSquared;

	public FScore(double beta) {
		super();
		this.beta = beta;
		this.betaSquared = beta * beta;
	}

	public double getBeta() {
		return beta;
	}

	@Override
	public <T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
		double recall = StandardMeasures.RECALL.compute(similarity, pairs).doubleValue();
		double precision = StandardMeasures.PRECISION.compute(similarity, pairs).doubleValue();
		return (1 + betaSquared) * ((recall * precision) / (recall + (betaSquared * precision)));
	}

	@Override
	public String getName() {
		return String.format("F-%.2f", beta);
	}

	@Override
	public MeasureDirection getMeasureDirection() {
		return MeasureDirection.HIGHER_IS_BETTER;
	}
}
