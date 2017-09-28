package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.Document;
import org.bionlpst.corpus.DocumentCollection;
import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.util.Filter;

public class AnnotationEvaluation extends Evaluation<Annotation> {
	private final boolean inputIteration;
	private final Filter<Annotation> preFilter;

	public AnnotationEvaluation(String name, PairingAlgorithm<Annotation> pairingAlgorithm, Similarity<Annotation> matchingSimilarity, boolean inputIteration, Filter<Annotation> preFilter) {
		super(name, pairingAlgorithm, matchingSimilarity);
		this.preFilter = preFilter;
		this.inputIteration = inputIteration;
	}

	public AnnotationEvaluation(String name, PairingAlgorithm<Annotation> pairingAlgorithm, Similarity<Annotation> matchingSimilarity, Collection<Scoring<Annotation>> scorings, boolean inputIteration, Filter<Annotation> preFilter) {
		super(name, pairingAlgorithm, matchingSimilarity, scorings);
		this.preFilter = preFilter;
		this.inputIteration = inputIteration;
	}

	public Filter<Annotation> getPreFilter() {
		return preFilter;
	}

	public boolean isInputIteration() {
		return inputIteration;
	}

	public List<Pair<Annotation>> getDocumentPairs(Document doc) {
		if (inputIteration) {
			AnnotationSet aset = doc.getInputAnnotationSet();
			Collection<Annotation> annotations = aset.getAnnotations(preFilter);
			List<Pair<Annotation>> result = new ArrayList<Pair<Annotation>>(annotations.size());
			for (Annotation a : annotations) {
				Pair<Annotation> p = new Pair<Annotation>(a, a);
				result.add(p);
			}
			return result;
		}
		Collection<Annotation> reference = doc.getReferenceAnnotationSet().getAnnotations(preFilter);
		Collection<Annotation> prediction = doc.getPredictionAnnotationSet().getAnnotations(preFilter);
		return getPairs(reference, prediction);
	}

	public List<Pair<Annotation>> getPairs(DocumentCollection documentCollection) {
		List<Pair<Annotation>> result = new ArrayList<Pair<Annotation>>();
		for (Document doc : documentCollection.getDocuments()) {
			result.addAll(getDocumentPairs(doc));
		}
		return result;
	}

	public EvaluationResult<Annotation> getResult(DocumentCollection documentCollection, boolean keepPairs, BootstrapConfig bootstrap) {
		return getResult(getPairs(documentCollection), keepPairs, bootstrap);
	}
	
	public EvaluationResult<Annotation> getMainResult(DocumentCollection documentCollection, boolean keepPairs, BootstrapConfig bootstrap) {
		return getMainResult(getPairs(documentCollection), keepPairs, bootstrap);
	}
}
