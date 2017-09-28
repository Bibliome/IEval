package org.bionlpst.app.web.json;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.evaluation.Scoring;
import org.codehaus.jettison.json.JSONObject;

public enum ScoringJsonConverter implements JsonConverter<Scoring<Annotation>> {
	INSTANCE;

	@Override
	public JSONObject convert(Scoring<Annotation> scoring) throws Exception {
		JSONObject result = new JSONObject();
		result.put("name", scoring.getName());
		result.put("measures", ListJsonConverter.convert(MeasureJsonConverter.INSTANCE, scoring.getMeasures()));
		return result;
	}
}
