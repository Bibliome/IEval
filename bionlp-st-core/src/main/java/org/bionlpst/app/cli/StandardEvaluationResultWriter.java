package org.bionlpst.app.cli;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.TextBoundCollector;
import org.bionlpst.evaluation.EvaluationResult;
import org.bionlpst.evaluation.MeasureResult;
import org.bionlpst.evaluation.MeasureResult.ConfidenceInterval;
import org.bionlpst.evaluation.Pair;
import org.bionlpst.evaluation.ScoringResult;
import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.util.Named;
import org.bionlpst.util.fragment.ImmutableFragment;

public enum StandardEvaluationResultWriter implements EvaluationResultWriter {
	INSTANCE;

	@Override
	public void displayEvaluationResult(EvaluationResult<Annotation> eval, boolean detailedEvaluation, double confidence) {
		System.out.printf("  %s\n", eval.getEvaluation().getName());
		if (detailedEvaluation) {
			displayPairing(eval);
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
		
	@Override
	public void displayCorpusHeader(Named named, String defaultName) {
		System.out.println("Evaluation for corpus " + (named == null ? defaultName : named.getName()));
	}

	@Override
	public void displayDocumentHeader(Document doc) {
		System.out.println("Evaluation for document " + doc.getId());
	}
	
	private static void displayPairing(EvaluationResult<Annotation> eval) {
		System.out.println("    Pairing");
		Similarity<Annotation> sim = eval.getEvaluation().getMatchingSimilarity();
		for (Pair<Annotation> pair : eval.getPairs()) {
			System.out.print("      ");
			System.out.print(getPairType(sim, pair));
			displayAnnotation(pair.getReference(), AnnotationSetSelector.REFERENCE, 20);
			displayAnnotation(pair.getPrediction(), AnnotationSetSelector.PREDICTION, 20);
			System.out.println();
		}
	}

	private static String getPairType(Similarity<Annotation> sim, Pair<Annotation> pair) {
		if (pair.hasBoth()) {
			double s = sim.compute(pair.getReference(), pair.getPrediction());
			if (s == 1.0) {
				return "TP";
			}
			return String.format("MM:%.4f", s);
		}
		if (pair.hasReference()) {
			return "FN";
		}
		return "FP";
	}
	
	private static void displayAnnotation(Annotation ann, AnnotationSetSelector sel, int wsz) {
		if (ann == null) {
			System.out.print("\t\t\t\t");
			return;
		}
		ImmutableFragment frag = TextBoundCollector.INSTANCE.getGlobalFragment(ann);
		String text = ann.getDocument().getContents().replace('\n', ' ');
		String before = text.substring(Math.max(0, frag.getStart() - wsz), frag.getStart());
		String in = text.substring(frag.getStart(), frag.getEnd());
		String after = text.substring(frag.getEnd(), Math.min(text.length(), frag.getEnd() + wsz));
		System.out.print('\t');
		System.out.print(ann.getId());
		System.out.print('\t');
		System.out.print(before);
		System.out.print('\t');
		System.out.print(in);
		System.out.print('\t');
		System.out.print(after);
		System.out.print('\t');
		boolean notFirst = false;
		for (Normalization norm : ann.getNormalizationBackReferences()) {
			if (!norm.getAnnotationSet().getSelector().equals(sel)) {
				continue;
			}
			if (notFirst) {
				System.out.print(", ");
			}
			else {
				notFirst = true;
			}
			String ref = norm.getReferent();
			String label = norm.getLabel();
			if (label == null) {
				System.out.print(ref);
			}
			else {
				System.out.format("%s (%s)", ref, label);
			}
		}
	}
}
