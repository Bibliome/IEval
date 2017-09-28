package org.bionlpst.schema.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.bionlpst.corpus.Relation;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

/**
 * Checks that each argument in a relation has an allowed role.
 * @author rbossy
 *
 */
public class AllowedRelationArgumentsSchema implements Schema<Relation> {
	private final Collection<String> allowedRoles = new HashSet<String>();
	
	public AllowedRelationArgumentsSchema(String... allowedRoles) {
		this(Arrays.asList(allowedRoles));
	}
	
	public AllowedRelationArgumentsSchema(Collection<String> allowedRoles) {
		super();
		this.allowedRoles.addAll(allowedRoles);
	}

	public void addAllowedRole(String role) {
		if (allowedRoles.contains(role)) {
			throw new RuntimeException();
		}
		allowedRoles.add(role);
	}

	@Override
	public void check(CheckLogger logger, Relation item) {
		for (String role : item.getRoles()) {
			if (!allowedRoles.contains(role)) {
				logger.serious(item.getLocation(), "illegal argument role " + role);
			}
		}
	}

	@Override
	public Schema<Relation> reduce() {
		return this;
	}
}
