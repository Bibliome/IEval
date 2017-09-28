package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

import org.bionlpst.evaluation.similarity.Similarity;

public class ReferencePairing<T> implements PairingAlgorithm<T> {
	private final double threshold;
	private final boolean falseNegativePairs;
	
	public ReferencePairing(double threshold, boolean falseNegativePairs) {
		super();
		this.threshold = threshold;
		this.falseNegativePairs = falseNegativePairs;
	}

	public double getThreshold() {
		return threshold;
	}

	public boolean isFalseNegativePairs() {
		return falseNegativePairs;
	}

	@Override
	public List<Pair<T>> bestPairing(Collection<T> reference, Collection<T> prediction, Similarity<T> similarity) {
		List<Pair<T>> result = new ArrayList<Pair<T>>();
		for (T pred : prediction) {
			T ref = getBest(similarity, pred, reference);
			Pair<T> p = new Pair<T>(ref, pred);
			result.add(p);
		}
		if (falseNegativePairs) {
			Collection<T> seenRefs = new HashSet<T>();
			for (Pair<T> p : result) {
				if (p.hasReference()) {
					seenRefs.add(p.getReference());
				}
			}
			for (T ref : reference) {
				if (!seenRefs.contains(ref)) {
					Pair<T> p = new Pair<T>(ref, null);
					result.add(p);
				}
			}
		}
		return result;
	}
	
	private T getBest(Similarity<T> similarity, T pred, Collection<T> reference) {
		double bestSim = 0;
		T result = null;
		for (T ref : reference) {
			double s = similarity.compute(ref, pred);
			if (s > threshold && (result == null || s > bestSim)) {
				bestSim = s;
				result = ref;
			}
		}
		return result;
	}
}
