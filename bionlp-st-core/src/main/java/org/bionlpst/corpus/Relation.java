package org.bionlpst.corpus;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.util.Location;
import org.bionlpst.util.message.CheckLogger;

/**
 * Relation annotations.
 * @author rbossy
 *
 */
public class Relation extends Annotation {
	private final Map<String,String> argumentReferences;
	private final Map<String,Annotation> arguments = new LinkedHashMap<String,Annotation>();
	
	/**
	 * Creates a new relation.
	 * @param annotationSet annotation set to which belongs this annotation.
	 * @param location location where this annotation has been read.
	 * @param id identifier of this annotation.
	 * @param type type of this annotation.
	 * @param argumentReferences argument references, the map keys are the roles, the map values are the argument references.
	 * @throws BioNLPSTException if either the specified annotation set or its parent already contain an annotation with the same identifier as this annotation.
	 * @throws NullPointerException if one of the specified parameters is null.
	 */
	public Relation(CheckLogger logger, AnnotationSet annotationSet, Location location, String id, String type, Map<String,String> argumentReferences) {
		super(logger, annotationSet, location, id, type);
		this.argumentReferences = new HashMap<String,String>(argumentReferences);
	}

	@Override
	public void resolveReferences(CheckLogger logger) throws BioNLPSTException {
		for (Map.Entry<String,String> e : argumentReferences.entrySet()) {
			arguments.put(e.getKey(), resolveReference(logger, e.getValue()));
		}
	}

	/**
	 * Returns the arguments of this relation with their roles.
	 * @return the arguments of this relation with their roles. The returned map is an unmodifiable view.
	 * @throws IllegalStateException if references are not resolved.
	 */
	public Map<String,Annotation> getArgumentMap() throws IllegalStateException {
		if (arguments.isEmpty()) {
			throw new IllegalStateException("references were not resolved");
		}
		return Collections.unmodifiableMap(arguments);
	}
	
	/**
	 * Returns the arguments of this relation.
	 * @return the arguments of this relation. The returned collection is an unmodifiable view.
	 * @throws IllegalStateException if references are not resolved.
	 */
	public Collection<Annotation> getArguments() throws IllegalStateException {
		if (arguments.isEmpty()) {
			throw new IllegalStateException("references were not resolved");
		}
		return Collections.unmodifiableCollection(arguments.values());
	}
	
	/**
	 * Returns the roles of arguments of this relation.
	 * @return the roles of arguments of this relation. The returned collection is an unmodifiable view.
	 * @throws IllegalStateException if references are not resolved.
	 */
	public Collection<String> getRoles() throws IllegalStateException {
		if (arguments.isEmpty()) {
			throw new IllegalStateException("references were not resolved");
		}
		return Collections.unmodifiableCollection(arguments.keySet());
	}
	
	/**
	 * Returns the argument in this relation with the specified role.
	 * @param role the argument role.
	 * @return the argument in this relation with the specified role.
	 * @throws BioNLPSTException if this relation has no argument with the specified role.
	 * @throws IllegalStateException if references are not resolved.
	 */
	public Annotation getArgument(String role) throws BioNLPSTException, IllegalStateException {
		if (arguments.isEmpty()) {
			throw new IllegalStateException("references were not resolved");
		}
		if (arguments.containsKey(role)) {
			return arguments.get(role);
		}
		throw new BioNLPSTException("relation " + getId() + " has no argument " + role);
	}

	/**
	 * Returns true if this relation has an argument with the specified role.
	 * @param role the argument role.
	 * @return true if this relation has an argument with the specified role.
	 * @throws IllegalStateException if references are not resolved.
	 */
	public boolean hasArgument(String role) throws IllegalStateException {
		if (arguments.isEmpty()) {
			throw new IllegalStateException("references were not resolved");
		}
		return arguments.containsKey(role);
	}

	@Override
	public <R,P> R accept(AnnotationVisitor<R,P> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public AnnotationKind getKind() {
		return AnnotationKind.RELATION;
	}

	@Override
	public TextBound asTextBound() {
		return null;
	}

	@Override
	public Relation asRelation() {
		return this;
	}

	@Override
	public SingleReferenceAnnotation asSingleReferenceAnnotation() {
		return null;
	}

	@Override
	public Modifier asModifier() {
		return null;
	}

	@Override
	public Normalization asNormalization() {
		return null;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append(super.toString());
		sb.append(" in ");
		sb.append(getDocument().getId());
		sb.append(" {");
		for (Map.Entry<String,Annotation> e : arguments.entrySet()) {
			sb.append(' ');
			sb.append(e.getKey());
			sb.append(':');
			sb.append(e.getValue().toString());
		}
		sb.append(" }");
		return sb.toString();
	}
}
