package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.bionlpst.evaluation.similarity.Similarity;

/**
 * An evaluation object computes pairings and scorings.
 * @author rbossy
 *
 * @param <T>
 */
public class Evaluation<T> {
	private final String name;
	private final PairingAlgorithm<T> pairingAlgorithm;
	private final Similarity<T> matchingSimilarity;
	private final List<Scoring<T>> scorings = new ArrayList<Scoring<T>>();

	public Evaluation(String name, PairingAlgorithm<T> pairingAlgorithm, Similarity<T> matchingSimilarity) {
		super();
		this.name = name;
		this.pairingAlgorithm = pairingAlgorithm;
		this.matchingSimilarity = matchingSimilarity;
	}

	public Evaluation(String name, PairingAlgorithm<T> pairingAlgorithm, Similarity<T> matchingSimilarity, Collection<Scoring<T>> scorings) {
		this(name, pairingAlgorithm, matchingSimilarity);
		this.scorings.addAll(scorings);
	}
	
	public String getName() {
		return name;
	}

	public PairingAlgorithm<T> getPairingAlgorithm() {
		return pairingAlgorithm;
	}
	
	public Similarity<T> getMatchingSimilarity() {
		return matchingSimilarity;
	}

	public List<Scoring<T>> getScorings() {
		return Collections.unmodifiableList(scorings);
	}
	
	public void addScoring(Scoring<T> scoring) {
		scorings.add(scoring);
	}
	
	public List<Pair<T>> getPairs(Collection<T> reference, Collection<T> prediction) {
		return pairingAlgorithm.bestPairing(reference, prediction, matchingSimilarity);
	}
	
	public EvaluationResult<T> getResult(List<Pair<T>> pairs, boolean keepPairs, BootstrapConfig bootstrap) {
		List<ScoringResult<T>> scoringResults = new ArrayList<ScoringResult<T>>(scorings.size());
		for (Scoring<T> score : scorings) {
			scoringResults.add(score.getResult(pairs, bootstrap));
		}
		return new EvaluationResult<T>(this, keepPairs ? pairs : null, scoringResults);
	}
	
	public EvaluationResult<T> getMainResult(List<Pair<T>> pairs, boolean keepPairs, BootstrapConfig bootstrap) {
		Scoring<T> mainScoring = scorings.get(0);
		ScoringResult<T> mainScoringResult = mainScoring.getResult(pairs, bootstrap);
		List<ScoringResult<T>> list = Collections.singletonList(mainScoringResult);
		return new EvaluationResult<T>(this, keepPairs ? pairs : null, list);
	}

	public EvaluationResult<T> getResults(Collection<T> reference, Collection<T> prediction, boolean keepPairs, BootstrapConfig bootstrap) {
		List<Pair<T>> pairs = getPairs(reference, prediction);
		return getResult(pairs, keepPairs, bootstrap);
	}

	public EvaluationResult<T> getMainResult(Collection<T> reference, Collection<T> prediction, boolean keepPairs, BootstrapConfig bootstrap) {
		List<Pair<T>> pairs = getPairs(reference, prediction);
		return getMainResult(pairs, keepPairs, bootstrap);
	}
}
