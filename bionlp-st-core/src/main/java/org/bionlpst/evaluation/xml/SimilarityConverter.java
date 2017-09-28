package org.bionlpst.evaluation.xml;

import java.io.BufferedReader;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.evaluation.similarity.AnnotationKindSimilarity;
import org.bionlpst.evaluation.similarity.AnnotationTypeDispatchSimilarity;
import org.bionlpst.evaluation.similarity.AnnotationTypeSimilarity;
import org.bionlpst.evaluation.similarity.CompositeSimilarity;
import org.bionlpst.evaluation.similarity.ConstantSimilarity;
import org.bionlpst.evaluation.similarity.Identity;
import org.bionlpst.evaluation.similarity.Max;
import org.bionlpst.evaluation.similarity.MaxFromEquivalence;
import org.bionlpst.evaluation.similarity.Min;
import org.bionlpst.evaluation.similarity.NormalizationJaccard;
import org.bionlpst.evaluation.similarity.NormalizationSimilarity;
import org.bionlpst.evaluation.similarity.Product;
import org.bionlpst.evaluation.similarity.RelationArgumentSimilarity;
import org.bionlpst.evaluation.similarity.SameTypeAndArgumentsSimilarity;
import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.evaluation.similarity.SimilarityCutoff;
import org.bionlpst.evaluation.similarity.SingleReferenceSimilarity;
import org.bionlpst.evaluation.similarity.TextBoundJaccard;
import org.bionlpst.evaluation.similarity.WangSimilarity;
import org.bionlpst.util.SourceStream;
import org.bionlpst.util.Util;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.bionlpst.util.dom.SourceStreamConverter;
import org.w3c.dom.Element;

public class SimilarityConverter implements DOMElementConverter<Similarity<Annotation>> {
	private final ClassLoader classLoader;
	
	public SimilarityConverter(ClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}

