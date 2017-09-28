package org.bionlpst.schema;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.bionlpst.util.message.CheckLogger;

/**
 * A composite schema contains several schemas.
 * Checking an item checks this item against each contained schema.
 * @author rbossy
 *
 * @param <T>
 */
public class CompositeSchema<T> implements Schema<T> {
	private final List<Schema<T>> schemas = new ArrayList<Schema<T>>();

	/**
	 * Creates a new empty composite schema.
	 */
	public CompositeSchema() {
		super();
	}
	
	/**
	 * Creates a new composite schema containing the specified schemas.
	 * @param schemas
	 */
	public CompositeSchema(Collection<Schema<T>> schemas) {
		super();
		this.schemas.addAll(schemas);
	}
	
	/**
	 * Adds a schema into this composite schema.
	 * @param schema schema to add.
	 */
	public void addCompound(Schema<T> schema) {
		schemas.add(schema);
	}

	@Override
	public void check(CheckLogger logger, T item) {
		for (Schema<T> schema : schemas) {
			schema.check(logger, item);
		}
	}

	@Override
	public Schema<T> reduce() {
		if (schemas.isEmpty()) {
			return new NullSchema<T>().reduce();
		}
		if (schemas.size() == 1) {
			return schemas.get(0).reduce();
		}
		CompositeSchema<T> result = new CompositeSchema<T>();
		for (Schema<T> schema : schemas) {
			result.addCompound(schema.reduce());
		}
		return result;
	}
}
