package org.bionlpst.schema;

import org.bionlpst.util.message.CheckLogger;

/**
 * The null schema does nothing.
 * @author rbossy
 *
 * @param <T>
 */
public class NullSchema<T> implements Schema<T> {
	@Override
	public void check(CheckLogger logger, T item) {
	}

	@Override
	public Schema<T> reduce() {
		return this;
	}
}