	@Override
	public Similarity<Annotation> convert(Element element) throws Exception {
		String tag = element.getTagName();
		switch (tag) {
			case "constant": {
				double value = DOMUtil.getDoubleContents(element);
				return new ConstantSimilarity<Annotation>(value);
			}
			case "product": {
				Product<Annotation> result = new Product<Annotation>();
				fillCompositeSimilarity(result, element);
				return result;
			}
			case "min": {
				Min<Annotation> result = new Min<Annotation>();
				fillCompositeSimilarity(result, element);
				return result;
			}
			case "max": {
				Max<Annotation> result = new Max<Annotation>();
				fillCompositeSimilarity(result, element);
				return result;
			}
			case "cutoff": {
				double threshold = DOMUtil.getDoubleAttribute(element, "threshold", 0);
				boolean strict = DOMUtil.getBooleanAttribute(element, "strict", true);
				Similarity<Annotation> sim = convert(DOMUtil.getFirstChildElement(element));
				return new SimilarityCutoff<Annotation>(sim, threshold, strict);
			}
			case "same-kind": {
				return AnnotationKindSimilarity.INSTANCE;
			}
			case "jaccard": {
				return TextBoundJaccard.INSTANCE;
			}
			case "same": {
				return new Identity<Annotation>();
			}
			case "type": {
				double differentTypeValue = DOMUtil.getDoubleAttribute(element, "different", 0);
				return new AnnotationTypeSimilarity<Annotation>(differentTypeValue);
			}
			case "type-table": {
				AnnotationTypeSimilarity.TypeTable typeTable = new AnnotationTypeSimilarity.TypeTable();
				for (Element child : DOMUtil.getChildrenElements(element)) {
					String ref = DOMUtil.getMandatoryAttribute(child, "ref");
					String pred = DOMUtil.getMandatoryAttribute(child, "pred");
					double value = DOMUtil.getDoubleContents(child);
					typeTable.addTypePairValue(ref, pred, value);
				}
				return new AnnotationTypeSimilarity<Annotation>(typeTable);
			}
			case "type-dispatch": {
				double defaultValue = DOMUtil.getDoubleAttribute(element, "default", 1);
				double differentValue = DOMUtil.getDoubleAttribute(element, "different", 0);
				Similarity<Annotation> defaultSimilarity = new ConstantSimilarity<Annotation>(defaultValue);
				Similarity<Annotation> differentSimilarity = new ConstantSimilarity<Annotation>(differentValue);
				AnnotationTypeDispatchSimilarity<Annotation> result = new AnnotationTypeDispatchSimilarity<Annotation>(defaultSimilarity, differentSimilarity);
				for (Element child : DOMUtil.getChildrenElements(element)) {
					String name = DOMUtil.getMandatoryAttribute(child, "type");
					Similarity<Annotation> sim = convert(child);
					result.addSimilarity(name, sim);
				}
				return result;
			}
			case "annotation-reference": {
				Similarity<Annotation> sim = convert(DOMUtil.getFirstChildElement(element));
				return new SingleReferenceSimilarity(sim);
			}
			case "same-annotation-reference": {
				return new SingleReferenceSimilarity(new Identity<Annotation>());
			}
			case "argument": {
				String role = DOMUtil.getMandatoryAttribute(element, "role");
				Similarity<Annotation> sim = convert(DOMUtil.getFirstChildElement(element));
				return new RelationArgumentSimilarity(role, sim);
			}
			case "same-type-and-arguments": {
				Collection<String> commutativeTypes;
				if (element.hasAttribute("commutative-types")) {
					commutativeTypes = new HashSet<String>(Util.split(element.getAttribute("commutative-types"), ','));
				}
				else {
					commutativeTypes = Collections.emptySet();
				}
				boolean resolveEquivalences = DOMUtil.getBooleanAttribute(element, "resolve-equivalences", true);
				Map<String,SameTypeAndArgumentsSimilarity.TypeConversion> typeConversions = new HashMap<String,SameTypeAndArgumentsSimilarity.TypeConversion>();
				for (Element te : DOMUtil.getChildrenElements(element)) {
					String type = te.getTagName();
					SameTypeAndArgumentsSimilarity.TypeConversion conversion = convertTypeConversion(te, type);
					typeConversions.put(type, conversion);
				}
				return new SameTypeAndArgumentsSimilarity(commutativeTypes, resolveEquivalences, typeConversions);
			}
			case "custom": {
				@SuppressWarnings("unchecked")
				Similarity<Annotation> result = DOMUtil.getContentsByClassName(element, Similarity.class);
				return result;
			}
			case "normalization": {
				String normalizationType = DOMUtil.getMandatoryAttribute(element, "normalization-type");
				Map<String,String> referentMap = getReferentMap(element);
				return new NormalizationSimilarity(normalizationType, NormalizationJaccard.INSTANCE, referentMap);
			}
			case "wang": {
				double weight = DOMUtil.getDoubleAttribute(element, "weight");
				String normalizationType = DOMUtil.getMandatoryAttribute(element, "normalization-type");
				SourceStreamConverter converter = new SourceStreamConverter(classLoader);
				SourceStream source = converter.convert(element);
				try (BufferedReader r = source.openBufferedReader()) {
					Similarity<Collection<String>> wang = WangSimilarity.createFromParentsFile(r, weight);
					return new NormalizationSimilarity(normalizationType, wang);
				}
			}
			case "equivalence": {
				Similarity<Annotation> sim = convert(DOMUtil.getFirstChildElement(element));
				return new MaxFromEquivalence(sim);
			}
			default: {
				throw new BioNLPSTException("unknown similarity: " + tag);
			}
		}
	}
	
	private void fillCompositeSimilarity(CompositeSimilarity<Annotation> result, Element element) throws Exception {
		for (Element child : DOMUtil.getChildrenElements(element)) {
			Similarity<Annotation> sim = convert(child);
			result.addSimilarity(sim);
		}
	}
	
	private static SameTypeAndArgumentsSimilarity.TypeConversion convertTypeConversion(Element element, String type) {
		String newType = DOMUtil.getAttribute(element, "new-type", type);
		Map<String,String> roleConversion = new HashMap<String,String>();
		for (Element child : DOMUtil.getChildrenElements(element)) {
			String role = child.getTagName();
			String newRole = child.getTextContent();
			roleConversion.put(role, newRole);
		}
		return new SameTypeAndArgumentsSimilarity.TypeConversion(newType, roleConversion);
	}
	
	private Map<String,String> getReferentMap(Element element) throws Exception {
		Map<String,String> result = new HashMap<String,String>();
		for (Element child : DOMUtil.getChildrenElements(element)) {
			if (child.getTagName().equals("referent-map")) {
				SourceStreamConverter converter = new SourceStreamConverter(classLoader);
				SourceStream source = converter.convert(child);
				try (BufferedReader r = source.openBufferedReader()) {
					while (true) {
						String line = r.readLine();
						if (line == null) {
							break;
						}
						List<String> columns = Util.split(line, '\t');
						String from = columns.get(0);
						String to = columns.get(1);
						result.put(from, to);
					}
				}
			}
		}
		return result;
	}
}
