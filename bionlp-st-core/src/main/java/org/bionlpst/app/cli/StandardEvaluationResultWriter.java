package org.bionlpst.app.cli;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Document;
import org.bionlpst.evaluation.EvaluationResult;
import org.bionlpst.evaluation.MeasureResult;
import org.bionlpst.evaluation.Pair;
import org.bionlpst.evaluation.ScoringResult;
import org.bionlpst.evaluation.MeasureResult.ConfidenceInterval;
import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.util.Named;

public enum StandardEvaluationResultWriter implements EvaluationResultWriter {
	INSTANCE;

	@Override
	public void displayEvaluationResult(EvaluationResult<Annotation> eval, boolean detailedEvaluation, double confidence) {
		System.out.printf("  %s\n", eval.getEvaluation().getName());
		if (detailedEvaluation) {
			System.out.println("    Pairing");
			Similarity<Annotation> sim = eval.getEvaluation().getMatchingSimilarity();
			for (Pair<Annotation> pair : eval.getPairs()) {
				Annotation ref = pair.getReference();
				Annotation pred = pair.getPrediction();
				System.out.printf("      %-4s %-4s %.4f\n", getAnnotationId(ref), getAnnotationId(pred), pair.hasBoth() ? sim.compute(ref, pred) : 0);
			}
		}
		for (ScoringResult<Annotation> scoring : eval.getScoringResults()) {
			System.out.printf("    %s\n", scoring.getScoring().getName());
			for (MeasureResult measure : scoring.getMeasureResults()) {
				System.out.printf("      %s: %s", measure.getMeasure().getName(), measure.getResult());
				if (confidence > 0.0) {
					ConfidenceInterval inter = measure.getConfidenceInterval(confidence);
					System.out.printf(" (%s-%s)", inter.lo, inter.hi);
				}
				System.out.println();
			}
		}
	}
	
	private static String getAnnotationId(Annotation ann) {
		if (ann == null) {
			return "--";
		}
		return ann.getId();
	}
	
	@Override
	public void displayCorpusHeader(Named named, String defaultName) {
		System.out.println("Evaluation for corpus " + (named == null ? defaultName : named.getName()));
	}

	@Override
	public void displayDocumentHeader(Document doc) {
		System.out.println("Evaluation for document " + doc.getId());
	}
}
