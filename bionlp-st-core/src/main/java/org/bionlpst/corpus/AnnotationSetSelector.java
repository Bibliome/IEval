package org.bionlpst.corpus;

import org.bionlpst.util.Filter;

/**
 * An annotation set selector specifies if an annotation set contains input, reference, or predicted annotations.
 * @author rbossy
 *
 */
public enum AnnotationSetSelector implements Filter<Annotation> {
	INPUT {
		@Override
		public AnnotationSet getAnnotationSet(Document doc) {
			return doc.getInputAnnotationSet();
		}
	},
	
	REFERENCE {
		@Override
		public AnnotationSet getAnnotationSet(Document doc) {
			return doc.getReferenceAnnotationSet();
		}
	},
	
	PREDICTION {
		@Override
		public AnnotationSet getAnnotationSet(Document doc) {
			return doc.getPredictionAnnotationSet();
		}
	};
	
	/**
	 * Retrieves the annotation set in the specified document corresponding to this selector.
	 * @param doc document.
	 * @return an annotation set.
	 */
	public abstract AnnotationSet getAnnotationSet(Document doc);

	@Override
	public boolean accept(Annotation item) {
		return item.getAnnotationSet().getSelector() == this;
	}

	@Override
	public Filter<Annotation> reduce() {
		return this;
	}
}
