package org.bionlpst.evaluation.similarity;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

public abstract class CompositeSimilarity<T> implements Similarity<T> {
	private final Collection<Similarity<T>> similarities = new ArrayList<Similarity<T>>();

	protected CompositeSimilarity() {
		super();
	}

	protected CompositeSimilarity(Collection<Similarity<T>> similarities) {
		this();
		this.similarities.addAll(similarities);
	}

	public void addSimilarity(Similarity<T> similarity) {
		similarities.add(similarity);
	}
	
	public Collection<Similarity<T>> getSimilarities() {
		return Collections.unmodifiableCollection(similarities);
	}
	
	protected void explainSimilarities(StringBuilder sb, T a, T b, String sep) {
		boolean first = true;
		for (Similarity<T> sim : similarities) {
			if (first) {
				first = false;
			}
			else {
				sb.append(sep);
			}
			sim.explain(sb, a, b);
		}
	}
}
