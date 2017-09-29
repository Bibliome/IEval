package org.bionlpst.util.dom;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMAliases {
	private final Map<String,Element> aliases = new HashMap<String,Element>();
	private final String aliasNameAttribute;
	private final String replaceAliasAttribute;
	private final String appendAliasAttribute;
	private final String replaceAliasChildrenAttribute;
	private final String appendAliasChildrenAttribute;

	public DOMAliases(String aliasNameAttribute, String replaceAliasAttribute, String appendAliasAttribute, String replaceAliasChildrenAttribute, String appendAliasChildrenAttribute) {
		super();
		this.aliasNameAttribute = aliasNameAttribute;
		this.replaceAliasAttribute = replaceAliasAttribute;
		this.appendAliasAttribute = appendAliasAttribute;
		this.replaceAliasChildrenAttribute = replaceAliasChildrenAttribute;
		this.appendAliasChildrenAttribute = appendAliasChildrenAttribute;
	}

	public void addAlias(String name, Element value) {
		if (hasAlias(name)) {
			throw new IllegalArgumentException("duplicate alias name: " + name);
		}
		aliases.put(name, value);
	}
	
	public boolean hasAlias(String name) {
		return aliases.containsKey(name);
	}
	
	public Element getAlias(String name) {
		if (hasAlias(name)) {
			return aliases.get(name);
		}
		throw new IllegalArgumentException("unknown alias: " + name);
	}
	
	public Node getAliasClone(String name, Document owner) {
		Element value = getAlias(name);
		Node result = value.cloneNode(true);
		owner.adoptNode(result);
		return result;
	}
	
	public void addRecursiveAliases(Element elt) {
		if (elt.hasAttribute(aliasNameAttribute)) {
			String name = elt.getAttribute(aliasNameAttribute);
			addAlias(name, elt);
		}
		addRecursiveChildrenAliases(elt);
	}

	private void addRecursiveChildrenAliases(Element elt) {
		for (Element child : DOMUtil.getChildrenElements(elt, true)) {
			addRecursiveAliases(child);
		}
	}

	public void addRecursiveAliases(Document doc) {
		addRecursiveAliases(doc.getDocumentElement());
	}
	
	public void replace(Element elt) {
		replaceChildren(elt);
		if (elt.hasAttribute(replaceAliasAttribute)) {
			String name = elt.getAttribute(replaceAliasAttribute);
			Document doc = elt.getOwnerDocument();
			Node repl = getAliasClone(name, doc);
			Node parent = elt.getParentNode();
			parent.replaceChild(repl, elt);
		}
		if (elt.hasAttribute(appendAliasAttribute)) {
			Document doc = elt.getOwnerDocument();
			String names = elt.getAttribute(appendAliasAttribute);
			for (String name : names.split("\\s+")) {
				Node repl = getAliasClone(name, doc);
				elt.appendChild(repl);
			}
		}
		if (elt.hasAttribute(replaceAliasChildrenAttribute)) {
			Document doc = elt.getOwnerDocument();
			Node parent = elt.getParentNode();
			String name = elt.getAttribute(replaceAliasChildrenAttribute);
			Node repl = getAliasClone(name, doc);
			NodeList children = repl.getChildNodes();
			for (int i = 0; i < children.getLength(); ++i) {
				Node child = children.item(i);
				parent.insertBefore(child, elt);
			}
			parent.removeChild(elt);
		}
		if (elt.hasAttribute(appendAliasChildrenAttribute)) {
			Document doc = elt.getOwnerDocument();
			String names = elt.getAttribute(appendAliasChildrenAttribute);
			for (String name : names.split("\\s+")) {
				Node repl = getAliasClone(name, doc);
				NodeList children = repl.getChildNodes();
				for (int i = 0; i < children.getLength(); ++i) {
					Node child = children.item(i);
					elt.appendChild(child);
				}
			}
		}
	}

	private void replaceChildren(Element elt) {
		for (Element child : DOMUtil.getChildrenElements(elt, true)) {
			replace(child);
		}
	}
	
	public void replace(Document doc) {
		Element root = doc.getDocumentElement();
		replace(root);
	}
}
