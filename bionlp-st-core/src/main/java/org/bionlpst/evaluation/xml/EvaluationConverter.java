package org.bionlpst.evaluation.xml;

import java.util.ArrayList;
import java.util.List;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationTypeFilter;
import org.bionlpst.evaluation.AnnotationEvaluation;
import org.bionlpst.evaluation.HeuristicPairing;
import org.bionlpst.evaluation.Pair;
import org.bionlpst.evaluation.PairFilter;
import org.bionlpst.evaluation.PairingAlgorithm;
import org.bionlpst.evaluation.Scoring;
import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.util.Filter;
import org.bionlpst.util.Util;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public class EvaluationConverter implements DOMElementConverter<AnnotationEvaluation> {
	private final ClassLoader classLoader;
	
	public EvaluationConverter(ClassLoader classLoader) {
		super();
		Util.notnull(classLoader);
		this.classLoader = classLoader;
	}

	@Override
	public AnnotationEvaluation convert(Element element) throws Exception {
		String name = DOMUtil.getMandatoryAttribute(element, "name");
		PairingAlgorithm<Annotation> pairingAlgorithm = new HeuristicPairing<Annotation>();
		Similarity<Annotation> matchingSimilarity = null;
		List<Scoring<Annotation>> scorings = new ArrayList<Scoring<Annotation>>();
		Filter<Annotation> preFilter = null;
		for (Element child : DOMUtil.getChildrenElements(element)) {
			String tag = child.getTagName();
			switch (tag) {
				case "pairing-algorithm": {
					PairingAlgorithmConverter converter = new PairingAlgorithmConverter(classLoader);
					pairingAlgorithm = converter.convert(DOMUtil.getFirstChildElement(child));
					break;
				}
				case "matching-similarity": {
					if (matchingSimilarity != null) {
						throw new BioNLPSTException("duplicate matching similarity");
					}
					matchingSimilarity = new SimilarityConverter(classLoader).convert(DOMUtil.getFirstChildElement(child));
					break;
				}
				case "pre-filter": {
					if (preFilter != null) {
						throw new BioNLPSTException("duplicate pre-filter");
					}
					FilterConverter converter = new FilterConverter(classLoader);
					preFilter = converter.convert(DOMUtil.getFirstChildElement(child));
					break;
				}
				case "scoring": {
					ScoringConverter scoringConverter = new ScoringConverter(classLoader, matchingSimilarity);
					Scoring<Annotation> scoring = scoringConverter.convert(child);
					scorings.add(scoring);
					break;
				}
				case "type-scorings": {
					ScoringConverter scoringConverter = new ScoringConverter(classLoader, matchingSimilarity, scorings);
					Scoring<Annotation> template = scoringConverter.convert(child);
					String[] types = scoringConverter.getTypes();
					if (types == null) {
						throw new BioNLPSTException("missing tag: types");
					}
					for (String type : types) {
						Filter<Annotation> filter = new AnnotationTypeFilter<Annotation>(type);
						Filter<Pair<Annotation>> pairFilter = new PairFilter<Annotation>(filter);
						Filter<Pair<Annotation>> postFilter = template.getPostFilter();
						Filter.Conjunction<Pair<Annotation>> andFilter = new Filter.Conjunction<Pair<Annotation>>();
						andFilter.addFilter(pairFilter);
						andFilter.addFilter(postFilter);
						Scoring<Annotation> scoring = new Scoring<Annotation>(type, andFilter, template.getSimilarity(), template.getMeasures());
						scorings.add(scoring);
					}
					break;
				}
				default: {
					throw new BioNLPSTException("unexpected tag: " + tag);
				}
			}
		}
		if (matchingSimilarity == null) {
			throw new BioNLPSTException("missing matching similarity");
		}
		if (scorings.isEmpty()) {
			throw new BioNLPSTException("missing scorings");
		}
		if (preFilter == null) {
			preFilter = new Filter.AcceptAll<Annotation>();
		}
		boolean inputIteration = DOMUtil.getBooleanAttribute(element, "input-iteration", false);
		return new AnnotationEvaluation(name, pairingAlgorithm, matchingSimilarity, scorings, inputIteration, preFilter);
	}
}
