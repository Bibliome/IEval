package org.bionlpst.util.dom;

import java.util.HashMap;
import java.util.Map;

import org.w3c.dom.Document;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

public class DOMAliases {
	private final Map<String,DocumentFragment> aliases = new HashMap<String,DocumentFragment>();

	public DOMAliases() {
		super();
	}

	public void addAlias(String name, DocumentFragment value) {
		if (hasAlias(name)) {
			throw new IllegalArgumentException("duplicate alias name: " + name);
		}
		aliases.put(name, value);
	}
	
	public boolean hasAlias(String name) {
		return aliases.containsKey(name);
	}
	
	public DocumentFragment getAlias(String name) {
		if (hasAlias(name)) {
			return aliases.get(name);
		}
		throw new IllegalArgumentException("unknown alias: " + name);
	}
	
	public Node getAliasClone(String name, Document owner) {
		DocumentFragment value = getAlias(name);
		Node result = value.cloneNode(true);
		owner.adoptNode(result);
		return result;
	}
	
	public boolean replace(Node elt) {
		boolean result = replaceChildren(elt);
		String tagName = elt.getNodeName();
		if (!hasAlias(tagName)) {
			return result;
		}
		NodeList children = elt.getChildNodes();
		if (children.getLength() > 0) {
			return result;
		}
		Document owner = elt.getOwnerDocument();
		Node value = getAliasClone(tagName, owner);
		Node parent = elt.getParentNode();
		parent.replaceChild(value, elt);
		return true;
	}

	private boolean replaceChildren(Node elt) {
		boolean result = false;
		for (Element child : DOMUtil.getChildrenElements(elt, true)) {
			result = replace(child) || result;
		}
		return result;
	}
	
	public void replace(Document doc) {
		Element root = doc.getDocumentElement();
		replace(root);
	}
	
	public void replaceValues() {
		for (DocumentFragment df : aliases.values()) {
			while (replace(df));
		}
	}
}
