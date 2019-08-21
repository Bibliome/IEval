package org.bionlpst.corpus;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;

import org.bionlpst.evaluation.Pair;
import org.bionlpst.util.Filter;

public class UniqueFormAndNormalizationFilter implements Filter<Annotation> {
	private final String textBoundType;
	private final String normalizationType;
	
	private final Collection<Pair<String>> formNormPairs = new LinkedHashSet<Pair<String>>();

	public UniqueFormAndNormalizationFilter(String textBoundType, String normalizationType) {
		super();
		this.textBoundType = textBoundType;
		this.normalizationType = normalizationType;
	}

	@Override
	public boolean accept(Annotation item) {
		TextBound tb = item.asTextBound();
		if (tb == null) {
			return false;
		}
		if ((textBoundType != null) && !tb.getType().equals(textBoundType)) {
			return false;
		}
		if (tb.getAnnotationSet().getSelector() == AnnotationSetSelector.PREDICTION) {
			return false;
		}
		String form = tb.getForm();
		List<Pair<String>> pairs = new ArrayList<Pair<String>>();
		for (Normalization norm : tb.getNormalizationBackReferences(normalizationType)) {
			if (norm.getAnnotationSet().getSelector() == AnnotationSetSelector.PREDICTION) {
				continue;
			}
			Pair<String> pair = new Pair<String>(form, norm.getReferent());
			if (formNormPairs.contains(pair)) {
				return false;
			}
			pairs.add(pair);
		}
		formNormPairs.addAll(pairs);
		return true;
	}

	@Override
	public void init() {
		formNormPairs.clear();
	}

	@Override
	public Filter<Annotation> reduce() {
		return this;
	}
}
