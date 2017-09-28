package org.bionlpst.app.web.json;

import org.bionlpst.app.Task;
import org.codehaus.jettison.json.JSONObject;

public enum TaskJsonConverter implements JsonConverter<Task> {
	INSTANCE;

	@Override
	public JSONObject convert(Task task) throws Exception {
		JSONObject result = new JSONObject();
		result.put("name", task.getName());
		result.put("description", task.getDescription());
		result.put("test-check", task.hasTest());
		result.put("test-evaluate", task.isTestHasReferenceAnnotations());
		result.put("evaluations", ListJsonConverter.convert(EvaluationJsonConverter.INSTANCE, task.getEvaluations()));
		return result;
	}
}
