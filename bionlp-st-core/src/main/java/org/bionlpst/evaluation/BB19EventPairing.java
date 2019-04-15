package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationSet;
import org.bionlpst.corpus.Relation;
import org.bionlpst.evaluation.similarity.Similarity;
import org.bionlpst.util.message.CheckLogger;

public class BB19EventPairing implements PairingAlgorithm<Annotation> {
	private final PairingAlgorithm<Annotation> pairingAlgorithm = new HeuristicPairing<Annotation>();

	@Override
	public List<Pair<Annotation>> bestPairing(Collection<Annotation> reference, Collection<Annotation> prediction, Similarity<Annotation> similarity) {
		CheckLogger logger = new CheckLogger();
		Map<Relation,Collection<Relation>> saturation = saturateReference(logger, reference);
		if (saturation.isEmpty()) {
			return pairingAlgorithm.bestPairing(reference, prediction, similarity);
		}
		Collection<Annotation> extendedReference = extendReference(reference, saturation);
		List<Pair<Annotation>> result = pairingAlgorithm.bestPairing(extendedReference, prediction, similarity);
		Map<Annotation,Pair<Annotation>> pairMap = getReferencePairingMap(result);
		Collection<Pair<Annotation>> toAdd = new ArrayList<Pair<Annotation>>();
		for (Map.Entry<Relation,Collection<Relation>> e : saturation.entrySet()) {
			Relation original = e.getKey();
			Annotation best = getBest(pairMap, e.getValue(), similarity);
			Pair<Annotation> p = new Pair<Annotation>(original, best);
			toAdd.add(p);
		}
		Collection<String> toRemove = new HashSet<String>();
		for (Collection<Relation> c : saturation.values()) {
			for (Relation r : c) {
				toRemove.add(r.getId());
			}
		}
		Iterator<Pair<Annotation>> pIt = result.iterator();
		while (pIt.hasNext()) {
			Pair<Annotation> p = pIt.next();
			if (p.hasReference()) {
				Annotation ref = p.getReference();
				if (toRemove.contains(ref.getId())) {
					pIt.remove();
					continue;
				}
			}
			if (p.hasPrediction()) {
				Annotation pred = p.getPrediction();
				if (toRemove.contains(pred.getId())) {
					pIt.remove();
					continue;
				}
			}
		}
		result.addAll(toAdd);
		return result;
	}
	
	private static Annotation getBest(Map<Annotation,Pair<Annotation>> pairMap, Collection<Relation> equiv, Similarity<Annotation> similarity) {
		Annotation best = null;
		double bestScore = 0;
		for (Relation rel : equiv) {
			if (pairMap.containsKey(rel)) {
				Pair<Annotation> p = pairMap.get(rel);
				double score = p.compute(similarity);
				if (score > 0 && (best == null || score > bestScore)) {
					best = p.getPrediction();
					bestScore = score;
				}
			}
		}
		return best;
	}

	private static Map<Annotation,Pair<Annotation>> getReferencePairingMap(List<Pair<Annotation>> pairing) {
		Map<Annotation,Pair<Annotation>> result = new HashMap<Annotation,Pair<Annotation>>();
		for (Pair<Annotation> p : pairing) {
			if (p.hasBoth()) {
				result.put(p.getReference(), p);
			}
		}
		return result;
	}

	private static Collection<Annotation> extendReference(Collection<Annotation> reference, Map<Relation,Collection<Relation>> saturation) {
		Collection<Annotation> result = new ArrayList<Annotation>(reference);
		for (Collection<Relation> c : saturation.values()) {
			result.addAll(c);
		}
		return result;
	}
	
	private static Map<Relation,Collection<Relation>> saturateReference(CheckLogger logger, Collection<Annotation> reference) {
		Map<Relation,Collection<Relation>> result = new HashMap<Relation,Collection<Relation>>();
		for (Annotation a : reference) {
			Relation livesIn = getRelation(a);
			if (livesIn == null) {
				continue;
			}
			Collection<Relation> equiv = saturate(logger, livesIn);
			if (equiv != null) {
				result.put(livesIn, equiv);
			}
		}
		return result;
	}
	
	private static Collection<Relation> saturate(CheckLogger logger, Relation relation) {
		Iterator<String> roles = relation.getRoles().iterator();
		String role1 = roles.next();
		String role2 = roles.next();
		Annotation arg1 = relation.getArgument(role1);
		Annotation arg2 = relation.getArgument(role2);
		Collection<Annotation> equiv1 = arg1.getEquivalents();
		Collection<Annotation> equiv2 = arg2.getEquivalents();
		int len1 = equiv1.size();
		int len2 = equiv2.size();
		if (len1 == 1 && len2 == 1) {
			return null;
		}
		Collection<Relation> result = new ArrayList<Relation>(len1 * len2);
		result.add(relation);
		String type = relation.getType();
		AnnotationSet annotationSet = relation.getAnnotationSet();
		for (Annotation a1 : equiv1) {
			if (a1.equals(arg1)) {
				continue;
			}
			for (Annotation a2 : equiv2) {
				if (a2.equals(arg2)) {
					continue;
				}
				String id = UUID.randomUUID().toString();
				Map<String,String> argumentReferences = new HashMap<String,String>();
				argumentReferences.put(role1, a1.getId());
				argumentReferences.put(role2, a2.getId());
				Relation rel = new Relation(logger, annotationSet, relation.getLocation(), id, type, argumentReferences);
				rel.resolveReferences(logger);
				annotationSet.removeAnnotation(id);
				result.add(rel);
			}
		}
		annotationSet.resolveReferences(logger);
		return result;
	}
	
	private static Relation getRelation(Annotation a) {
		Relation result = a.asRelation();
		if (result == null) {
			return null;
		}
		if (!result.hasArgument("Microorganism")) {
			return null;
		}
		switch (result.getType()) {
			case "Lives_In":
				if (!result.hasArgument("Location")) {
					return null;
				}
				break;
			case "Exhibits":
				if (!result.hasArgument("Phenotype")) {
					return null;
				}
				break;
			default:
				return null;
		}
		return result;
	}
}
