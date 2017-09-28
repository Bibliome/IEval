package org.bionlpst.schema.xml;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Relation;
import org.bionlpst.schema.CompositeSchema;
import org.bionlpst.schema.Schema;
import org.bionlpst.schema.lib.AllowedRelationArgumentsSchema;
import org.bionlpst.schema.lib.BackReferenceCardinalitySchema;
import org.bionlpst.schema.lib.MandatoryAlternativeRelationArgumentSchema;
import org.bionlpst.schema.lib.MandatoryRelationArgumentsSchema;
import org.bionlpst.schema.lib.RelationArgumentTypecheckSchema;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public enum RelationConverter implements DOMElementConverter<Schema<Relation>> {
	INSTANCE;
	
	@Override
	public Schema<Relation> convert(Element element) throws Exception {
		CompositeSchema<Relation> result = new CompositeSchema<Relation>();
		
		for (Element child : DOMUtil.getChildrenElements(element)) {
			String tag = child.getTagName();
			switch (tag) {
				case "custom": {
					@SuppressWarnings("unchecked")
					Schema<Relation> schema = DOMUtil.getContentsByClassName(child, Schema.class);
					result.addCompound(schema);
					break;
				}
				case "backreference-cardinality": {
					String type = child.getTextContent().trim();
					int atLeast = DOMUtil.getIntAttribute(child, "at-least", 0);
					int atMost = DOMUtil.getIntAttribute(child, "at-most", Integer.MAX_VALUE);
					Schema<Relation> schema = new BackReferenceCardinalitySchema<Relation>(type, atLeast, atMost);
					result.addCompound(schema);
					break;
				}
				case "roles": {
					String[] allowedRoles = DOMUtil.getArrayContents(child);
					Schema<Relation> schema = new AllowedRelationArgumentsSchema(allowedRoles);
					result.addCompound(schema);
					break;
				}
				case "mandatory-arguments": {
					String[] mandatoryRoles = DOMUtil.getArrayContents(child);
					Schema<Relation> schema = new MandatoryRelationArgumentsSchema(mandatoryRoles);
					result.addCompound(schema);
					break;
				}
				case "mandatory-alternatives": {
					boolean exclusive = DOMUtil.getBooleanAttribute(child, "exclusive");
					String[] alternativeRoles = DOMUtil.getArrayContents(child);
					Schema<Relation> schema = new MandatoryAlternativeRelationArgumentSchema(exclusive, alternativeRoles);
					result.addCompound(schema);
					break;
				}
				case "argument-types": {
					String role = DOMUtil.getMandatoryAttribute(child, "role");
					String[] allowedTypes = DOMUtil.getArrayContents(child);
					Schema<Relation> schema = new RelationArgumentTypecheckSchema(role, allowedTypes);
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
