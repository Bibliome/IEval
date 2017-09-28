package org.bionlpst.evaluation;

import org.bionlpst.evaluation.similarity.Similarity;

/**
 * A pair.
 * @author rbossy
 *
 * @param <T>
 */
public class Pair<T> {
	private final T reference;
	private final T prediction;
	
	/**
	 * Creates a new pair.
	 * @param reference reference item, null for a false positive.
	 * @param prediction predicted item, null for a false negative.
	 * @throws IllegalArgumentException if both parameters are null.
	 */
	public Pair(T reference, T prediction) throws IllegalArgumentException {
		super();
		if (reference == null && prediction == null) {
			throw new IllegalArgumentException();
		}
		this.reference = reference;
		this.prediction = prediction;
	}

	/**
	 * Returns the reference item.
	 * @return the reference item. null for false positive.
	 */
	public T getReference() {
		return reference;
	}

	/**
	 * Returns the predicted item.
	 * @return the predicted item. null for false negative.
	 */
	public T getPrediction() {
		return prediction;
	}
	
	/**
	 * Returns true if the reference item is not null.
	 * @return true if the reference item is not null.
	 */
	public boolean hasReference() {
		return reference != null;
	}
	
	/**
	 * Returns true if the predicted item is not null.
	 * @return true if the predicted item is not null.
	 */
	public boolean hasPrediction() {
		return prediction != null;
	}
	
	/**
	 * Returns true if both reference and predicted items are not null.
	 * @return true if both reference and predicted items are not null.
	 */
	public boolean hasBoth() {
		return reference != null && prediction != null;
	}
	
	/**
	 * Compute the similarity between the reference and the predicted items.
	 * @param similarity
	 * @return the similarity between the reference and the predicted items.
	 */
	public double compute(Similarity<T> similarity) {
		return similarity.compute(reference, prediction);
	}
	
	public void explain(StringBuilder sb, Similarity<T> similarity) {
		similarity.explain(sb, reference, prediction);
	}
	
	public String explain(Similarity<T> similarity) {
		StringBuilder sb = new StringBuilder();
		explain(sb, similarity);
		return sb.toString();
	}

	@Override
	public String toString() {
		return "(" + reference + ", " + prediction + ")";
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((prediction == null) ? 0 : prediction.hashCode());
		result = prime * result + ((reference == null) ? 0 : reference.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		@SuppressWarnings("rawtypes")
		Pair other = (Pair) obj;
		if (prediction == null) {
			if (other.prediction != null)
				return false;
		}
		else if (!prediction.equals(other.prediction))
			return false;
		if (reference == null) {
			if (other.reference != null)
				return false;
		}
		else if (!reference.equals(other.reference))
			return false;
		return true;
	}
	
	public static enum Selector {
		REFERENCE {
			@Override
			public <T> T get(Pair<T> pair) {
				return pair.reference;
			}

			@Override
			public <T> T other(Pair<T> pair) {
				return pair.prediction;
			}

			@Override
			public <T> boolean has(Pair<T> pair) {
				return pair.reference != null;
			}

			@Override
			public <T> boolean hasOther(Pair<T> pair) {
				return pair.prediction != null;
			}

			@Override
			public String toString() {
				return "reference";
			}
		},
		
		PREDICTION {
			@Override
			public <T> T get(Pair<T> pair) {
				return pair.prediction;
			}

			@Override
			public <T> T other(Pair<T> pair) {
				return pair.reference;
			}

			@Override
			public <T> boolean has(Pair<T> pair) {
				return pair.prediction != null;
			}

			@Override
			public <T> boolean hasOther(Pair<T> pair) {
				return pair.reference != null;
			}

			@Override
			public String toString() {
				return "prediction";
			}
		};
		
		public abstract <T> T get(Pair<T> pair);
		public abstract <T> T other(Pair<T> pair);
		public abstract <T> boolean has(Pair<T> pair);
		public abstract <T> boolean hasOther(Pair<T> pair);
	}
}
