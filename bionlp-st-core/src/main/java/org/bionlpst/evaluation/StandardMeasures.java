package org.bionlpst.evaluation;

import java.util.Arrays;
import java.util.Collection;

import org.bionlpst.evaluation.similarity.Similarity;

public enum StandardMeasures implements Measure {
	/**
	 * Counts the number of reference items.
	 */
	REFERENCES {
		@Override
		public <T> Long compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			long result = 0;
			for (Pair<T> p : pairs) {
				if (p.hasReference()) {
					result++;
				}
			}
			return result;
		}

		@Override
		public String getName() {
			return "References";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.NO_DIRECTION;
		}
	},
	
	/**
	 * Counts the number of predicted items.
	 */
	PREDICTIONS {
		@Override
		public <T> Long compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			long result = 0;
			for (Pair<T> p : pairs) {
				if (p.hasPrediction()) {
					result++;
				}
			}
			return result;
		}

		@Override
		public String getName() {
			return "Predictions";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.NO_DIRECTION;
		}
	},

	/**
	 * Sums the similarity of matching pairs.
	 */
	MATCHES {
		@Override
		public <T> Double compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			double result = 0;
			for (Pair<T> p : pairs) {
				if (p.hasBoth()) {
					double s = p.compute(similarity);
					result += s;
				}
			}
			return result;
		}

		@Override
		public String getName() {
			return "Matches";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.HIGHER_IS_BETTER;
		}
	},

	/**
	 * Sums the (1 - similarity) of pairs.
	 */
	MISMATCHES {
		@Override
		public <T> Double compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			double result = 0;
			for (Pair<T> p : pairs) {
				if (p.hasBoth()) {
					result += 1 - p.compute(similarity);
				}
			}
			return result;
		}

		@Override
		public String getName() {
			return "Mismatches";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.LOWER_IS_BETTER;
		}
		
	},

	/**
	 * Counts false positives.
	 */
	FALSE_POSITIVES {
		@Override
		public <T> Long compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			long result = 0;
			for (Pair<T> p : pairs) {
				if (!p.hasReference()) {
					result++;
				}
			}
			return result;
		}

		@Override
		public String getName() {
			return "False Positives";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.LOWER_IS_BETTER;
		}
		
	},
	
	/**
	 * Counts false negatives.
	 */
	FALSE_NEGATIVES {
		@Override
		public <T> Long compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			long result = 0;
			for (Pair<T> p : pairs) {
				if (!p.hasPrediction()) {
					result++;
				}
			}
			return result;
		}

		@Override
		public String getName() {
			return "False Negatives";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.LOWER_IS_BETTER;
		}
		
	},
	
	/**
	 * Same as FALSE_POSITIVES.
	 */
	INSERTIONS {
		@Override
		public <T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			return FALSE_POSITIVES.compute(similarity, pairs);
		}

		@Override
		public String getName() {
			return "Insertions";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.LOWER_IS_BETTER;
		}
	},
	
	/**
	 * Same as FALSE_NEGATIVES.
	 */
	DELETIONS {
		@Override
		public <T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			return FALSE_NEGATIVES.compute(similarity, pairs);
		}

		@Override
		public String getName() {
			return "Deletions";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.LOWER_IS_BETTER;
		}
	},
	
	/**
	 * Same as MISMATCHES.
	 */
	SUBSTITUTIONS {
		@Override
		public <T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			return MISMATCHES.compute(similarity, pairs);
		}

		@Override
		public String getName() {
			return "Substitutions";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.LOWER_IS_BETTER;
		}
	},
	
	/**
	 * Computes recall.
	 */
	RECALL {
		@Override
		public <T> Double compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			double matches = MATCHES.compute(similarity, pairs).doubleValue();
			long references = REFERENCES.compute(similarity, pairs).longValue();
			return matches / references;
		}

		@Override
		public String getName() {
			return "Recall";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.HIGHER_IS_BETTER;
		}
	},

	/**
	 * Computes precision.
	 */
	PRECISION {
		@Override
		public <T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			double matches = MATCHES.compute(similarity, pairs).doubleValue();
			long predictions = PREDICTIONS.compute(similarity, pairs).longValue();
			return matches / predictions;
		}

		@Override
		public String getName() {
			return "Precision";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.HIGHER_IS_BETTER;
		}
	},

	MATCH_ACCURACY {
		@Override
		public <T> Double compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			double matches = MATCHES.compute(similarity, pairs).doubleValue();
			int both = 0;
			for (Pair<T> p : pairs) {
				if (p.hasBoth()) {
					both++;
				}
			}
			return matches / both;
		}

		@Override
		public String getName() {
			return "Match accuracy";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.HIGHER_IS_BETTER;
		}
	},

	/**
	 * Computes SER.
	 */
	SLOT_ERROR_RATE {
		@Override
		public <T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			double substitutions = SUBSTITUTIONS.compute(similarity, pairs).longValue();
			long insertions = INSERTIONS.compute(similarity, pairs).longValue();
			long deletions = DELETIONS.compute(similarity, pairs).longValue();
			long references = REFERENCES.compute(similarity, pairs).longValue();
			return (substitutions + insertions + deletions) / references;
		}

		@Override
		public String getName() {
			return "SER";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.LOWER_IS_BETTER;
		}
	},
	
	/**
	 * Computes SER.
	 */
	INVERTED_SLOT_ERROR_RATE {
		@Override
		public <T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			double ser = SLOT_ERROR_RATE.compute(similarity, pairs).doubleValue();
			return 1.0 / (ser + 1);
		}

		@Override
		public String getName() {
			return "ISER";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.HIGHER_IS_BETTER;
		}
	},

	/**
	 * Computes F1 score.
	 */
	F1_SCORE {
		@Override
		public <T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
			double recall = RECALL.compute(similarity, pairs).doubleValue();
			double precision = PRECISION.compute(similarity, pairs).doubleValue();
			return 2 * ((recall * precision) / (recall + precision));
		}

		@Override
		public String getName() {
			return "F1";
		}

		@Override
		public MeasureDirection getMeasureDirection() {
			return MeasureDirection.HIGHER_IS_BETTER;
		}
	};
	
	public static Collection<? extends Measure> getF1Measures() {
		return Arrays.asList(
				F1_SCORE,
				RECALL,
				PRECISION
				);
	}
	
	public static Collection<? extends Measure> getSERMeasures() {
		return Arrays.asList(
				SLOT_ERROR_RATE,
				INVERTED_SLOT_ERROR_RATE,
				MISMATCHES,
				MATCHES,
				INSERTIONS,
				DELETIONS
				);
	}
	
	public static Collection<? extends Measure> getCountMeasures() {
		return Arrays.asList(
				PREDICTIONS,
				REFERENCES
				);
	}
}
