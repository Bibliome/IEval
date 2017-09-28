package org.bionlpst.app.web.json;

import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public enum NumberJsonConverter implements JsonConverter<Number> {
	INSTANCE;

	@Override
	public Object convert(Number number) throws JSONException {
		double d = number.doubleValue();
		if (Double.isNaN(d)) {
			return JSONObject.NULL;
		}
		if (Double.isInfinite(d)) {
			return JSONObject.NULL;
		}
		return number;
	}
	
	
}
