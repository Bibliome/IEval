package org.bionlpst.schema.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.SingleReferenceAnnotation;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationKind;
import org.bionlpst.corpus.DummyAnnotation;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

/**
 * Schema that checks the type of the annotation referenced by a modifier or a normalization.
 * @author rbossy
 *
 * @param <T>
 */
public class SingleReferenceAnnotationTypecheckSchema<T extends SingleReferenceAnnotation> implements Schema<T> {
	private final Collection<String> allowedTypes = new HashSet<String>();
	public SingleReferenceAnnotationTypecheckSchema(String... allowedTypes) {
		this(Arrays.asList(allowedTypes));
	}

	public SingleReferenceAnnotationTypecheckSchema(Collection<String> allowedTypes) {
		super();
		this.allowedTypes.addAll(allowedTypes);
	}

	public Collection<String> getAllowedTypes() {
		return Collections.unmodifiableCollection(allowedTypes);
	}

	public void addAllowedType(String type) throws BioNLPSTException {
		if (allowedTypes.contains(type)) {
			throw new BioNLPSTException();
		}
		allowedTypes.add(type);
	}

	@Override
	public void check(CheckLogger logger, T item) {
		Annotation ann = item.getAnnotation();
		String type = ann.getType();
		if (!allowedTypes.contains(type)) {
			if (ann.getKind() == AnnotationKind.DUMMY && type.equals(DummyAnnotation.DUMMY_TYPE)) {
				return;
			}
			logger.serious(item.getLocation(), "type error [" + ann.getLocation().getMessage(type) + "]");
		}
	}

	@Override
	public Schema<T> reduce() {
		return this;
	}
}
