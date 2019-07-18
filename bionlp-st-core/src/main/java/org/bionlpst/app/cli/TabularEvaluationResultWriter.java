package org.bionlpst.app.cli;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Document;
import org.bionlpst.evaluation.EvaluationResult;
import org.bionlpst.evaluation.MeasureResult;
import org.bionlpst.evaluation.MeasureResult.ConfidenceInterval;
import org.bionlpst.evaluation.ScoringResult;
import org.bionlpst.util.Named;

public class TabularEvaluationResultWriter implements EvaluationResultWriter {
	private String target;

	@Override
	public void displayEvaluationResult(EvaluationResult<Annotation> eval, boolean detailedEvaluation, double confidence) {
		String evalName = eval.getEvaluation().getName();
		if (detailedEvaluation) {
		}
		for (ScoringResult<Annotation> scoring : eval.getScoringResults()) {
			String scoringName = scoring.getScoring().getName();
			for (MeasureResult measure : scoring.getMeasureResults()) {
				String measureName = measure.getMeasure().getName();
				System.out.printf("%s\t%s\t%s\t%s\t%s", target, evalName, scoringName, measureName, measure.getResult());
				if (confidence > 0.0) {
					ConfidenceInterval inter = measure.getConfidenceInterval(confidence);
					System.out.printf("\t%s\t%s", inter.lo, inter.hi);
				}
				System.out.println();
			}
		}
	}

	@Override
	public void displayCorpusHeader(Named named, String defaultName) {
		if (named == null) {
			target = defaultName;
		}
		else {
			target = named.getName();
		}
	}

	@Override
	public void displayDocumentHeader(Document doc) {
		target = doc.getId();
	}

}
