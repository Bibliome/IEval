package org.bionlpst.app.cli;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.TextBoundCollector;
import org.bionlpst.evaluation.EvaluationResult;
import org.bionlpst.evaluation.Pair;
import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.util.Named;
import org.bionlpst.util.fragment.ImmutableFragment;

public enum EvaluationPairingWriter implements EvaluationResultWriter {
	INSTANCE;

	@Override
	public void displayEvaluationResult(EvaluationResult<Annotation> eval, boolean detailedEvaluation, double confidence) {
		if (detailedEvaluation) {
			Similarity<Annotation> sim = eval.getEvaluation().getMatchingSimilarity();
			for (Pair<Annotation> pair : eval.getPairs()) {
				System.out.print(getDocumentId(pair));
				System.out.format("\t%s\t%.4f", getPairType(sim, pair), sim.compute(pair.getReference(), pair.getPrediction()));
				displayAnnotation(null, pair.getReference(), AnnotationSetSelector.REFERENCE, 20);
				displayAnnotation(pair.getReference(), pair.getPrediction(), AnnotationSetSelector.PREDICTION, 20);
				System.out.println();
			}
		}
	}
	
	private static String getDocumentId(Pair<Annotation> pair) {
		Annotation ann = getSomeAnnotation(pair);
		return ann.getDocument().getId();
	}
	
	private static Annotation getSomeAnnotation(Pair<Annotation> pair) {
		if (pair.hasReference()) {
			return pair.getReference();
		}
		return pair.getPrediction();
	}

	private static String getPairType(Similarity<Annotation> sim, Pair<Annotation> pair) {
		if (pair.hasBoth()) {
			double s = sim.compute(pair.getReference(), pair.getPrediction());
			if (s == 1.0) {
				return "TP";
			}
			return String.format("MM", s);
		}
		if (pair.hasReference()) {
			return "FN";
		}
		return "FP";
	}
	
	private static void displayAnnotation(Annotation ex, Annotation ann, AnnotationSetSelector sel, int wsz) {
		if (ann == null) {
			System.out.print("\t\t\t\t\t\t");
			return;
		}
		if (ex != ann) {
			ImmutableFragment frag = TextBoundCollector.INSTANCE.getGlobalFragment(ann);
			String text = ann.getDocument().getContents().replace('\n', ' ');
			String before = text.substring(Math.max(0, frag.getStart() - wsz), frag.getStart()).trim();
			String in = text.substring(frag.getStart(), frag.getEnd());
			String after = text.substring(frag.getEnd(), Math.min(text.length(), frag.getEnd() + wsz)).trim();
			System.out.format("\t%s\t%d-%d\t%s\t%s\t%s\t%s", ann.getId(), frag.getStart(), frag.getEnd(), ann.getType(), before, in, after);
			System.out.print('\t');
			System.out.print(ann.getId());
			System.out.print('\t');
			System.out.print(before);
			System.out.print('\t');
			System.out.print(in);
			System.out.print('\t');
			System.out.print(after);
		}
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

	@Override
	public void displayCorpusHeader(Named named, String defaultName) {
	}

	@Override
	public void displayDocumentHeader(Document doc) {
	}
}
