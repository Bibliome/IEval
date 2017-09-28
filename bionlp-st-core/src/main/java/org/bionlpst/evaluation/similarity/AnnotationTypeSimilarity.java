package org.bionlpst.evaluation.similarity;

import java.util.ArrayList;
import java.util.Collection;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.evaluation.Pair;
import org.bionlpst.util.Util;

/**
 * Yields 1 if both annotations have the same type, otherwise the value provided in the constructor.
 * @author rbossy
 *
 * @param <T>
 */
public class AnnotationTypeSimilarity<T extends Annotation> implements Similarity<T> {
	private final Similarity<String> typeSimilarity;

	public AnnotationTypeSimilarity(Similarity<String> typeSimilarity) {
		super();
		this.typeSimilarity = typeSimilarity;
	}
	
	public AnnotationTypeSimilarity(double differentTypeValue) {
		this(new ConstantSimilarity<String>(differentTypeValue));
	}
	
	/**
	 * Yields 0 if types are different.
	 */
	public AnnotationTypeSimilarity() {
		this(0);
	}

	@Override
	public double compute(T a, T b) {
		String tA = a.getType();
		String tB = b.getType();
		if (tA.equals(tB)) {
			return 1;
		}
		return typeSimilarity.compute(tA, tB);
	}
	
	private static class TypePairValue extends Pair<String> {
		private final double value;

		private TypePairValue(String reference, String prediction, double value) throws IllegalArgumentException {
			super(Util.notnull(reference), Util.notnull(prediction));
			this.value = value;
		}
		
		private boolean match(String a, String b) {
			return getReference().equals(a) && getPrediction().equals(b);
		}
	}
	
	public static class TypeTable implements Similarity<String> {
		private final Collection<TypePairValue> values = new ArrayList<TypePairValue>();

		public TypeTable() {
			super();
		}
		
		public void addTypePairValue(String reference, String prediction, double value) {
			values.add(new TypePairValue(reference, prediction, value));
		}

		@Override
		public double compute(String a, String b) {
			for (TypePairValue tpv : values) {
				if (tpv.match(a, b)) {
					return tpv.value;
				}
			}
			return 0;
		}

		@Override
		public void explain(StringBuilder sb, String a, String b) {
			sb.append(a);
			sb.append('/');
			sb.append(b);
			sb.append(" = ");
			sb.append(compute(a, b));
		}
	}

	@Override
	public void explain(StringBuilder sb, T a, T b) {
		sb.append("Type: ");
		String tA = a.getType();
		String tB = b.getType();
		if (tA.equals(tB)) {
			sb.append(" = 1");
			return;
		}
		typeSimilarity.explain(sb, a.getType(), b.getType());
	}
}
