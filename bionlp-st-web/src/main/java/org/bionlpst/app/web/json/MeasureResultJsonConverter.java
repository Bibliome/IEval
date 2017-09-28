package org.bionlpst.app.web.json;

import org.bionlpst.evaluation.MeasureResult;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public enum MeasureResultJsonConverter implements JsonConverter<MeasureResult> {
	INSTANCE;
	
	public final double confidenceIntervalProbability = 0.8;

	@Override
	public JSONObject convert(MeasureResult measureResult) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("name", measureResult.getMeasure().getName());
		Number value = measureResult.getResult();
		result.put("value", NumberJsonConverter.INSTANCE.convert(value));
		MeasureResult.ConfidenceInterval confInterval = measureResult.getConfidenceInterval(confidenceIntervalProbability);
		if (confInterval != null) {
			JSONObject ci = new JSONObject();
			ci.put("min", confInterval.lo);
			ci.put("max", confInterval.hi);
			result.put("interval", ci);
		}
		return result;
	}
}
