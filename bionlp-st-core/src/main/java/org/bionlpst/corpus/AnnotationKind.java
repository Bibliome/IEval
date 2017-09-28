package org.bionlpst.corpus;

import org.bionlpst.util.Filter;

/**
 * Annotation kinds.
 * @author rbossy
 *
 */
public enum AnnotationKind implements Filter<Annotation> {
	/**
	 * Dummy annotations. 
	 */
	DUMMY,
	
	/**
	 * Text-bound annotations.
	 */
	TEXT_BOUND,
	
	/**
	 * Normalizations.
	 */
	NORMALIZATION,
	
	/**
	 * Modifiers.
	 */
	MODIFIER,
	
	/**
	 * Relations and events.
	 */
	RELATION;

	@Override
	public boolean accept(Annotation item) {
		return item.getKind() == this;
	}

	@Override
	public Filter<Annotation> reduce() {
		return this;
	}
}
