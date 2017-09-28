package org.bionlpst.evaluation.similarity;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Relation;

public class RelationArgumentSimilarity implements Similarity<Annotation> {
	private final String role;
	private final Similarity<Annotation> similarity;
	
	public RelationArgumentSimilarity(String role, Similarity<Annotation> similarity) {
		super();
		this.role = role;
		this.similarity = similarity;
	}

	@Override
	public double compute(Annotation a, Annotation b) {
		Relation ra = a.asRelation();
		Relation rb = b.asRelation();
		if (ra == null || rb == null) {
			return 0;
		}
		if (!ra.hasArgument(role) || !rb.hasArgument(role)) {
			return 0;
		}
		return similarity.compute(ra.getArgument(role), rb.getArgument(role));
	}

	@Override
	public void explain(StringBuilder sb, Annotation a, Annotation b) {
		Relation ra = a.asRelation();
		Relation rb = b.asRelation();
		if (ra == null || rb == null) {
			sb.append("No Arguments");
			return;
		}
		if (!ra.hasArgument(role) || !rb.hasArgument(role)) {
			sb.append("No Argument ");
			sb.append(role);
			return;
		}
		sb.append("Argument ");
		sb.append(role);
		sb.append(": ");
		similarity.explain(sb, ra.getArgument(role), rb.getArgument(role));
	}
}
