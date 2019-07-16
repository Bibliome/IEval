package org.bionlpst.util.dom;

import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.util.SourceStream;
import org.bionlpst.util.Util;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.Text;

public enum DOMUtil {
	;
	
	public static List<Element> getChildrenElements(Node elt, boolean allowJunkText) {
		List<Element> result = new ArrayList<Element>();
		NodeList children = elt.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			switch (child.getNodeType()) {
				case Node.ELEMENT_NODE: {
					result.add((Element) child);
					break;
				}
				case Node.TEXT_NODE: {
					if (allowJunkText) {
						break;
					}
					Text txt = (Text) child;
					String s = txt.getWholeText();
					if (!s.trim().isEmpty()) {
						throw new BioNLPSTException("junk text in tag " + elt.getNodeName() + ": " + s);
					}
					break;
				}
			}
		}
		return result;
	}
	
	public static Element getFirstChildElement(Element element) {
		NodeList children = element.getChildNodes();
		for (int i = 0; i < children.getLength(); ++i) {
			Node child = children.item(i);
			switch (child.getNodeType()) {
				case Node.ELEMENT_NODE: {
					return (Element) child;
				}
			}
		}
		throw new BioNLPSTException("no child in " + element.getTagName());
	}
	
	public static String getAttribute(Element element, String attribute, String defaultValue) {
		if (element.hasAttribute(attribute)) {
			return element.getAttribute(attribute);
		}
		return defaultValue;
	}
	
	public static String getMandatoryAttribute(Element element, String attribute) {
		if (element.hasAttribute(attribute)) {
			return element.getAttribute(attribute);
		}
		throw new BioNLPSTException("expected attribute: " + attribute + " in " + getElementPath(element));
	}
	
	private static String getElementPath(Element element) {
		StringBuilder result = new StringBuilder();
		for (Node e = element; e != null; e = e.getParentNode()) {
			if (e != element) {
				result.append(" < ");
			}
			String name = e.getNodeName();
			result.append(name);
		}
		return result.toString();
	}
	
	private static boolean getBoolean(String s) {
		switch (s) {
			case "true":
			case "yes":
				return true;
			case "false":
			case "no":
				return false;
			default:
				throw new BioNLPSTException("invalid boolean value: " + s);
		}		
	}
	
	public static boolean getBooleanAttribute(Element element, String attribute, boolean defaultValue) {
		if (element.hasAttribute(attribute)) {
			return getBoolean(element.getAttribute(attribute));
		}
		return defaultValue;
	}
	
	public static int getIntAttribute(Element element, String attribute, int defaultValue) {
		if (element.hasAttribute(attribute)) {
			return Integer.parseInt(element.getAttribute(attribute));
		}
		return defaultValue;
	}
	
	public static double getDoubleAttribute(Element element, String attribute, double defaultValue) {
		if (element.hasAttribute(attribute)) {
			return Double.parseDouble(element.getAttribute(attribute));
		}
		return defaultValue;
	}
	
	public static boolean getBooleanAttribute(Element element, String attribute) {
		return getBoolean(getMandatoryAttribute(element, attribute));
	}
	
	public static int getIntAttribute(Element element, String attribute) {
		return Integer.parseInt(getMandatoryAttribute(element, attribute));
	}
	
	public static double getDoubleAttribute(Element element, String attribute) {
		return Double.parseDouble(getMandatoryAttribute(element, attribute));
	}
	
	public static <T> T getContentsByClassName(Element element, Class<T> superClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		String className = element.getTextContent();
		return Util.instantiateAndCast(className, superClass);
	}
	
	public static boolean getBooleanContents(Element element) {
		return getBoolean(element.getTextContent().trim());
	}
	
	public static int getIntContents(Element element) {
		return Integer.parseInt(element.getTextContent());
	}
	
	public static double getDoubleContents(Element element) {
		return Double.parseDouble(element.getTextContent());
	}
	
	public static String[] getArrayContents(Element element) {
		String separator = getAttribute(element, "separator", ",");
		String contents = element.getTextContent();
		String[] result = contents.split(separator);
		for (int i = 0; i < result.length; ++i) {
			result[i] = result[i].trim();
		}
		return result;
	}
	
	public static <T> T convert(DOMElementConverter<T> converter, Document doc) throws Exception {
		DOMAliases aliases = createAliases(doc);
		Element element = doc.getDocumentElement();
		aliases.replace(element);
		return converter.convert(element);
	}
	
	public static void writeDOMToFile(Document doc, File f) throws TransformerException {
		TransformerFactory tf = TransformerFactory.newInstance();
		Transformer t = tf.newTransformer();
		Source source = new DOMSource(doc);
		Result result = new StreamResult(f);
		t.transform(source, result);		
	}
	
	public static DOMAliases createAliases(Document doc) {
		DOMAliases result = new DOMAliases();
		Element element = doc.getDocumentElement();
		for (Element child : getChildrenElements(element, true)) {
			String tagName = child.getTagName();
			if (tagName.equals("aliases")) {
				for (Element alias : getChildrenElements(child, false)) {
					String name = alias.getTagName();
					DocumentFragment value = doc.createDocumentFragment();
					NodeList valueContent = alias.getChildNodes();
					for (int i = 0; i < valueContent.getLength(); ++i) {
						Node n = valueContent.item(i);
						Node n2 = n.cloneNode(true);
						value.appendChild(n2);
					}
					result.addAlias(name, value);
				}
				element.removeChild(child);
			}
		}
		result.replaceValues();
		return result;
	}
	
	public static DocumentBuilder createDocumentBuilder() throws ParserConfigurationException {
		DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
		return docBuilderFactory.newDocumentBuilder();
	}
	
	public static <T> T convert(DOMElementConverter<T> converter, InputStream is) throws Exception {
		DocumentBuilder docBuilder = createDocumentBuilder();
		Document doc = docBuilder.parse(is);
		return convert(converter, doc);
	}
	
	public static <T> T convert(DOMElementConverter<T> converter, File file) throws Exception {
		DocumentBuilder docBuilder = createDocumentBuilder();
		Document doc = docBuilder.parse(file);
		return convert(converter, doc);
	}
	
	public static <T> T convert(DOMElementConverter<T> converter, SourceStream source) throws Exception {
		try (InputStream is = source.open()) {
			return convert(converter, is);
		}
	}
}
