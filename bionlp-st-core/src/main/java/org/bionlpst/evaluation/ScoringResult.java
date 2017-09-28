package org.bionlpst.evaluation;

import java.util.Collections;
import java.util.List;

public class ScoringResult<T> {
	private final Scoring<T> scoring;
	private final List<MeasureResult> measureResults;
	
	ScoringResult(Scoring<T> scoring, List<MeasureResult> measureResults) {
		super();
		this.scoring = scoring;
		this.measureResults = measureResults;
	}

	public Scoring<T> getScoring() {
		return scoring;
	}

	public List<MeasureResult> getMeasureResults() {
		return Collections.unmodifiableList(measureResults);
	}
}
