package org.bionlpst.evaluation.xml;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Annotation;
import org.bionlpst.evaluation.HeuristicPairing;
import org.bionlpst.evaluation.PairingAlgorithm;
import org.bionlpst.evaluation.PredictionPairing;
import org.bionlpst.evaluation.ReferencePairing;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public class PairingAlgorithmConverter implements DOMElementConverter<PairingAlgorithm<Annotation>> {
	private final ClassLoader classLoader;

	public PairingAlgorithmConverter(ClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public PairingAlgorithm<Annotation> convert(Element element) throws Exception {
		String tag = element.getTagName();
		switch (tag) {
			case "heuristic": {
				return new HeuristicPairing<Annotation>();
			}
			case "references": {
				double threshold = DOMUtil.getDoubleAttribute(element, "threshold", 0);
				boolean falseNegativePairs = DOMUtil.getBooleanAttribute(element, "false-negatives", false);
				return new ReferencePairing<Annotation>(threshold, falseNegativePairs);
			}
			case "predictions": {
				double threshold = DOMUtil.getDoubleAttribute(element, "threshold", 0);
				boolean falsePositivePairs = DOMUtil.getBooleanAttribute(element, "false-positives", false);
				return new PredictionPairing<Annotation>(threshold, falsePositivePairs);
			}
			case "custom": {
				@SuppressWarnings("unchecked")
				PairingAlgorithm<Annotation> result = DOMUtil.getContentsByClassName(element, PairingAlgorithm.class);
				return result;
			}
			default: {
				throw new BioNLPSTException("unknown similarity: " + tag);
			}
		}
	}
}
