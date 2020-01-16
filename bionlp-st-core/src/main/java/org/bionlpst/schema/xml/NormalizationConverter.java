package org.bionlpst.schema.xml;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.schema.CompositeSchema;
import org.bionlpst.schema.Schema;
import org.bionlpst.schema.lib.BackReferenceCardinalitySchema;
import org.bionlpst.schema.lib.NormalizationLabelSchema;
import org.bionlpst.schema.lib.NormalizationVocabularySchema;
import org.bionlpst.schema.lib.SingleReferenceAnnotationTypecheckSchema;
import org.bionlpst.util.SourceStream;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.bionlpst.util.dom.SourceStreamConverter;
import org.w3c.dom.Element;

public class NormalizationConverter implements DOMElementConverter<Schema<Normalization>> {
	private final ClassLoader classLoader;
	
	public NormalizationConverter(ClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public Schema<Normalization> convert(Element element) throws Exception {
		CompositeSchema<Normalization> result = new CompositeSchema<Normalization>();
		
		for (Element child : DOMUtil.getChildrenElements(element, false)) {
			String tag = child.getTagName();
			switch (tag) {
				case "custom": {
					@SuppressWarnings("unchecked")
					Schema<Normalization> schema = DOMUtil.getContentsByClassName(child, Schema.class);
					result.addCompound(schema);
					break;
				}
				case "backreference-cardinality": {
					String type = child.getTextContent().trim();
					int atLeast = DOMUtil.getIntAttribute(child, "at-least", 0);
					int atMost = DOMUtil.getIntAttribute(child, "at-most", Integer.MAX_VALUE);
					Schema<Normalization> schema = new BackReferenceCardinalitySchema<Normalization>(type, atLeast, atMost);
					result.addCompound(schema);
					break;
				}
				case "annotation-types": {
					String[] allowedTypes = DOMUtil.getArrayContents(child);
					Schema<Normalization> schema = new SingleReferenceAnnotationTypecheckSchema<Normalization>(allowedTypes);
					result.addCompound(schema);
					break;
				}
				case "reference-vocabulary": {
					SourceStream source = new SourceStreamConverter(classLoader).convert(child);
					Collection<String> allowedValues = new HashSet<String>();
					try (BufferedReader r = source.openBufferedReader()) {
						while (true) {
							String line = r.readLine();
							if (line == null) {
								break;
							}
							allowedValues.add(line.trim());
						}
					}
					Schema<Normalization> schema = new NormalizationVocabularySchema(allowedValues);
					result.addCompound(schema);
					break;
				}
				case "labels": {
					SourceStream source = new SourceStreamConverter(classLoader).convert(child);
					Map<String,String> labels = new HashMap<String,String>();
					try (BufferedReader r = source.openBufferedReader()) {
						while (true) {
							String line = r.readLine();
							if (line == null) {
								break;
							}
							int tab = line.indexOf('\t');
							if (tab != -1) {
								String ref = line.substring(0, tab);
								String label =  line.substring(tab+1);
								labels.put(ref, label);
							}
						}
					}
					Schema<Normalization> schema = new NormalizationLabelSchema(labels);
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
