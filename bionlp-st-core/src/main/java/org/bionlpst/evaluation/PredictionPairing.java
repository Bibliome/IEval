package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bionlpst.evaluation.similarity.Similarity;

public class PredictionPairing<T> implements PairingAlgorithm<T> {
	private final double threshold;
	private final boolean falsePositivePairs;
	
	public PredictionPairing(double threshold, boolean falsePositivePairs) {
		super();
		this.threshold = threshold;
		this.falsePositivePairs = falsePositivePairs;
	}

	public double getThreshold() {
		return threshold;
	}

	public boolean isFalsePositivePairs() {
		return falsePositivePairs;
	}

	@Override
	public List<Pair<T>> bestPairing(Collection<T> reference, Collection<T> prediction, Similarity<T> similarity) {
		List<Pair<T>> result = new ArrayList<Pair<T>>();
		for (T ref : reference) {
			T pred = getBest(similarity, ref, prediction);
			Pair<T> p = new Pair<T>(ref, pred);
			result.add(p);
		}
		if (falsePositivePairs) {
			Collection<T> seenPreds = new HashSet<T>();
			for (Pair<T> p : result) {
				if (p.hasPrediction()) {
					seenPreds.add(p.getPrediction());
				}
			}
			for (T pred : prediction) {
				if (!seenPreds.contains(pred)) {
					Pair<T> p = new Pair<T>(null, pred);
					result.add(p);
				}
			}
		}
		return result;
	}
	
	private T getBest(Similarity<T> similarity, T ref, Collection<T> prediction) {
		double bestSim = 0;
		T result = null;
		for (T pred : prediction) {
			double s = similarity.compute(ref, pred);
			if (s > threshold && (result == null || s > bestSim)) {
				bestSim = s;
				result = pred;
			}
		}
		return result;
	}
}
