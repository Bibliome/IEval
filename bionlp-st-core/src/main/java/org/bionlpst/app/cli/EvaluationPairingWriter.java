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
				System.out.format("\t%s\t%.4f", getPairType(sim, pair), similarityCompute(sim, pair));
				displayAnnotation(null, pair.getReference(), AnnotationSetSelector.REFERENCE, 20);
				displayAnnotation(pair.getReference(), pair.getPrediction(), AnnotationSetSelector.PREDICTION, 20);
				System.out.println();
			}
		}
	}
	
	private static <T> double similarityCompute(Similarity<T> sim, Pair<T> pair) {
		T ref = pair.getReference();
		T pred = pair.getPrediction();
		if ((ref == null) || (pred == null)) {
			return 0.0;
		}
		return sim.compute(ref, pred);
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
			if (Math.abs(s-1.0) <= 0.000001) {
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
			System.out.print("\t\t\t\t\t\t\t");
			return;
		}
		if (ex != ann) {
			ImmutableFragment frag = TextBoundCollector.INSTANCE.getGlobalFragment(ann);
			String text = ann.getDocument().getContents().replace('\n', ' ');
			String before = text.substring(Math.max(0, frag.getStart() - wsz), frag.getStart()).trim();
			String in = text.substring(frag.getStart(), frag.getEnd());
			String after = text.substring(frag.getEnd(), Math.min(text.length(), frag.getEnd() + wsz)).trim();
			System.out.format("\t%s\t%d-%d\t%s\t%s\t%s\t%s", ann.getId(), frag.getStart(), frag.getEnd(), ann.getType(), before, in, after);
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
		System.out.print("doc_id\tpair_type\tsim\tref_id\tref_off\tref_type\tref_before\tref_form\tref_after\tref_norm\tpred_id\tpred_off\tpred_type\tpred_before\tpred_form\tpred_after\tpred_norm\n");
	}

	@Override
	public void displayDocumentHeader(Document doc) {
	}
}
