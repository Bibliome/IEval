package org.bionlpst.app.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.source.ContentAndReferenceSource;
import org.bionlpst.corpus.source.bionlpst.BioNLPSTSource;
import org.bionlpst.corpus.source.bionlpst.InputStreamCollection;
import org.bionlpst.corpus.source.pubtator.PubTatorSource;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public class ContentAndReferenceSourceConverter implements DOMElementConverter<ContentAndReferenceSource> {
	private final InputStreamCollectionConverter corpusSourceConverter;

	public ContentAndReferenceSourceConverter(ClassLoader classLoader) {
		super();
		this.corpusSourceConverter = new InputStreamCollectionConverter(classLoader);
	}

	@Override
	public ContentAndReferenceSource convert(Element element) throws Exception {
		InputStreamCollection inputStreamCollection = corpusSourceConverter.convert(element);
		String format = DOMUtil.getAttribute(element, "format", "bionlp-st");
		switch (format) {
			case "bionlp-st":
				return new BioNLPSTSource(inputStreamCollection);
			case "pubtator":
				return convertPubTatorSource(element, inputStreamCollection);
			default:
				throw new BioNLPSTException("unknown format " + format);
		}
	}
	
	private static PubTatorSource convertPubTatorSource(Element element, InputStreamCollection inputStreamCollection) {
		PubTatorSource result = new PubTatorSource(inputStreamCollection, DOMUtil.getBooleanAttribute(element, "ignore-relations", false));
		if (element.hasAttribute("sections")) {
			String s = element.getAttribute("sections");
			Map<String,String> secMap = convertMap("sections", s, ",", '=');
			for (Map.Entry<String,String> e : secMap.entrySet()) {
				result.addSectionName(e.getKey(), e.getValue());
			}
		}
		if (element.hasAttribute("input-types")) {
			String s = element.getAttribute("input-types");
			for (String t : convertArray(s, ",")) {
				result.addInputType(t);
			}
		}
		if (element.hasAttribute("output-types")) {
			String s = element.getAttribute("output-types");
			for (String t : convertArray(s, ",")) {
				result.addOutputType(t);
			}
		}
		if (element.hasAttribute("normalization-types")) {
			String s = element.getAttribute("normalization-types");
			Map<String,String> normMap = convertMap("normalization-types", s, ",", '=');
			for (Map.Entry<String,String> e : normMap.entrySet()) {
				result.addNormalizationType(e.getKey(), e.getValue());
			}
		}
		if (element.hasAttribute("relation-roles")) {
			String s = element.getAttribute("relation-roles");
			Map<String,String[]> roleMap = convertMapOfArray("relation-roles", s);
			for (Map.Entry<String,String[]> e : roleMap.entrySet()) {
				String type = e.getKey();
				String[] roles = e.getValue();
				if (roles.length != 2) {
					throw new BioNLPSTException("expected 2 roles");
				}
				result.addRelationRoles(type, roles[0], roles[1]);
			}
		}
		return result;
	}
	
	private static Map<String,String> convertMap(String attr, String s, String sep, char qual) {
		Map<String,String> result = new LinkedHashMap<String,String>();
		for (String e : s.split(sep)) {
			int eq = e.indexOf(qual);
			if (eq == -1) {
				throw new BioNLPSTException("missing '"+qual+"' in " + attr + " (" + e + ")");
			}
			result.put(e.substring(0, eq).trim(), e.substring(eq+1).trim());
		}
		return result;
	}
	
	private static String[] convertArray(String s, String sep) {
		String[] result = s.split(sep);
		for (int i = 0; i < result.length; ++i) {
			result[i] = result[i].trim();
		}
		return result;
	}
	
	private static Map<String,String[]> convertMapOfArray(String attr, String s) {
		Map<String,String[]> result = new LinkedHashMap<String,String[]>();
		for (String e : s.split(",")) {
			int eq = e.indexOf('=');
			if (eq == -1) {
				throw new BioNLPSTException("missing '=' in " + attr + " (" + e + ")");
			}
			result.put(e.substring(0, eq).trim(), convertArray(e.substring(eq+1).trim(), "/"));
		}
		return result;
	}
}
