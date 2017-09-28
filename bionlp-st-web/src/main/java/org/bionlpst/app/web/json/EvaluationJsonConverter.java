package org.bionlpst.app.web.json;

import org.bionlpst.evaluation.AnnotationEvaluation;
import org.codehaus.jettison.json.JSONObject;

public enum EvaluationJsonConverter implements JsonConverter<AnnotationEvaluation> {
	INSTANCE;

	@Override
	public JSONObject convert(AnnotationEvaluation eval) throws Exception {
		JSONObject result = new JSONObject();
		result.put("name", eval.getName());
		result.put("scorings", ListJsonConverter.convert(ScoringJsonConverter.INSTANCE, eval.getScorings()));
		return result;
	}
}
