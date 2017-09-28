package org.bionlpst.schema.lib;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Document;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

/**
 * Schema that checks all annotations in the checked document.
 * @author rbossy
 *
 */
public class BasicDocumentSchema implements Schema<Document> {
	private final Schema<Annotation> annotationSchema;

	public BasicDocumentSchema(Schema<Annotation> annotationSchema) {
		super();
		this.annotationSchema = annotationSchema;
	}

	public Schema<Annotation> getAnnotationSchema() {
		return annotationSchema;
	}

	@Override
	public void check(CheckLogger logger, Document item) {
		for (Annotation ann : item.getInputAnnotationSet().getAnnotations()) {
			annotationSchema.check(logger, ann);
		}
		for (Annotation ann : item.getReferenceAnnotationSet().getAnnotations()) {
			annotationSchema.check(logger, ann);
		}
		for (Annotation ann : item.getPredictionAnnotationSet().getAnnotations()) {
			annotationSchema.check(logger, ann);
		}
	}

	@Override
	public Schema<Document> reduce() {
		return new BasicDocumentSchema(annotationSchema.reduce());
	}
}
