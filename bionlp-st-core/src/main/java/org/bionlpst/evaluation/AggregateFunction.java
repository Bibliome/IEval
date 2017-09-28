package org.bionlpst.evaluation;

import java.util.Arrays;
import java.util.List;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.evaluation.similarity.Similarity;

public enum AggregateFunction {
	MEAN {
		@Override
		public <T> double doCompute(Similarity<T> similarity, T reference, List<T> predictions) {
			double total = 0;
			for (T pred : predictions) {
				total += similarity.compute(reference, pred);
			}
			return total / predictions.size();
		}

		@Override
		public String toString() {
			return "mean";
		}
	},
	
	MEDIAN {
		@Override
		public <T> double doCompute(Similarity<T> similarity, T reference, List<T> predictions) {
			double[] sim = new double[predictions.size()];
			for (int i = 0; i < sim.length; ++i) {
				sim[i] = similarity.compute(reference, predictions.get(i));
			}
			Arrays.sort(sim);
			return sim[sim.length / 2];
		}

		@Override
		public String toString() {
			return "median";
		}
	},
	
	MIN {
		@Override
		public <T> double doCompute(Similarity<T> similarity, T reference, List<T> predictions) {
			double result = Double.MAX_VALUE;
			for (T pred : predictions) {
				result = Math.min(result, similarity.compute(reference, pred));
			}
			return result;
		}

		@Override
		public String toString() {
			return "min";
		}
	},
	
	MAX {
		@Override
		public <T> double doCompute(Similarity<T> similarity, T reference, List<T> predictions) {
			double result = 0;
			for (T pred : predictions) {
				result = Math.max(result, similarity.compute(reference, pred));
			}
			return result;
		}

		@Override
		public String toString() {
			return "max";
		}
	}
	;
	
	public abstract <T> double doCompute(Similarity<T> similarity, T reference, List<T> predictions);
	
	public <T> double compute(Similarity<T> similarity, T reference, List<T> predictions) {
		if (predictions.isEmpty()) {
			return 0;
		}
		return doCompute(similarity, reference, predictions);
	}
	
	public static AggregateFunction get(String value) {
		switch (value) {
			case "mean": return MEAN;
			case "median": return MEDIAN;
			case "min": return MIN;
			case "max": return MAX;
			default: throw new BioNLPSTException("unknown aggregate function: " + value);
		}
	}
}
