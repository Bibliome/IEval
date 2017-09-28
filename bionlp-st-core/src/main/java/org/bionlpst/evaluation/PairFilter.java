package org.bionlpst.evaluation;

import org.bionlpst.util.Filter;

/**
 * Accept pairs when the filter provided in the constructor accepts either one of the items of the pair.
 * @author rbossy
 *
 * @param <T>
 */
public class PairFilter<T> implements Filter<Pair<T>> {
	private final Filter<T> filter;

	public PairFilter(Filter<T> filter) {
		super();
		this.filter = filter;
	}

	@Override
	public boolean accept(Pair<T> item) {
		return (item.hasReference() && filter.accept(item.getReference())) || (item.hasPrediction() && filter.accept(item.getPrediction()));
//		return item.hasReference() && filter.accept(item.getReference());
	}

	@Override
	public Filter<Pair<T>> reduce() {
		Filter<T> filter = this.filter.reduce();
		if (filter instanceof Filter.AcceptAll) {
			return new Filter.AcceptAll<Pair<T>>();
		}
		if (filter instanceof Filter.RejectAll) {
			return new Filter.RejectAll<Pair<T>>();
		}
		return new PairFilter<T>(filter);
	}
}
