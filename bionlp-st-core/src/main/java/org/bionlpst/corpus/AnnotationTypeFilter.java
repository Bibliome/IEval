package org.bionlpst.corpus;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.bionlpst.util.Filter;

/**
 * Filter of annotations by their types.
 * @author rbossy
 *
 * @param <T> subclass of annotation.
 */
public class AnnotationTypeFilter<T extends Annotation> implements Filter<T> {
	private final Collection<String> types = new HashSet<String>();
	
	/**
	 * Creates a new annotation type filter.
	 * @param types accepted types.
	 */
	public AnnotationTypeFilter(Collection<String> types) {
		this.types.addAll(types);
	}
	
	/**
	 * Creates a new annotation type filter.
	 * @param types accepted types.
	 */
	public AnnotationTypeFilter(String... types) {
		this(Arrays.asList(types));
	}

	/**
	 * Adds the specified allowed type.
	 * @param type allowed type.
	 */
	public void addType(String type) {
		types.add(type);
	}

	/**
	 * Returns the allowed types.
	 * @return the allowed types.
	 */
	public Collection<String> getTypes() {
		return Collections.unmodifiableCollection(types);
	}

	@Override
	public boolean accept(T item) {
		return types.contains(item.getType());
	}

	@Override
	public Filter<T> reduce() {
		if (types.isEmpty()) {
			return new Filter.RejectAll<T>();
		}
		return this;
	}

	@Override
	public void init() {
	}
}
