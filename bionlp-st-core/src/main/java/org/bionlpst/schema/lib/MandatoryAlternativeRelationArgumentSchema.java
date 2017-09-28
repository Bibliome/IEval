package org.bionlpst.schema.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.bionlpst.corpus.Relation;
import org.bionlpst.schema.NullSchema;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.Util;
import org.bionlpst.util.message.CheckLogger;

public class MandatoryAlternativeRelationArgumentSchema implements Schema<Relation> {
	private final boolean exclusive;
	private final Collection<String> alternativeRoles = new LinkedHashSet<String>();

	public MandatoryAlternativeRelationArgumentSchema(boolean exclusive, Collection<String> alternativeRoles) {
		super();
		this.exclusive = exclusive;
		this.alternativeRoles.addAll(alternativeRoles);
	}
	public MandatoryAlternativeRelationArgumentSchema(boolean exclusive, String... alternativeRoles) {
		this(exclusive, Arrays.asList(alternativeRoles));
	}
		
	public void addAlternativeRole(String role) {
		alternativeRoles.add(role);
	}

	@Override
	public void check(CheckLogger logger, Relation item) {
		String found = null;
		for (String role : item.getRoles()) {
			if (alternativeRoles.contains(role)) {
				if (exclusive) {
					if (found == null) {
						found = role;
					}
					else {
						logger.serious(item.getLocation(), "Mutually exclusive arguments: " + found + " / " + role);
					}
				}
				else {
					return;
				}
			}
		}
		if (found == null) {
			logger.serious(item.getLocation(), "missing one of mandatory arguments: " + Util.join(alternativeRoles, ", "));
		}
	}

	@Override
	public Schema<Relation> reduce() {
		if (alternativeRoles.isEmpty()) {
			return new NullSchema<Relation>().reduce();
		}
		return this;
	}
	
	
}
