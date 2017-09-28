package org.bionlpst.schema.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.bionlpst.corpus.Relation;
import org.bionlpst.schema.NullSchema;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

/**
 * Schema that checks a relation has all mandatory arguments.
 * @author rbossy
 *
 */
public class MandatoryRelationArgumentsSchema implements Schema<Relation> {
	private final Collection<String> mandatoryRoles = new HashSet<String>();
	
	public MandatoryRelationArgumentsSchema(Collection<String> mandatoryRoles) {
		super();
		this.mandatoryRoles.addAll(mandatoryRoles);
	}

	public MandatoryRelationArgumentsSchema(String... mandatoryRoles) {
		this(Arrays.asList(mandatoryRoles));
	}

	public void addMandatoryRole(String role) {
		mandatoryRoles.add(role);
	}

	@Override
	public void check(CheckLogger logger, Relation item) {
		for (String role : mandatoryRoles) {
			if (!item.hasArgument(role)) {
				logger.serious(item.getLocation(), "missing mandatory argument " + role);
			}
		}
	}

	@Override
	public Schema<Relation> reduce() {
		if (mandatoryRoles.isEmpty()) {
			return new NullSchema<Relation>().reduce();
		}
		return this;
	}
}
