package org.bionlpst.app.web.json;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.evaluation.ScoringResult;
import org.codehaus.jettison.json.JSONObject;

public enum ScoringResultJsonConverter implements JsonConverter<ScoringResult<Annotation>> {
	INSTANCE;

	@Override
	public JSONObject convert(ScoringResult<Annotation> scoringResult) throws Exception {
		JSONObject result = new JSONObject();
		result.put("name", scoringResult.getScoring().getName());
		result.put("measures", ListJsonConverter.convert(MeasureResultJsonConverter.INSTANCE, scoringResult.getMeasureResults()));
		return result;
	}
}
