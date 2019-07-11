package org.bionlpst.corpus;

import java.util.ArrayList;
import java.util.Collection;

import org.bionlpst.util.Filter;

/**
 * Schema that checks the number of references to the checked annotation.
 * @author rbossy
 *
 * @param <T>
 */
public class BackReferenceCardinalityFilter<T extends Annotation> implements Filter<T> {
	private final String type;
	private final int atLeast;
	private final int atMost;

	/**
	 * 
	 * @param type type of the annotations that reference the checked annotation.
	 * @param atLeast minimum number of references expected.
	 * @param atMost maximum number of references expected.
	 */
	public BackReferenceCardinalityFilter(String type, int atLeast, int atMost) {
		super();
		this.type = type;
		this.atLeast = atLeast;
		this.atMost = atMost;
	}

	private boolean check(Collection<Annotation> backRefs) {
		int n = backRefs.size();
		if (atLeast == atMost && n != atLeast) {
			return false;
		}
		if (n < atLeast) {
			return false;
		}
		if (n > atMost) {
			return false;
		}
		return true;
	}

	@Override
	public boolean accept(T item) {
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
				return check(referenceBackRefs) || check(predictionBackRefs);
			}
			case PREDICTION: {
				return check(predictionBackRefs);
			}
			case REFERENCE: {
				return check(referenceBackRefs);
			}
			default: {
				throw new RuntimeException();
			}
		}
	}

	@Override
	public Filter<T> reduce() {
		return this;
	}

	@Override
	public void init() {
	}
}
