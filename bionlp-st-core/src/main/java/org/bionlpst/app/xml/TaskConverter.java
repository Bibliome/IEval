package org.bionlpst.app.xml;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.app.CorpusPostprocessing;
import org.bionlpst.app.Task;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.parser.CorpusAndReferenceParser;
import org.bionlpst.evaluation.AnnotationEvaluation;
import org.bionlpst.evaluation.xml.EvaluationConverter;
import org.bionlpst.schema.Schema;
import org.bionlpst.schema.xml.CorpusSchemaConverter;
import org.bionlpst.util.Util;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public class TaskConverter implements DOMElementConverter<Task> {
	private final ClassLoader classLoader;
	private final CorpusAndReferenceParserConverter corpusAndReferenceParserConverter;
	
	public TaskConverter(ClassLoader classLoader) {
		Util.notnull(classLoader);
		this.classLoader = classLoader;
		this.corpusAndReferenceParserConverter = new CorpusAndReferenceParserConverter(classLoader);
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public CorpusAndReferenceParserConverter getCorpusAndReferenceParserConverter() {
		return corpusAndReferenceParserConverter;
	}

	@Override
	public Task convert(Element element) throws Exception {
		String name = DOMUtil.getMandatoryAttribute(element, "name");
		Task result = new Task(name);
		for (Element child : DOMUtil.getChildrenElements(element, false)) {
			String tag = child.getTagName();
			switch (tag) {
				case "description": {
					String descr = child.getTextContent();
					result.addDescription(descr.trim());
					break;
				}
				case "post-processing": {
					CorpusPostprocessing corpusPostprocessing = DOMUtil.getContentsByClassName(child, CorpusPostprocessing.class);
					result.setCorpusPostprocessing(corpusPostprocessing);
					break;
				}
				case "schema": {
					Schema<Corpus> schema = result.getSchema();
					if (schema != null) {
						throw new BioNLPSTException("duplicate schema");
					}
					schema = new CorpusSchemaConverter(classLoader).convert(child);
					schema = schema.reduce();
					result.setSchema(schema);
					break;
				}
				case "evaluation": {
					AnnotationEvaluation eval = new EvaluationConverter(classLoader).convert(child);
					result.addEvaluation(eval);
					break;
				}
				case "train": {
					CorpusAndReferenceParser train = result.getTrainSource();
					if (train != null) {
						throw new BioNLPSTException("duplicate train source");
					}
					train = corpusAndReferenceParserConverter.convert(child);
					result.setTrainSource(train);
					break;
				}
				case "dev": {
					CorpusAndReferenceParser dev = result.getDevSource();
					if (dev != null) {
						throw new BioNLPSTException("duplicate dev source");
					}
					dev = corpusAndReferenceParserConverter.convert(child);
					result.setDevSource(dev);
					break;
				}
				case "test": {
					CorpusAndReferenceParser test = result.getTestSource();
					if (test != null) {
						throw new BioNLPSTException("duplicate test source");
					}
					test = corpusAndReferenceParserConverter.convert(child);
					result.setTestSource(test);
					boolean hasReferenceAnnotations = DOMUtil.getBooleanAttribute(child, "with-reference", false);
					result.setTestHasReferenceAnnotations(hasReferenceAnnotations);
					break;
				}
				default: {
					throw new BioNLPSTException("unexpected tag: " + tag);
				}
			}
		}
		if (result.getSchema() == null) {
			throw new BioNLPSTException("missing schema");
		}
		if (result.getTrainSource() == null) {
			throw new BioNLPSTException("missing train corpus");
		}
		if (result.getDevSource() == null) {
			throw new BioNLPSTException("missing dev corpus");
		}
		if (result.getEvaluations().isEmpty()) {
			throw new BioNLPSTException("missing evaluations");
		}
		return result;
	}
}
