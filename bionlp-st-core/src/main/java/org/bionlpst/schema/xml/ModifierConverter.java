package org.bionlpst.schema.xml;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Modifier;
import org.bionlpst.schema.CompositeSchema;
import org.bionlpst.schema.Schema;
import org.bionlpst.schema.lib.BackReferenceCardinalitySchema;
import org.bionlpst.schema.lib.SingleReferenceAnnotationTypecheckSchema;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public enum ModifierConverter implements DOMElementConverter<Schema<Modifier>> {
	INSTANCE;
	
	@Override
	public Schema<Modifier> convert(Element element) throws Exception {
		CompositeSchema<Modifier> result = new CompositeSchema<Modifier>();
		
		for (Element child : DOMUtil.getChildrenElements(element)) {
			String tag = child.getTagName();
			switch (tag) {
				case "custom": {
					@SuppressWarnings("unchecked")
					Schema<Modifier> schema = DOMUtil.getContentsByClassName(child, Schema.class);
					result.addCompound(schema);
					break;
				}
				case "backreference-cardinality": {
					String type = child.getTextContent().trim();
					int atLeast = DOMUtil.getIntAttribute(child, "at-least", 0);
					int atMost = DOMUtil.getIntAttribute(child, "at-most", Integer.MAX_VALUE);
					Schema<Modifier> schema = new BackReferenceCardinalitySchema<Modifier>(type, atLeast, atMost);
					result.addCompound(schema);
					break;
				}
				case "annotation-types": {
					String[] allowedTypes = DOMUtil.getArrayContents(child);
					Schema<Modifier> schema = new SingleReferenceAnnotationTypecheckSchema<Modifier>(allowedTypes);
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
