package org.bionlpst.corpus.source.pubannotation;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.AnnotationKind;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
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
	private final Location location = new Location(getName(), -1);
	private final Random idGenerator = new Random();
	
	public PubAnnotationSource(InputStreamFactory inputStreamFactory) {
		super();
		this.inputStreamFactory = inputStreamFactory;
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
			getPredictions(logger, corpus, jsDoc);
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
	
	private void getPredictions(CheckLogger logger, Corpus corpus, JSONObject jsDoc) {
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

	private void convertDenotation(CheckLogger logger, AnnotationSet aset, JSONObject denotation) {
		String id = getId(logger, AnnotationKind.TEXT_BOUND, denotation);
		String type = denotation.optString("obj", null);
		if (type == null) {
			logger.suspicious(location, "denotation has no type, skipping");
			return;
		}
		if (!denotation.has("span")) {
			logger.serious(location, "denotation is missing span, ignoring denotation");
			return;
		}
		Object spans = denotation.opt("span");
		List<ImmutableFragment> fragments = convertSpans(logger, spans);
		new TextBound(logger, aset, location, id, type, fragments);
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
}
