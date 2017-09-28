package org.bionlpst.evaluation;

import java.util.Collection;
import java.util.List;

import org.bionlpst.evaluation.similarity.Similarity;

/**
 * Algorithm for finding the optimal pairing.
 * @author rbossy
 *
 * @param <T>
 */
public interface PairingAlgorithm<T> {
	/**
	 * Returns the optimal pairing (that maximize the sum of the specified similarity).
	 * @param reference reference items.
	 * @param prediction predicted items.
	 * @param similarity similarity to maximize.
	 * @return the optimal pairing.
	 */
	List<Pair<T>> bestPairing(Collection<T> reference, Collection<T> prediction, Similarity<T> similarity);
}
