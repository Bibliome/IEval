package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class EvaluationResult<T> {
	private final Evaluation<T> evaluation;
	private final List<Pair<T>> pairs;
	private final List<ScoringResult<T>> scoringResults;
	
	EvaluationResult(Evaluation<T> evaluation, List<Pair<T>> pairs, List<ScoringResult<T>> scoringResults) {
		super();
		this.evaluation = evaluation;
		this.pairs = pairs;
		this.scoringResults = scoringResults;
	}
	
	public EvaluationResult(Evaluation<T> evaluation) {
		this(evaluation, new ArrayList<Pair<T>>(), new ArrayList<ScoringResult<T>>());
	}

	public Evaluation<T> getEvaluation() {
		return evaluation;
	}

	public List<Pair<T>> getPairs() {
		return Collections.unmodifiableList(pairs);
	}

	public List<ScoringResult<T>> getScoringResults() {
		return Collections.unmodifiableList(scoringResults);
	}
}
