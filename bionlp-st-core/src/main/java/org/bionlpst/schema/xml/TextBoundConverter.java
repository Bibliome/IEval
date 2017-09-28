package org.bionlpst.schema.xml;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.schema.CompositeSchema;
import org.bionlpst.schema.Schema;
import org.bionlpst.schema.lib.BackReferenceCardinalitySchema;
import org.bionlpst.schema.lib.FragmentCardinalityTextBoundSchema;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public enum TextBoundConverter implements DOMElementConverter<Schema<TextBound>> {
	INSTANCE;
	
	@Override
	public Schema<TextBound> convert(Element element) throws Exception {
		CompositeSchema<TextBound> result = new CompositeSchema<TextBound>();
		
		for (Element child : DOMUtil.getChildrenElements(element)) {
			String tag = child.getTagName();
			switch (tag) {
				case "custom": {
					@SuppressWarnings("unchecked")
					Schema<TextBound> schema = DOMUtil.getContentsByClassName(child, Schema.class);
					result.addCompound(schema);
					break;
				}
				case "backreference-cardinality": {
					String type = child.getTextContent().trim();
					int atLeast = DOMUtil.getIntAttribute(child, "at-least", 0);
					int atMost = DOMUtil.getIntAttribute(child, "at-most", Integer.MAX_VALUE);
					Schema<TextBound> schema = new BackReferenceCardinalitySchema<TextBound>(type, atLeast, atMost);
					result.addCompound(schema);
					break;
				}
				case "fragment-cardinality": {
					int maxFragments = DOMUtil.getIntContents(child);
					Schema<TextBound> schema = new FragmentCardinalityTextBoundSchema(maxFragments);
					result.addCompound(schema);
					break;
				}
				default: {
					throw new BioNLPSTException("unexpected tag: " + tag);
				}
			}
		}
		return result;
	}
}
