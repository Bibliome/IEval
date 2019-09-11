package org.bionlpst.corpus.writer;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationKind;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.Relation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.util.fragment.Fragment;
import org.bionlpst.util.fragment.ImmutableFragment;
import org.codehaus.jettison.json.JSONArray;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public enum PubAnnotationWriter {
	;
	
	public static void write(Corpus corpus, String sourcedb) throws IOException, JSONException {
		JSONArray json = toJSON(corpus, sourcedb);
		try (Writer w = new OutputStreamWriter(System.out)) {
			json.write(w);
		}
	}

	private static JSONArray toJSON(Corpus corpus, String sourcedb) throws JSONException {
		JSONArray result = new JSONArray();
		for (Document doc : corpus.getDocuments()) {
			JSONObject j = toJSON(doc, sourcedb);
			result.put(j);
		}
		return result;
	}
	
	private static JSONObject toJSON(Document doc, String sourcedb) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("sourcedb", sourcedb);
		result.put("sourceid", doc.getId());
		result.put("denotations", new JSONArray());
		result.put("relations", new JSONArray());
		result.put("text", doc.getContents());
		toJSON(doc.getInputAnnotationSet(), result);
		toJSON(doc.getReferenceAnnotationSet(), result);
		return result;
	}
	
	private static void toJSON(AnnotationSet aset, JSONObject json) throws JSONException {
		JSONArray denotations = json.getJSONArray("denotations");
		for (Annotation ann : aset.getAnnotations(AnnotationKind.TEXT_BOUND)) {
			TextBound tb = ann.asTextBound();
			JSONObject j = toJSON(tb);
			denotations.put(j);
		}
		JSONArray relations = json.getJSONArray("relations");
		for (Annotation ann : aset.getAnnotations(AnnotationKind.RELATION)) {
			Relation rel = ann.asRelation();
			JSONObject j = toJSON(rel);
			relations.put(j);
		}
	}
	
	private static JSONObject createAnnotationJSON(Annotation ann, String typeKey) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("id", ann.getId());
		result.put(typeKey, ann.getType());
		return result;
	}
	
	private static JSONObject toJSON(Fragment frag) throws JSONException {
		JSONObject result = new JSONObject();
		result.put("begin", frag.getStart());
		result.put("end", frag.getEnd());
		return result;
	}
	
	private static JSONObject toJSON(TextBound textBound) throws JSONException {
		JSONObject result = createAnnotationJSON(textBound, "obj");
		result.put("obj", textBound.getType());
		List<ImmutableFragment> frags = textBound.getFragments();
		if (frags.size() == 1) {
			result.put("span", toJSON(frags.get(0)));
		}
		else {
			JSONArray spans = new JSONArray();
			result.put("span", spans);
			for (Fragment frag : frags) {
				spans.put(frag);
			}
		}
		return result;
	}
	
	private static JSONObject toJSON(Relation rel) throws JSONException {
		Collection<Annotation> args = rel.getArguments();
		if (args.size() != 2) {
			throw new BioNLPSTException("only convert binary relations");
		}
		JSONObject result = createAnnotationJSON(rel, "pred");
		Iterator<Annotation> it = args.iterator();
		result.put("subj", it.next().getId());
		result.put("obj", it.next().getId());
		return result;
	}
}
