package org.bionlpst.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;

/**
 * Predicate that accepts or rejects an item.
 * @author rbossy
 *
 * @param <T>
 */
public interface Filter<T> {
	/**
	 * Returns true if the specified item is accepted, false if it is rejected.
	 * @param item item to test.
	 * @return true if the specified item is accepted, false if it is rejected.
	 */
	boolean accept(T item);
	
	void init();
	
	/**
	 * Reduces this filter. May return this filter if it is not reducable.
	 * @return an equivalent reduced form of this filter.
	 */
	Filter<T> reduce();

	/**
	 * Accepts all items.
	 * @author rbossy
	 *
	 * @param <T>
	 */
	public static class AcceptAll<T> implements Filter<T> {
		@Override
		public boolean accept(T item) {
			return true;
		}

		@Override
		public Filter<T> reduce() {
			return this;
		}

		@Override
		public void init() {
		}
	}
	
	/**
	 * Rejects all items.
	 * @author rbossy
	 *
	 * @param <T>
	 */
	public static class RejectAll<T> implements Filter<T> {
		@Override
		public boolean accept(T item) {
			return false;
		}

		@Override
		public Filter<T> reduce() {
			return this;
		}

		@Override
		public void init() {
		}
	}
	
	/**
	 * Inversion decorator class.
	 * Rejects items accepted by the constructor filter, and accepts items rejected by the constructor filter.
	 * @author rbossy
	 *
	 * @param <T>
	 */
	public static class Inverse<T> implements Filter<T> {
		private final Filter<T> filter;

		public Inverse(Filter<T> filter) {
			super();
			this.filter = Util.notnull(filter);
		}

		@Override
		public boolean accept(T item) {
			return !filter.accept(item);
		}

		@Override
		public Filter<T> reduce() {
			Filter<T> filter = this.filter.reduce();
			if (filter instanceof AcceptAll) {
				return new RejectAll<T>();
			}
			if (filter instanceof RejectAll) {
				return new AcceptAll<T>();
			}
			return new Inverse<T>(filter);
		}

		@Override
		public void init() {
			filter.init();
		}
	}

	/**
	 * Abstract class for filters composed with filters.
	 * @author rbossy
	 *
	 * @param <T>
	 */
	public static abstract class CompositeFilter<T> implements Filter<T> {
		private final List<Filter<T>> filters = new ArrayList<Filter<T>>();

		/**
		 * Creates a composite filter.
		 */
		protected CompositeFilter() {
			super();
		}

		/**
		 * Creates a composite filter that contains the specified filters.
		 * @param filters
		 */
		protected CompositeFilter(Collection<Filter<T>> filters) {
			this.filters.addAll(Util.nonenull(filters));
		}

		/**
		 * Returns all the filters in this composite filter.
		 * @return all the filters in this composite filter. The returned list is an unmodifiable view.
		 */
		public List<Filter<T>> getFilters() {
			return Collections.unmodifiableList(filters);
		}
		
		/**
		 * Adds a filter to this composite filter.
		 * @param filter filter to add.
		 */
		public void addFilter(Filter<T> filter) {
			filters.add(Util.notnull(filter));
		}

		@Override
		public void init() {
			for (Filter<T> f : filters) {
				f.init();
			}
		}
	}
	
	/**
	 * A composite filter that accepts items that are accepted by all contained filters.
	 * If one of the filters reject an item, then the item is rejected.
	 * A conjunction behaves like a boolean AND.
	 * @author rbossy
	 *
	 * @param <T>
	 */
	public static class Conjunction<T> extends CompositeFilter<T> {
		public Conjunction() {
			super();
		}

		public Conjunction(Collection<Filter<T>> filters) {
			super(filters);
		}

		@Override
		public boolean accept(T item) {
			for (Filter<T> filter : getFilters()) {
				if (!filter.accept(item)) {
					return false;
				}
			}
			return true;
		}

		@Override
		public Filter<T> reduce() {
			List<Filter<T>> filters = new ArrayList<Filter<T>>(getFilters());
			ListIterator<Filter<T>> it = filters.listIterator();
			while (it.hasNext()) {
				Filter<T> filter = it.next();
				Filter<T> simplified = filter.reduce();
				if (simplified instanceof AcceptAll) {
					it.remove();
					continue;
				}
				if (simplified instanceof RejectAll) {
					return simplified;
				}
				it.set(simplified);
			}
			if (filters.isEmpty()) {
				return new AcceptAll<T>();
			}
			if (filters.size() == 1) {
				return filters.get(0);
			}
			return new Conjunction<T>(filters);
		}
	}
	
	/**
	 * A composite filter that accepts items that are accepted by any of the contained filters.
	 * If one of the filters accept an item, then the item is accepted.
	 * A conjunction behaves like a boolean OR.
	 * @author rbossy
	 *
	 * @param <T>
	 */
	public static class Disjunction<T> extends CompositeFilter<T> {
		public Disjunction() {
			super();
		}

		public Disjunction(Collection<Filter<T>> filters) {
			super(filters);
		}

		@Override
		public boolean accept(T item) {
			for (Filter<T> filter : getFilters()) {
				if (filter.accept(item)) {
					return true;
				}
			}
			return false;
		}

		@Override
		public Filter<T> reduce() {
			List<Filter<T>> filters = new ArrayList<Filter<T>>(getFilters());
			ListIterator<Filter<T>> it = filters.listIterator();
			while (it.hasNext()) {
				Filter<T> filter = it.next();
				Filter<T> simplified = filter.reduce();
				if (simplified instanceof AcceptAll) {
					return simplified;
				}
				if (simplified instanceof RejectAll) {
					it.remove();
					continue;
				}
				it.set(simplified);
			}
			if (filters.isEmpty()) {
				return new RejectAll<T>();
			}
			if (filters.size() == 1) {
				return filters.get(0);
			}
			return new Disjunction<T>(filters);
		}
	}
}
