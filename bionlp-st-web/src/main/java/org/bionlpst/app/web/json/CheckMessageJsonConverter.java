package org.bionlpst.app.web.json;

import org.bionlpst.util.Location;
import org.bionlpst.util.message.CheckMessage;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public enum CheckMessageJsonConverter implements JsonConverter<CheckMessage> {
	INSTANCE;

	@Override
	public Object convert(CheckMessage msg) throws JSONException {
		JSONObject result = new JSONObject();
		Location loc = msg.getLocation();
		result.put("source", loc.getSource());
		result.put("lineno", loc.getLineno());
		result.put("level", msg.getLevel());
		result.put("body", msg.getBody());
		return result;
	}
}
