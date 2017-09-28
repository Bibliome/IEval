package org.bionlpst.evaluation.similarity;

import java.util.HashMap;
import java.util.Map;

import org.bionlpst.corpus.Annotation;

/**
 * If both annotations have the same type, delegates to a similarity. Otherwise yields 0.
 * @author rbossy
 *
 * @param <T>
 */
public class AnnotationTypeDispatchSimilarity<T extends Annotation> implements Similarity<T> {
	private final Map<String,Similarity<T>> similarities = new HashMap<String,Similarity<T>>();
	private final Similarity<T> defaultSimilarity;
	private final Similarity<T> differentSimilarity;

	public AnnotationTypeDispatchSimilarity(Similarity<T> defaultSimilarity, Similarity<T> differentSimilarity) {
		super();
		this.defaultSimilarity = defaultSimilarity;
		this.differentSimilarity = differentSimilarity;
	}

	public void addSimilarity(String type, Similarity<T> similarity) {
		if (similarities.containsKey(type)) {
			throw new RuntimeException();
		}
		similarities.put(type, similarity);
	}

	@Override
	public double compute(T a, T b) {
		String tA = a.getType();
		if (tA.equals(b.getType())) {
			if (similarities.containsKey(tA)) {
				Similarity<T> sim = similarities.get(tA);
				return sim.compute(a, b);
			}
			return defaultSimilarity.compute(a, b);
		}
		return differentSimilarity.compute(a, b);
	}

	@Override
	public void explain(StringBuilder sb, T a, T b) {
		String tA = a.getType();
		String tB = b.getType();
		sb.append("Type ");
		sb.append(tA);
		if (tA.equals(tB)) {
			sb.append(": ");
			if (similarities.containsKey(tA)) {
				Similarity<T> sim = similarities.get(tA);
				sim.explain(sb, a, b);
			}
			else {
				defaultSimilarity.explain(sb, a, b);
			}
			return;
		}
		sb.append('/');
		sb.append(tB);
		sb.append(": ");
		differentSimilarity.explain(sb, a, b);
	}
}
