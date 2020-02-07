package org.bionlpst.corpus.source.pubannotation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationKind;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.Relation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.corpus.source.PredictionSource;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.fragment.ImmutableFragment;
import org.bionlpst.util.message.CheckLogger;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class PubAnnotationSource implements PredictionSource {
	private final InputStreamFactory inputStreamFactory;
	private final Location location;
	private final Random idGenerator = new Random();
	
	public PubAnnotationSource(InputStreamFactory inputStreamFactory) {
		super();
		this.inputStreamFactory = inputStreamFactory;
		this.location = new Location(getName(), -1);
	}

	@Override
	public String getName() {
		return inputStreamFactory.getName();
	}

	@Override
	public void fillPredictions(CheckLogger logger, Corpus corpus) throws BioNLPSTException, IOException {
		JSONArray jsDocs = getJSONArrayDocs(logger);
		for (int i = 0; i < jsDocs.length(); ++i) {
			JSONObject jsDoc = jsDocs.optJSONObject(i);
			if (jsDoc == null) {
				logger.serious(location, "expected document annotations as JSON object, skipping");
				continue;
			}
			loadAnnotations(logger, corpus, jsDoc);
		}
	}
	
	private JSONArray getJSONArrayDocs(CheckLogger logger) throws IOException {
		try {
			String js = getJSONString();
			return new JSONArray(js);
		}
		catch (JSONException e) {
			logger.serious(location, "could not parse JSON array, aborting");
			return new JSONArray();
		}
	}

	private String getJSONString() throws IOException {
		try (InputStream is = inputStreamFactory.getInputStream()) {
			return Util.readWholeStream(is);
		}
	}
	
	private void loadAnnotations(CheckLogger logger, Corpus corpus, JSONObject jsDoc) {
		if (!jsDoc.has("id")) {
			logger.serious(location, "document object has no id");
			return;
		}
		String id = jsDoc.optString("id", null);
		if (id == null) {
			logger.serious(location, "expected document id as String, skipping");
			return;
		}
		if (!corpus.hasDocument(id)) {
			logger.serious(location, "unknown document " + id);
			return;
		}
		Document doc = corpus.getDocument(id);
		AnnotationSet aset = AnnotationSetSelector.PREDICTION.getAnnotationSet(doc);
		getPredictions(logger, aset, jsDoc);
	}

	private void getPredictions(CheckLogger logger, AnnotationSet aset, JSONObject jsDoc) {
		convertDenotations(logger, aset, jsDoc);
		convertRelations(logger, aset, jsDoc);
		convertNormalizations(logger, aset, jsDoc);
	}

	private static String getAnnotationKindPrefix(AnnotationKind kind) {
		switch (kind) {
		case DUMMY: return "D";
		case MODIFIER: return "M";
		case NORMALIZATION: return "N";
		case RELATION: return "R";
		case TEXT_BOUND: return "T";
		}
		throw new RuntimeException();
	}
	
	private static String getAnnotationKindName(AnnotationKind kind) {
		switch (kind) {
		case DUMMY: return "dummy";
		case MODIFIER: return "modification";
		case NORMALIZATION: return "normalization";
		case RELATION: return "relation";
		case TEXT_BOUND: return "denotation";
		}
		throw new RuntimeException();
	}
	
	private String generateId(AnnotationKind kind) {
		long nid = idGenerator.nextLong();
		return getAnnotationKindPrefix(kind) + "gen" + nid;
	}
	
	private String getId(CheckLogger logger, AnnotationKind kind, JSONObject annotation) {
		if (!annotation.has("id")) {
			logger.suspicious(location, getAnnotationKindName(kind) + " has no id, assigning one");
			return generateId(kind);
		}
		String id = annotation.optString("id", null);
		if (id == null) {
			logger.suspicious(location, "expected id as String for " + getAnnotationKindName(kind) + ", assigning one");
			return generateId(kind);
		}
		return id;
	}
	
	private void convertDenotations(CheckLogger logger, AnnotationSet aset, JSONObject jsDoc) {
		if (!jsDoc.has("denotations")) {
			return;
		}
		JSONArray denotations = jsDoc.optJSONArray("denotations");
		if (denotations == null) {
			logger.serious(location, "expected denotations as JSON array, skipping");
			return;
		}
		for (int i = 0; i < denotations.length(); ++i) {
			JSONObject denotation = denotations.optJSONObject(i);
			if (denotation == null) {
				logger.serious(location, "expected denotation as JSON object, skipping");
				continue;
			}
			convertDenotation(logger, aset, denotation);
		}
	}

	private void convertDenotation(CheckLogger logger, AnnotationSet aset, JSONObject denotation) {
		String id = getId(logger, AnnotationKind.TEXT_BOUND, denotation);
		String type = denotation.optString("obj", null);
		if (type == null) {
			logger.suspicious(location, "denotation has no type, skipping");
			return;
		}
		switch (id.charAt(0)) {
			case 'T': {
				if (!denotation.has("span")) {
					logger.serious(location, "denotation is missing span, ignoring denotation");
					return;
				}
				Object spans = denotation.opt("span");
				List<ImmutableFragment> fragments = convertSpans(logger, spans);
				new TextBound(logger, aset, location, id, type, fragments);
				break;
			}
			case 'E': {
				Map<String,String> emptyArgs = Collections.emptyMap();
				new Relation(logger, aset, location, id, type, emptyArgs);
				break;
			}
			default: {
				logger.serious(location, "denotation (" + id + ") is supposed to have an id that starts with a 'T' of a 'E'");
			}
		}
	}

	private List<ImmutableFragment> convertSpans(CheckLogger logger, Object spans) {
		if (spans instanceof JSONObject) {
			ImmutableFragment frag = convertSpan(logger, (JSONObject) spans);
			return Collections.singletonList(frag);
		}
		if (spans instanceof JSONArray) {
			JSONArray spansArray = (JSONArray) spans;
			List<ImmutableFragment> result = new ArrayList<ImmutableFragment>(spansArray.length());
			for (int i = 0; i < spansArray.length(); ++i) {
				JSONObject span = spansArray.optJSONObject(i);
				if (span == null) {
					logger.serious(location, "expected span as JSON object, ignoring");
					continue;
				}
				ImmutableFragment frag = convertSpan(logger, span);
				if (frag != null) {
					result.add(frag);
				}
			}
			return result;
		}
		logger.serious(location, "expected span as JSON object or array");
		return Collections.emptyList();
	}

	private ImmutableFragment convertSpan(CheckLogger logger, JSONObject span) {
		int begin = convertOffset(logger, span, "begin");
		int end = convertOffset(logger, span, "end");
		if ((begin == -1) || (end == -1)) {
			return null;
		}
		return new ImmutableFragment(begin, end);
	}
	
	private int convertOffset(CheckLogger logger, JSONObject span, String prop) {
		if (!span.has(prop)) {
			logger.serious(location, "fragment lacks " + prop + ", ignoring fragment");
			return -1;
		}
		int result = span.optInt(prop, -1);
		if (result == -1) {
			logger.serious(location, "expected fragment " + prop + " as integer, ignoring fragment");
		}
		return result;
	}
	
	private void convertRelations(CheckLogger logger, AnnotationSet aset, JSONObject jsDoc) {
		if (!jsDoc.has("relations")) {
			return;
		}
		JSONArray relations = jsDoc.optJSONArray("relations");
		if (relations == null) {
			logger.serious(location, "expected relations as JSON array, skipping");
			return;
		}
		for (int i = 0; i < relations.length(); ++i) {
			JSONObject relation = relations.optJSONObject(i);
			if (relation == null) {
				logger.serious(location, "expected relation as JSON object, skipping");
				continue;
			}
			convertRelation(logger, aset, relation);
		}
	}

	private void convertRelation(CheckLogger logger, AnnotationSet aset, JSONObject relation) {
		String id = getId(logger, AnnotationKind.RELATION, relation);
		String type = relation.optString("pred", null);
		if (type == null) {
			logger.suspicious(location, "relation has no type, skipping");
			return;
		}
		String subj = relation.optString("subj");
		if (subj == null) {
			logger.suspicious(location, "relation has no subject, skipping");
			return;
		}
		String obj = relation.optString("obj");
		if (obj == null) {
			logger.suspicious(location, "relation has no object, skipping");
			return;
		}
		switch (subj.charAt(0)) {
			case 'T': {
				Map<String,String> args = new LinkedHashMap<String,String>();
				args.put("subj", subj);
				args.put("obj", obj);
				new Relation(logger, aset, location, id, type, args);
				break;
			}
			case 'E': {
				if (!aset.hasAnnotation(subj)) {
					logger.serious(location, "event not found: " + subj + ", skipping");
					return;
				}
				Annotation a = aset.getAnnotation(subj);
				Relation rel = a.asRelation();
				if (rel == null) {
					logger.serious(location, subj + " is supposed to be an event");
					return;
				}
				rel.setArgumentReference(logger, location, type, obj);
				break;
			}
			default: {
				logger.suspicious(location, "subject of relation " + id + " (" + subj + ") is supposed to have an id that starts with a 'T' of a 'E'");
				break;
			}
		}
	}
	
	private void convertNormalizations(CheckLogger logger, AnnotationSet aset, JSONObject jsDoc) {
		if (!jsDoc.has("attributes")) {
			return;
		}
		JSONArray normalizations = jsDoc.optJSONArray("attributes");
		if (normalizations == null) {
			logger.serious(location, "expected attributes/normalizations as JSON array, skipping");
			return;
		}
		for (int i = 0; i < normalizations.length(); ++i) {
			JSONObject normalization = normalizations.optJSONObject(i);
			if (normalization == null) {
				logger.serious(location, "expected normalization as JSON object, skipping");
				continue;
			}
			convertNormalization(logger, aset, normalization);
		}
	}
	
	private void convertNormalization(CheckLogger logger, AnnotationSet aset, JSONObject normalization) {
		String id = getId(logger, AnnotationKind.NORMALIZATION, normalization);
		String type = normalization.optString("pred", null);
		if (type == null) {
			logger.suspicious(location, "normalization has no type, skipping");
			return;
		}
		String annotation = normalization.optString("subj");
		if (annotation == null) {
			logger.suspicious(location, "relation has no annotation (subj), skipping");
			return;
		}
		String ref = normalization.optString("obj");
		if (ref == null) {
			logger.suspicious(location, "relation has no referent (obj), skipping");
			return;
		}
		new Normalization(logger, aset, location, id, type, annotation, ref);
	}
}
