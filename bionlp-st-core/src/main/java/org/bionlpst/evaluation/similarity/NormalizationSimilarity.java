package org.bionlpst.evaluation.similarity;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationSetSelector;
import org.bionlpst.corpus.Normalization;
import org.bionlpst.util.Filter;

public class NormalizationSimilarity implements Similarity<Annotation> {
	private final String normalizationType;
	private final Filter<String> acceptedReferents;
	private final Similarity<Collection<String>> similarity;
	private final Map<String,String> referentMap;
	
	public NormalizationSimilarity(String normalizationType, Filter<String> acceptedReferents, Similarity<Collection<String>> similarity, Map<String,String> referentMap) {
		super();
		this.normalizationType = normalizationType;
		this.acceptedReferents = acceptedReferents;
		this.similarity = similarity;
		this.referentMap = referentMap;
	}
	
	public NormalizationSimilarity(String normalizationType, Filter<String> acceptedReferents, Similarity<Collection<String>> similarity) {
		this(normalizationType, acceptedReferents, similarity, new HashMap<String,String>());
	}
	
	private Collection<String> getNormalizations(Annotation a, AnnotationSetSelector selector) {
		Collection<String> result = new HashSet<String>();
		for (Annotation bref : a.getBackReferences()) {
			Normalization norm = bref.asNormalization();
			if (norm != null && normalizationType.equals(norm.getType()) && selector == bref.getAnnotationSet().getSelector()) {
				String referent = norm.getReferent();
				if (referentMap.containsKey(referent)) {
					referent = referentMap.get(referent);
				}
				if (acceptedReferents.accept(referent)) {
					result.add(referent);
				}
			}
		}
		return result;
	}

	@Override
	public double compute(Annotation a, Annotation b) {
		Collection<String> as = getNormalizations(a, AnnotationSetSelector.REFERENCE);
		Collection<String> bs = getNormalizations(b, AnnotationSetSelector.PREDICTION);
		return similarity.compute(as, bs);
	}

	@Override
	public void explain(StringBuilder sb, Annotation a, Annotation b) {
		Collection<String> as = getNormalizations(a, AnnotationSetSelector.REFERENCE);
		Collection<String> bs = getNormalizations(b, AnnotationSetSelector.PREDICTION);
		similarity.explain(sb, as, bs);
	}
}
