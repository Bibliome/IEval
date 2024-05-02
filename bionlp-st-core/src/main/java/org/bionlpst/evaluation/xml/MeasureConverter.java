package org.bionlpst.evaluation.xml;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.evaluation.AggregateFunction;
import org.bionlpst.evaluation.AggregateMeasure;
import org.bionlpst.evaluation.Measure;
import org.bionlpst.evaluation.Pair;
import org.bionlpst.evaluation.StandardMeasures;
import org.bionlpst.util.dom.DOMElementConverter;
import org.w3c.dom.Element;

public enum MeasureConverter implements DOMElementConverter<Measure> {
	INSTANCE {
		@Override
		public Measure convert(Element element) throws Exception {
			String s = element.getTextContent();
			return getMeasure(s);
		}
	};
	
	public static Measure getMeasure(String s) {
		switch (s.trim().toLowerCase()) {
			case "deletions":
			case "deletion":
			case "dels":
			case "del":
				return StandardMeasures.DELETIONS;
			case "f1-score":
			case "f1":
			case "f-score":
			case "f":
				 return StandardMeasures.F1_SCORE;
			case "false-negatives":
			case "false-negative":
			case "fn":
				return StandardMeasures.FALSE_NEGATIVES;
			case "false-positives":
			case "false-positive":
			case "fp":
				return StandardMeasures.FALSE_POSITIVES;
			case "insertions":
			case "inserts":
			case "insertion":
			case "insert":
			case "ins":
				return StandardMeasures.INSERTIONS;
			case "matches":
			case "match":
				return StandardMeasures.MATCHES;
			case "mismatches":
			case "substitutions":
			case "subs":
			case "mismatch":
			case "substitution":
			case "sub":
				return StandardMeasures.MISMATCHES;
			case "precision":
			case "prec":
				return StandardMeasures.PRECISION;
			case "predictions":
			case "preds":
			case "prediction":
			case "pred":
				return StandardMeasures.PREDICTIONS;
			case "recall":
			case "rec":
				return StandardMeasures.RECALL;
			case "references":
			case "refs":
			case "reference":
			case "ref":
				return StandardMeasures.REFERENCES;
			case "slot-error-rate":
			case "ser":
				return StandardMeasures.SLOT_ERROR_RATE;
			case "inverted-slot-error-rate":
			case "iser":
				return StandardMeasures.INVERTED_SLOT_ERROR_RATE;
			case "match-accuracy":
				return StandardMeasures.MATCH_ACCURACY;
			default: {
				int dash = s.indexOf('-');
				if (dash == -1) {
					throw new BioNLPSTException("unknown measure: " + s);
				}
				AggregateFunction aggregateFunction = AggregateFunction.get(s.substring(0, dash));
				Pair.Selector pairSelector = null;
				switch (s.substring(dash+1)) {
					case "predictions": pairSelector = Pair.Selector.PREDICTION; break;
					case "references": pairSelector = Pair.Selector.REFERENCE; break;
					default: throw new BioNLPSTException("unknown measure: " + s);
				}
				return new AggregateMeasure(aggregateFunction, pairSelector);
			}
		}
	}
}
