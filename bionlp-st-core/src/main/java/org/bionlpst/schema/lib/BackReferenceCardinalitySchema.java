package org.bionlpst.schema.lib;

import java.util.ArrayList;
import java.util.Collection;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

/**
 * Schema that checks the number of references to the checked annotation.
 * @author rbossy
 *
 * @param <T>
 */
public class BackReferenceCardinalitySchema<T extends Annotation> implements Schema<T> {
	private final String type;
	private final int atLeast;
	private final int atMost;
	
	/**
	 * 
	 * @param type type of the annotations that reference the checked annotation.
	 * @param atLeast minimum number of references expected.
	 * @param atMost maximum number of references expected.
	 */
	public BackReferenceCardinalitySchema(String type, int atLeast, int atMost) {
		super();
		this.type = type;
		this.atLeast = atLeast;
		this.atMost = atMost;
	}
	
	private void check(CheckLogger logger, Annotation item, Collection<Annotation> backRefs) {
		int n = backRefs.size();
		if (atLeast == atMost && n != atLeast) {
			logger.serious(item.getLocation(), "expected " + atLeast + " references from " + type + ", got " + n);
			return;
		}
		if (n < atLeast) {
			logger.serious(item.getLocation(), "expected at least " + atLeast + " references from " + type + ", got " + n);
			return;
		}
		if (n > atMost) {
			logger.serious(item.getLocation(), "expected at most " + atMost + " references from " + type + ", got " + n);
			return;
		}
	}

	@Override
	public void check(CheckLogger logger, T item) {
		Collection<Annotation> referenceBackRefs = new ArrayList<Annotation>();
		Collection<Annotation> predictionBackRefs = new ArrayList<Annotation>();
		for (Annotation br : item.getBackReferences()) {
			if (br.getType().equals(type)) {
				AnnotationSet aset = br.getAnnotationSet();
				switch (aset.getSelector()) {
				case INPUT: {
					referenceBackRefs.add(br);
					predictionBackRefs.add(br);
					break;
				}
				case PREDICTION: {
					predictionBackRefs.add(br);
					break;
				}
				case REFERENCE: {
					referenceBackRefs.add(br);
					break;
				}
				}
			}
		}
		AnnotationSet aset = item.getAnnotationSet();
		switch (aset.getSelector()) {
		case INPUT: {
			//check(logger, item, referenceBackRefs);
			check(logger, item, predictionBackRefs);
			break;
		}
		case PREDICTION: {
			check(logger, item, predictionBackRefs);
			break;
		}
		case REFERENCE: {
			//check(logger, item, referenceBackRefs);
			break;
		}
		}
	}

	@Override
	public Schema<T> reduce() {
		return this;
	}
}
