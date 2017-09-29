package org.bionlpst.evaluation.xml;

import java.io.BufferedReader;
import java.util.List;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationKind;
import org.bionlpst.corpus.AnnotationTypeFilter;
import org.bionlpst.corpus.BackReferenceCardinalityFilter;
import org.bionlpst.corpus.SameSentenceFilter;
import org.bionlpst.util.Filter;
import org.bionlpst.util.SourceStream;
import org.bionlpst.util.Util;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.bionlpst.util.dom.SourceStreamConverter;
import org.w3c.dom.Element;

public class FilterConverter implements DOMElementConverter<Filter<Annotation>> {
	private final ClassLoader classLoader;

	public FilterConverter(ClassLoader classLoader) {
		super();
		Util.notnull(classLoader);
		this.classLoader = classLoader;
	}

	@Override
	public Filter<Annotation> convert(Element element) throws Exception {
		String tag = element.getTagName();
		switch (tag) {
			case "true": {
				return new Filter.AcceptAll<Annotation>();
			}
			case "false": {
				return new Filter.RejectAll<Annotation>();
			}
			case "text-bound": {
				return AnnotationKind.TEXT_BOUND;
			}
			case "relation": {
				return AnnotationKind.RELATION;
			}
			case "modifier": {
				return AnnotationKind.MODIFIER;
			}
			case "normalization": {
				return AnnotationKind.NORMALIZATION;
			}
			case "not": {
				List<Element> children = DOMUtil.getChildrenElements(element, false);
				if (children.isEmpty()) {
					throw new BioNLPSTException("<not> requires a filter");
				}
				if (children.size() > 1) {
					throw new BioNLPSTException("<not> accepts a single filter");
				}
				Element firstChild = children.get(0);
				Filter<Annotation> filter = convert(firstChild);
				return new Filter.Inverse<Annotation>(filter);
			}
			case "and": {
				Filter.Conjunction<Annotation> result = new Filter.Conjunction<Annotation>();
				for (Element child : DOMUtil.getChildrenElements(element, false)) {
					Filter<Annotation> filter = convert(child);
					result.addFilter(filter);
				}
				return result;
			}
			case "or": {
				Filter.Disjunction<Annotation> result = new Filter.Disjunction<Annotation>();
				for (Element child : DOMUtil.getChildrenElements(element, false)) {
					Filter<Annotation> filter = convert(child);
					result.addFilter(filter);
				}
				return result;
			}
			case "types": {
				String[] types = DOMUtil.getArrayContents(element);
				return new AnnotationTypeFilter<Annotation>(types);
			}
			case "argument": {
				String role = DOMUtil.getMandatoryAttribute(element, "role");
				Element child = DOMUtil.getFirstChildElement(element);
				Filter<Annotation> filter = convert(child);
				return new RelationArgumentFilter(role, filter);
			}
			case "same-sentence": {
				SourceStreamConverter converter = new SourceStreamConverter(classLoader);
				SourceStream source = converter.convert(element);
				try (BufferedReader r = source.openBufferedReader()) {
					return new SameSentenceFilter(r);
				}
			}
			case "backreference-cardinality": {
				String type = element.getTextContent().trim();
				int atLeast = DOMUtil.getIntAttribute(element, "at-least", 0);
				int atMost = DOMUtil.getIntAttribute(element, "at-most", Integer.MAX_VALUE);
				return new BackReferenceCardinalityFilter<Annotation>(type, atLeast, atMost);
			}
			case "custom": {
				@SuppressWarnings("unchecked")
				Filter<Annotation> result = DOMUtil.getContentsByClassName(element, Filter.class);
				return result;
			}
			default: {
				throw new BioNLPSTException("unknown filter: " + tag);
			}
		}
	}
}
