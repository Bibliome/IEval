package org.bionlpst.app.web.json;

import java.io.File;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationVisitor;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.DummyAnnotation;
import org.bionlpst.corpus.Modifier;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.Relation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.util.Location;
import org.bionlpst.util.fragment.Fragment;
import org.codehaus.jettison.json.JSONException;
import org.codehaus.jettison.json.JSONObject;

public class AnnotationJsonConverter implements JsonConverter<Annotation>, AnnotationVisitor<StringBuilder,StringBuilder> {
	private final String fakeDir;
	
	public AnnotationJsonConverter(String fakeDir) {
		super();
		this.fakeDir = fakeDir;
	}

	@Override
	public JSONObject convert(Annotation ann) throws JSONException {
		JSONObject result = new JSONObject();
		Location loc = ann.getLocation();
		result.put("source", getSource(loc));
		result.put("lineno", loc.lineno);
		result.put("id", ann.getId());
		result.put("type", ann.getType());
		result.put("str", ann.accept(this, new StringBuilder()).toString());
		return result;
	}
	
	private String getSource(Location loc) {
		if (fakeDir == null) {
			return loc.getSource();
		}
		File f = new File(loc.getSource());
		return fakeDir + "/" + f.getName();
	}
	
	private void head(Annotation ann, StringBuilder sb) {
		sb.append(": ");
		sb.append(ann.getId());
		sb.append(' ');
		sb.append(ann.getType());
		sb.append(' ');
		ann.accept(this, sb);
	}

	@Override
	public StringBuilder visit(TextBound textBound, StringBuilder param) {
		param.append('[');
		Document doc = textBound.getDocument();
		String contents = doc.getContents();
		boolean first = true;
		for (Fragment frag : textBound.getFragments()) {
			if (first) {
				first = false;
			}
			else {
				param.append(' ');
			}
			param.append(contents.substring(frag.getStart(), frag.getEnd()));
		}
		param.append(']');
		return param;
	}

	@Override
	public StringBuilder visit(Relation relation, StringBuilder param) {
		param.append('{');
		boolean first = true;
		for (String role : relation.getRoles()) {
			if (first) {
				first = false;
			}
			else {
				param.append(", ");
			}
			param.append(role);
			Annotation arg = relation.getArgument(role);
			head(arg, param);
		}
		param.append('}');
		return param;
	}

	@Override
	public StringBuilder visit(Normalization normalization, StringBuilder param) {
		param.append('(');
		param.append(normalization.getReferent());
		head(normalization.getAnnotation(), param);
		param.append(')');
		return param;
	}

	@Override
	public StringBuilder visit(Modifier modifier, StringBuilder param) {
		param.append('<');
		head(modifier.getAnnotation(), param);
		param.append('>');
		return param;
	}

	@Override
	public StringBuilder visit(DummyAnnotation dummy, StringBuilder param) {
		param.append("PARSE ERROR");
		return param;
	}
}
