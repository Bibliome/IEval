package org.bionlpst.evaluation.xml;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.evaluation.Measure;
import org.bionlpst.evaluation.PairFilter;
import org.bionlpst.evaluation.Scoring;
import org.bionlpst.evaluation.StandardMeasures;
import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.util.Filter;
import org.bionlpst.util.Util;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public class ScoringConverter implements DOMElementConverter<Scoring<Annotation>> {
	private final ClassLoader classLoader;
	private final Similarity<Annotation> defaultSimilarity;
	private final List<Measure> defaultMeasures;
	private final Filter<Annotation> defaultPostFilter;
	private final boolean acceptTypes;
	private String[] types = null;

	private ScoringConverter(ClassLoader classLoader, Similarity<Annotation> defaultSimilarity, List<Measure> defaultMeasures, Filter<Annotation> defaultPostFilter, boolean acceptTypes) {
		super();
		this.classLoader = classLoader;
		this.defaultSimilarity = defaultSimilarity;
		this.defaultMeasures = defaultMeasures;
		this.defaultPostFilter = defaultPostFilter;
		Util.notnull(defaultPostFilter);
		this.acceptTypes = acceptTypes;
	}
	
	public ScoringConverter(ClassLoader classLoader, Similarity<Annotation> defaultSimilarity) {
		this(classLoader, defaultSimilarity, null, new Filter.AcceptAll<Annotation>(), false);
	}
	
	public ScoringConverter(ClassLoader classLoader, Similarity<Annotation> defaultSimilarity, List<Scoring<Annotation>> scorings) {
		this(classLoader, defaultSimilarity, scorings.isEmpty() ? null : scorings.get(0).getMeasures(), new Filter.AcceptAll<Annotation>(), true);
	}

	public boolean isAcceptTypes() {
		return acceptTypes;
	}

	public String[] getTypes() {
		return types;
	}

	public Similarity<Annotation> getDefaultSimilarity() {
		return defaultSimilarity;
	}

	public List<Measure> getDefaultMeasures() {
		return defaultMeasures;
	}

	public Filter<Annotation> getDefaultPostFilter() {
		return defaultPostFilter;
	}

	@Override
	public Scoring<Annotation> convert(Element element) throws Exception {
		String name = acceptTypes ? "" : DOMUtil.getMandatoryAttribute(element, "name");
		Filter<Annotation> postFilter = null;
		Similarity<Annotation> similarity = null;
		Collection<Measure> measures = new ArrayList<Measure>();
		for (Element child : DOMUtil.getChildrenElements(element)) {
			String tag = child.getTagName();
			switch (tag) {
				case "post-filter": {
					if (postFilter != null) {
						throw new BioNLPSTException("duplicate post-filter");
					}
					FilterConverter converter = new FilterConverter(classLoader);
					postFilter = converter.convert(DOMUtil.getFirstChildElement(child));
					break;
				}
				case "similarity": {
					if (similarity != null) {
						throw new BioNLPSTException("duplicate similarity");
					}
					similarity = new SimilarityConverter(classLoader).convert(DOMUtil.getFirstChildElement(child));
					break;
				}
				case "measure": {
					String sMeasure = child.getTextContent();
					Measure measure = MeasureConverter.getMeasure(sMeasure);
					measures.add(measure);
					break;
				}
				case "f1-measures": {
					measures.addAll(StandardMeasures.getF1Measures());
					break;
				}
				case "ser-measures": {
					measures.addAll(StandardMeasures.getSERMeasures());
					break;
				}
				case "count-measures": {
					measures.addAll(StandardMeasures.getCountMeasures());
					break;
				}
				case "types": {
					if (acceptTypes) {
						types = DOMUtil.getArrayContents(child);
						break;
					}
				}
				default: {
					throw new BioNLPSTException("unexpected tag: " + tag);
				}
			}
		}
		if (postFilter == null) {
			postFilter = defaultPostFilter;
		}
		if (similarity == null) {
			if (defaultSimilarity == null) {
				throw new BioNLPSTException("missing scoring similarity");
			}
			similarity = defaultSimilarity;
		}
		if (measures.isEmpty()) {
			if (defaultMeasures == null || defaultMeasures.isEmpty()) {
				throw new BioNLPSTException("missing measures in scoring");
			}
			measures = defaultMeasures;
		}
		return new Scoring<Annotation>(name, new PairFilter<Annotation>(postFilter), similarity, measures);
	}
}
