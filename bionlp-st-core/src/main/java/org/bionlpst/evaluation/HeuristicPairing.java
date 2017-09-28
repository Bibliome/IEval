package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.bionlpst.evaluation.similarity.Similarity;

/**
 * Heuristic for finding optimal pairs.
 * Because coding the Hungarian algorithm is a pain.
 * @author rbossy
 *
 * @param <T>
 */
public class HeuristicPairing<T> implements PairingAlgorithm<T> {
	@SuppressWarnings("serial")
	private static class Matches<T> extends TreeMap<Double,Collection<T>> {
		private void add(T value, double score) {
			Collection<T> values;
			if (containsKey(score)) {
				values = get(score);
			}
			else {
				values = new HashSet<T>();
				put(score, values);
			}
			values.add(value);
		}
		
		private Collection<T> bestValues() {
			return lastEntry().getValue();
		}
		
		private void removeAllValues(Collection<T> values) {
			Iterator<Collection<T>> it = values().iterator();
			while (it.hasNext()) {
				Collection<T> currentValues = it.next();
				currentValues.removeAll(values);
				if (currentValues.isEmpty()) {
					it.remove();
				}
			}
		}
	}
	
	@SuppressWarnings("serial")
	private static class MatchMap<T> extends HashMap<T,Matches<T>> {
		private MatchMap(Collection<T> keys) {
			for (T k : keys) {
				put(k, new Matches<T>());
			}
		}
		
		private void add(T key, T value, double score) {
			get(key).add(value, score);
		}
		
		private void removeAllValues(Collection<T> values) {
			for (Matches<T> m : values()) {
				m.removeAllValues(values);
			}
		}
		
		private T reciprocal(T value, Matches<T> matches) {
			for (T candidate : matches.bestValues()) {
				if (containsKey(candidate)) {
					Matches<T> backMatches = get(candidate);
					Collection<T> bestBackMatches = backMatches.bestValues();
					if (bestBackMatches.contains(value)) {
						return candidate;
					}
				}
			}
			return null;
		}
	}
	
	private static <T> void addResult(Collection<Pair<T>> result, T ref, T pred, Collection<T> refSeen, Collection<T> predSeen) {
		result.add(new Pair<T>(ref, pred));
		if (ref != null) {
			refSeen.add(ref);
		}
		if (pred != null) {
			predSeen.add(pred);
		}
	}

	@Override
	public List<Pair<T>> bestPairing(Collection<T> reference, Collection<T> prediction, Similarity<T> similarity) {
		List<Pair<T>> result = new ArrayList<Pair<T>>();
		
		// initialize score matrix
		MatchMap<T> ref2pred = new MatchMap<T>(reference);
		MatchMap<T> pred2ref = new MatchMap<T>(prediction);
		for (T ref : reference) {
			for (T pred : prediction) {
				double s = similarity.compute(ref, pred);
				if (s > 0) {
					ref2pred.add(ref, pred, s);
					pred2ref.add(pred, ref, s);
				}
			}
		}
		
		// iterations of reciprocal best matches AND remove empties
		boolean cont = true;
		while (cont) {
			Collection<T> refSeen = new HashSet<T>();
			Collection<T> predSeen = new HashSet<T>();
			for (Map.Entry<T,Matches<T>> e : ref2pred.entrySet()) {
				T ref = e.getKey();
				Matches<T> predMatches = e.getValue();
				if (predMatches.isEmpty()) {
					addResult(result, ref, null, refSeen, predSeen);
					continue;
				}
				T pred = pred2ref.reciprocal(ref, predMatches);
				if (pred != null) {
					addResult(result, ref, pred, refSeen, predSeen);
					break;
				}
			}
			for (Map.Entry<T,Matches<T>> e : pred2ref.entrySet()) {
				T pred = e.getKey();
				Matches<T> refMatches = e.getValue();
				if (refMatches.isEmpty()) {
					addResult(result, null, pred, refSeen, predSeen);
				}
			}
			ref2pred.keySet().removeAll(refSeen);
			ref2pred.removeAllValues(predSeen);
			pred2ref.keySet().removeAll(predSeen);
			pred2ref.removeAllValues(refSeen);
			
			cont = !(refSeen.isEmpty() && predSeen.isEmpty());
//			System.err.println("cont = " + cont);
		}
		
		if (!ref2pred.isEmpty()) {
			throw new RuntimeException();
		}
		if (!pred2ref.isEmpty()) {
			throw new RuntimeException();
		}
		
		return result;
	}
}
