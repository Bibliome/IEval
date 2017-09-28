package org.bionlpst.evaluation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bionlpst.evaluation.Pair.Selector;
import org.bionlpst.evaluation.similarity.Similarity;

public class AggregateMeasure implements Measure {
	private final AggregateFunction aggregateFunction;
	private final Pair.Selector pairSelector;
	
	public AggregateMeasure(AggregateFunction aggregateFunction, Selector pairSelector) {
		super();
		this.aggregateFunction = aggregateFunction;
		this.pairSelector = pairSelector;
	}

	@Override
	public String getName() {
		return aggregateFunction.toString() + "-" + pairSelector.toString() + "s";
	}

	@Override
	public MeasureDirection getMeasureDirection() {
		return MeasureDirection.HIGHER_IS_BETTER;
	}

	@Override
	public <T> Number compute(Similarity<T> similarity, Collection<Pair<T>> pairs) {
		Map<T,List<T>> pairMap = getPairMap(pairs);
		double total = 0;
		for (Map.Entry<T,List<T>> e : pairMap.entrySet()) {
			T key = e.getKey();
			List<T> values = e.getValue();
			total += aggregateFunction.compute(similarity, key, values);
		}
		return total / pairMap.size();
	}

	public <T> Map<T,List<T>> getPairMap(Collection<Pair<T>> pairs) {
		Map<T,List<T>> result = new HashMap<T,List<T>>();
		for (Pair<T> p : pairs) {
			if (pairSelector.has(p)) {
				Collection<T> values = ensure(result, pairSelector.get(p));
				if (pairSelector.hasOther(p)) {
					values.add(pairSelector.other(p));
				}
			}
		}
		return result;
	}
	
	private static <T> List<T> ensure(Map<T,List<T>> map, T key) {
		if (map.containsKey(key)) {
			return map.get(key);
		}
		List<T> result = new ArrayList<T>(2);
		map.put(key, result);
		return result;
	}
}
