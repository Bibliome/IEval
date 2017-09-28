package org.bionlpst.app.web.json;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.evaluation.Pair;
import org.bionlpst.evaluation.similarity.Similarity;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class PairJsonConverter implements JsonConverter<Pair<Annotation>> {
	private final Similarity<Annotation> similarity;

	public PairJsonConverter(Similarity<Annotation> similarity) {
		super();
		this.similarity = similarity;
	}

	@Override
	public JSONObject convert(Pair<Annotation> pair) throws JSONException {
		JSONObject result = new JSONObject();
		if (pair.hasReference()) {
			result.put("reference", new AnnotationJsonConverter("reference").convert(pair.getReference()));
		}
		if (pair.hasPrediction()) {
			result.put("prediction", new AnnotationJsonConverter("prediction").convert(pair.getPrediction()));
		}
		if (pair.hasBoth()) {
			result.put("similarity", pair.compute(similarity));
			result.put("explain-similarity", pair.explain(similarity));
		}
		return result;
	}
	
	
}
