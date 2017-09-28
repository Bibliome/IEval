package org.bionlpst.schema.lib;

import java.util.HashMap;
import java.util.Map;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationKind;
import org.bionlpst.corpus.DummyAnnotation;
import org.bionlpst.corpus.Modifier;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.corpus.Relation;
import org.bionlpst.corpus.TextBound;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

/**
 * A schema that dispatches the check of an annotation according to its type.
 * @author rbossy
 *
 */
public class AnnotationSchemaTypeDispatch implements Schema<Annotation> {
	private final Map<String,Schema<Annotation>> typeSchemas = new HashMap<String,Schema<Annotation>>();

	@Override
	public void check(CheckLogger logger, Annotation item) {
		String type = item.getType();
		if (item.getKind() == AnnotationKind.DUMMY && type.equals(DummyAnnotation.DUMMY_TYPE)) {
			return;
		}
		if (!typeSchemas.containsKey(type)) {
			logger.serious(item.getLocation(), "unknown type: " + type);
			return;
		}
		Schema<Annotation> schema = typeSchemas.get(type);
		schema.check(logger, item);
	}

	private void addTypeSchema(String type, Schema<Annotation> schema) {
		typeSchemas.put(type, schema);
	}
	
	/**
	 * Adds a text bound type schema. Annotations of the specified type will be checked against the specified schema, after ensuring the annotation is text bound.
	 * @param type
	 * @param schema
	 */
	public void addTextBoundSchema(String type, Schema<TextBound> schema) {
		addTypeSchema(type, new TextBoundSchemaWrapper(schema));
	}
	
	/**
	 * Adds a relation type schema. Annotations of the specified type will be checked against the specified schema, after ensuring the annotation is a relation.
	 * @param type
	 * @param schema
	 */
	public void addRelationSchema(String type, Schema<Relation> schema) {
		addTypeSchema(type, new RelationSchemaWrapper(schema));
	}
	
	/**
	 * Adds a modifier type schema. Annotations of the specified type will be checked against the specified schema, after ensuring the annotation is a modifier.
	 * @param type
	 * @param schema
	 */
	public void addModifierSchema(String type, Schema<Modifier> schema) {
		addTypeSchema(type, new ModifierSchemaWrapper(schema));
	}
	
	/**
	 * Adds a normalization type schema. Annotations of the specified type will be checked against the specified schema, after ensuring the annotation is a normalization.
	 * @param type
	 * @param schema
	 */
	public void addNormalizationSchema(String type, Schema<Normalization> schema) {
		addTypeSchema(type, new NormalizationSchemaWrapper(schema));
	}

	private static abstract class AnnotationSchemaWrapper<T extends Annotation> implements Schema<Annotation> {
		private final AnnotationKind expectedKind;
		protected final Schema<T> schema;

		protected AnnotationSchemaWrapper(AnnotationKind expectedKind, Schema<T> schema) {
			super();
			this.expectedKind = expectedKind;
			this.schema = schema;
		}

		@Override
		public void check(CheckLogger logger, Annotation item) {
			if (item.getKind() == AnnotationKind.DUMMY) {
				return;
			}
			if (expectedKind != item.getKind()) {
				logger.serious(item.getLocation(), "annotation should be " + expectedKind);
				return;
			}
			schema.check(logger, downcast(item));
		}

		protected abstract T downcast(Annotation item);
	}
	
	private static class TextBoundSchemaWrapper extends AnnotationSchemaWrapper<TextBound> {
		private TextBoundSchemaWrapper(Schema<TextBound> schema) {
			super(AnnotationKind.TEXT_BOUND, schema);
		}

		@Override
		protected TextBound downcast(Annotation item) {
			return item.asTextBound();
		}

		@Override
		public Schema<Annotation> reduce() {
			return new TextBoundSchemaWrapper(schema.reduce());
		}
	}
	
	private static class RelationSchemaWrapper extends AnnotationSchemaWrapper<Relation> {
		private RelationSchemaWrapper(Schema<Relation> schema) {
			super(AnnotationKind.RELATION, schema);
		}

		@Override
		protected Relation downcast(Annotation item) {
			return item.asRelation();
		}

		@Override
		public Schema<Annotation> reduce() {
			return new RelationSchemaWrapper(schema.reduce());
		}
	}

	private static class ModifierSchemaWrapper extends AnnotationSchemaWrapper<Modifier> {
		private ModifierSchemaWrapper(Schema<Modifier> schema) {
			super(AnnotationKind.MODIFIER, schema);
		}

		@Override
		protected Modifier downcast(Annotation item) {
			return item.asModifier();
		}

		@Override
		public Schema<Annotation> reduce() {
			return new ModifierSchemaWrapper(schema.reduce());
		}
	}
	
	private static class NormalizationSchemaWrapper extends AnnotationSchemaWrapper<Normalization> {
		private NormalizationSchemaWrapper(Schema<Normalization> schema) {
			super(AnnotationKind.NORMALIZATION, schema);
		}

		@Override
		protected Normalization downcast(Annotation item) {
			return item.asNormalization();
		}

		@Override
		public Schema<Annotation> reduce() {
			return new NormalizationSchemaWrapper(schema.reduce());
		}
	}

	@Override
	public Schema<Annotation> reduce() {
		AnnotationSchemaTypeDispatch result = new AnnotationSchemaTypeDispatch();
		for (Map.Entry<String,Schema<Annotation>> e : typeSchemas.entrySet()) {
			result.addTypeSchema(e.getKey(), e.getValue().reduce());
		}
		return result;
	}
}
