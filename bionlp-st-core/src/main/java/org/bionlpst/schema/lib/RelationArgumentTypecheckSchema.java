package org.bionlpst.schema.lib;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.AnnotationKind;
import org.bionlpst.corpus.DummyAnnotation;
import org.bionlpst.corpus.Relation;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

/**
 * Schema that checks the type of an argument of the checked relation.
 * @author rbossy
 *
 */
public class RelationArgumentTypecheckSchema implements Schema<Relation> {
	private final String role;
	private final Collection<String> allowedTypes = new HashSet<String>();

	public RelationArgumentTypecheckSchema(String role) {
		super();
		this.role = role;
	}

	public RelationArgumentTypecheckSchema(String role, Collection<String> allowedTypes) {
		super();
		this.role = role;
		this.allowedTypes.addAll(allowedTypes);
	}

	public RelationArgumentTypecheckSchema(String role, String... allowedTypes) {
		this(role, Arrays.asList(allowedTypes));
	}
	
	public void addAllowedType(String type) {
		allowedTypes.add(type);
	}

	@Override
	public void check(CheckLogger logger, Relation item) {
		if (!item.hasArgument(role)) {
			return;
		}
		Annotation arg = item.getArgument(role);
		String type = arg.getType();
		if (arg.getKind() == AnnotationKind.DUMMY && type.equals(DummyAnnotation.DUMMY_TYPE)) {
			return;
		}
		if (!allowedTypes.contains(type)) {
			logger.serious(item.getLocation(), "type error for argument " + role + " [" + arg.getLocation().getMessage(type) + "]");
		}
	}

	@Override
	public Schema<Relation> reduce() {
		return this;
	}
}
