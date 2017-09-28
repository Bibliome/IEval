package org.bionlpst.schema;

import org.bionlpst.util.message.CheckLogger;

/**
 * A schema checks an object and fills a message container with errors and warnings.
 * @author rbossy
 *
 * @param <T>
 */
public interface Schema<T> {
	/**
	 * Check the specified item.
	 * @param logger message container where to store warnings and errors.
	 * @param item item to check.
	 */
	void check(CheckLogger logger, T item);
	
	/**
	 * Reduces this schema.
	 * @return a reduced equivalent schema.
	 */
	Schema<T> reduce();
}
