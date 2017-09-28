package org.bionlpst.app.web.json;

import org.bionlpst.evaluation.Measure;
import org.bionlpst.evaluation.MeasureDirection;
import org.codehaus.jettison.json.JSONObject;

public enum MeasureJsonConverter implements JsonConverter<Measure>{
	INSTANCE;

	@Override
	public JSONObject convert(Measure measure) throws Exception {
		JSONObject result = new JSONObject();
		result.put("name", measure.getName());
		result.put("higher", measure.getMeasureDirection() == MeasureDirection.HIGHER_IS_BETTER);
		return result;
	}
}
