package org.bionlpst.evaluation.similarity;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.SingleReferenceAnnotation;

public class SingleReferenceSimilarity implements Similarity<Annotation> {
	private final Similarity<Annotation> similarity;

	public SingleReferenceSimilarity(Similarity<Annotation> similarity) {
		super();
		this.similarity = similarity;
	}

	@Override
	public double compute(Annotation a, Annotation b) {
		SingleReferenceAnnotation sra = a.asSingleReferenceAnnotation();
		SingleReferenceAnnotation srb = b.asSingleReferenceAnnotation();
		if (sra == null || srb == null) {
			return 0;
		}
		return similarity.compute(sra.getAnnotation(), srb.getAnnotation());
	}

	@Override
	public void explain(StringBuilder sb, Annotation a, Annotation b) {
		SingleReferenceAnnotation sra = a.asSingleReferenceAnnotation();
		SingleReferenceAnnotation srb = b.asSingleReferenceAnnotation();
		if (sra == null || srb == null) {
			sb.append("No Annotation");
			return;
		}
		sb.append("Annotation: ");
		similarity.explain(sb, sra, srb);
	}
}
