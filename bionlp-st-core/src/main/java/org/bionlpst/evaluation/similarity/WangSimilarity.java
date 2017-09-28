package org.bionlpst.evaluation.similarity;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.TreeMap;

import org.bionlpst.BioNLPSTException;

public class WangSimilarity implements Similarity<Collection<String>> {
	private final Map<String,Map<String,Double>> nodeSimilarityCache = new HashMap<String,Map<String,Double>>();
	private final Map<String,Map<String,Double>> sValues;
	private final double weight;

	public WangSimilarity(Map<String,Map<String,Double>> sValues, double weight) {
		super();
		this.sValues = sValues;
		this.weight = weight;
	}
	
	private static WangSimilarity createFromNodes(Map<String,Node> nodes, double weight) {
		return new WangSimilarity(sValues(nodes, weight), weight);
	}
	
	public static WangSimilarity createFromParentsFile(BufferedReader parents, double weight) throws IOException {
		return createFromNodes(buildNodes(parents), weight);
	}

	private static class Node {
		private final String id;
		private final Collection<Node> parents = new HashSet<Node>();
		
		private Node(String id) {
			super();
			this.id = id;
		}

		private void addParent(Node parent) {
			parents.add(parent);
		}
		
		private void sValues(Map<String,Double> result, double current, double weight) {
			if (result.containsKey(id)) {
				double prev = result.get(id);
				if (current > prev) {
					result.put(id, current);
				}
			}
			else {
				result.put(id, current);
			}
			double next = current * weight;
			for (Node parent : parents) {
				parent.sValues(result, next, weight);
			}
		}
		
		private Map<String,Double> sValues(double weight) {
			Map<String,Double> result = new TreeMap<String,Double>();
			sValues(result, 1, weight);
			return result;
		}
	}
	
	private static Node ensure(Map<String,Node> nodes, String id) {
		if (nodes.containsKey(id)) {
			return nodes.get(id);
		}
		Node result = new Node(id);
		nodes.put(id, result);
		return result;
	}
	
	private static void buildNodes(Map<String,Node> nodes, String childId, String parentId) {
		Node child = ensure(nodes, childId);
		Node parent = ensure(nodes, parentId);
		child.addParent(parent);
	}
	
	private static Map<String,Node> buildNodes(BufferedReader r) throws IOException {
		Map<String,Node> result = new TreeMap<String,Node>();
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			int tab = line.indexOf('\t');
			if (tab == -1) {
				throw new BioNLPSTException("missing tab");
			}
			String childId = line.substring(0, tab);
			String parentId = line.substring(tab+1);
			buildNodes(result, childId, parentId);
		}
		return result;
	}
	
	private static Map<String,Map<String,Double>> sValues(Map<String,Node> nodes, double weight) {
		Map<String,Map<String,Double>> result = new HashMap<String, Map<String,Double>>();
		for (Node node : nodes.values()) {
			result.put(node.id, node.sValues(weight));
		}
		return result;
	}
	
	private Double getCached(String a, String b) {
		if (!nodeSimilarityCache.containsKey(a)) {
			return null;
		}
		return nodeSimilarityCache.get(a).get(b);
	}
	
	private static double getSemanticValue(Map<String,Double> sValues) {
		double result = 0;
		for (double s : sValues.values()) {
			result += s;
		}
		return result;
	}
	
	private double getNodeSimilarity(StringBuilder sb, String a, String b) {
		Double cached = getCached(a, b);
		if (sb == null && cached != null) {
			return cached;
		}
		if (!sValues.containsKey(a)) {
			return 0;
		}
		if (!sValues.containsKey(b)) {
			return 0;
		}
		Map<String,Double> sValues_a = sValues.get(a);
		Map<String,Double> sValues_b = sValues.get(b);
		Collection<String> inter = new HashSet<String>(sValues_a.keySet());
		inter.retainAll(sValues_b.keySet());
		double interSum = 0;
		for (String id : inter) {
			interSum += sValues_a.get(id);
			interSum += sValues_b.get(id);
		}
		double SVa = getSemanticValue(sValues_a);
		double SVb = getSemanticValue(sValues_b);
		double result = interSum / (SVa + SVb);
		putCache(a, b, result);
		if (sb != null) {
			sb.append("sim(");
			sb.append(a);
			sb.append(",");
			sb.append(b);
			sb.append(") = ");
			sb.append(result);
			sb.append('\n');
//			sb.append("sva = ");
//			sb.append(sValues_a);
//			sb.append("\n");
//			sb.append("svb = ");
//			sb.append(sValues_b);
//			sb.append("\n");
//			sb.append("inter = ");
//			sb.append(inter);
//			sb.append("\n");
		}
		return result;
	}
	
	private Map<String,Double> ensureInCache(String id) {
		if (nodeSimilarityCache.containsKey(id)) {
			return nodeSimilarityCache.get(id);
		}
		Map<String,Double> result = new HashMap<String,Double>();
		nodeSimilarityCache.put(id, result);
		return result;
	}
	
	private void putCache(String a, String b, double result) {
		ensureInCache(a).put(b, result);
		ensureInCache(b).put(a, result);
	}

	private double getNodeSimilarity(StringBuilder sb, String a, Collection<String> bs) {
		double result = 0;
		for (String b : bs) {
			result = Math.max(result, getNodeSimilarity(sb, a, b));
		}
		return result;
	}
	
	private double getNodeSimilaritySum(StringBuilder sb, Collection<String> as, Collection<String> bs) {
		double result = 0;
		for (String a : as) {
			result += getNodeSimilarity(sb, a, bs);
		}
		return result;
	}

	@Override
	public double compute(Collection<String> as, Collection<String> bs) {
		return (getNodeSimilaritySum(null, as, bs) + getNodeSimilaritySum(null, bs, as)) / (as.size() + bs.size());
	}
	
	private double compute(StringBuilder sb, Collection<String> as, Collection<String> bs) {
		return (getNodeSimilaritySum(sb, as, bs) + getNodeSimilaritySum(sb, bs, as)) / (as.size() + bs.size());
	}

	@Override
	public void explain(StringBuilder sb, Collection<String> a, Collection<String> b) {
		sb.append("wang(");
		sb.append(weight);
		sb.append("):\n");
		double s = compute(sb, a, b);
		sb.append(" = ");
		sb.append(s);
	}
}
