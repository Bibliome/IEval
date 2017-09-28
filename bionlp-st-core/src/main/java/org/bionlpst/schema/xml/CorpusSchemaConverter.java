package org.bionlpst.schema.xml;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.Modifier;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.Relation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.schema.CompositeSchema;
import org.bionlpst.schema.Schema;
import org.bionlpst.schema.lib.AnnotationSchemaTypeDispatch;
import org.bionlpst.schema.lib.BasicCorpusSchema;
import org.bionlpst.schema.lib.BasicDocumentSchema;
import org.bionlpst.util.Util;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public class CorpusSchemaConverter implements DOMElementConverter<Schema<Corpus>> {
	private final ClassLoader classLoader;
	
	public CorpusSchemaConverter(ClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public Schema<Corpus> convert(Element element) throws Exception {
		AnnotationSchemaTypeDispatch typeDispatch = new AnnotationSchemaTypeDispatch();
		CompositeSchema<Annotation> annSchema = new CompositeSchema<Annotation>();
		CompositeSchema<Document> docSchema = new CompositeSchema<Document>();
		CompositeSchema<Corpus> result = new CompositeSchema<Corpus>();
		annSchema.addCompound(typeDispatch);
		docSchema.addCompound(new BasicDocumentSchema(annSchema));
		result.addCompound(new BasicCorpusSchema(docSchema));
		
		for (Element child : DOMUtil.getChildrenElements(element)) {
			String tag = child.getTagName();
			switch (tag) {
				case "custom-corpus": {
					String className = child.getTextContent();
					@SuppressWarnings("unchecked")
					Schema<Corpus> schema = Util.instantiateAndCast(className, Schema.class);
					result.addCompound(schema);
					break;
				}
				case "custom-document": {
					String className = child.getTextContent();
					@SuppressWarnings("unchecked")
					Schema<Document> schema = Util.instantiateAndCast(className, Schema.class);
					docSchema.addCompound(schema);
					break;
				}
				case "custom-annotation": {
					String className = child.getTextContent();
					@SuppressWarnings("unchecked")
					Schema<Annotation> schema = Util.instantiateAndCast(className, Schema.class);
					annSchema.addCompound(schema);
					break;
				}
				case "text-bound": {
					String type = DOMUtil.getMandatoryAttribute(child, "type");
					Schema<TextBound> schema = TextBoundConverter.INSTANCE.convert(child);
					typeDispatch.addTextBoundSchema(type, schema);
					break;
				}
				case "relation": {
					String type = DOMUtil.getMandatoryAttribute(child, "type");
					Schema<Relation> schema = RelationConverter.INSTANCE.convert(child);
					typeDispatch.addRelationSchema(type, schema);
					break;
				}
				case "modifier": {
					String type = DOMUtil.getMandatoryAttribute(child, "type");
					Schema<Modifier> schema = ModifierConverter.INSTANCE.convert(child);
					typeDispatch.addModifierSchema(type, schema);
					break;
				}
				case "normalization": {
					String type = DOMUtil.getMandatoryAttribute(child, "type");
					Schema<Normalization> schema = new NormalizationConverter(classLoader).convert(child);
					typeDispatch.addNormalizationSchema(type, schema);
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
