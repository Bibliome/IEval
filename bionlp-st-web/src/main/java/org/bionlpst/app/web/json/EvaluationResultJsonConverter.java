package org.bionlpst.app.web.json;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.evaluation.EvaluationResult;
import org.codehaus.jettison.json.JSONObject;

public class EvaluationResultJsonConverter implements JsonConverter<EvaluationResult<Annotation>> {
	private final boolean pairs;

	public EvaluationResultJsonConverter(boolean pairs) {
		super();
		this.pairs = pairs;
	}

	@Override
	public JSONObject convert(EvaluationResult<Annotation> evaluationResult) throws Exception {
		JSONObject result = new JSONObject();
		result.put("name", evaluationResult.getEvaluation().getName());
		result.put("scorings", ListJsonConverter.convert(ScoringResultJsonConverter.INSTANCE, evaluationResult.getScoringResults()));
		if (pairs) {
			PairJsonConverter pairConverter = new PairJsonConverter(evaluationResult.getEvaluation().getMatchingSimilarity());
			result.put("pairs", ListJsonConverter.convert(pairConverter, evaluationResult.getPairs()));
		}
		return result;
	}
}
