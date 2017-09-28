package org.bionlpst.corpus;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.message.CheckLogger;

/**
 * A BioNLP-ST annotation.
 * @author rbossy
 *
 */
public abstract class Annotation {
	private final AnnotationSet annotationSet;
	private final Location location;
	private String id;
	private final String type;
	private final Collection<Annotation> backReferences = new HashSet<Annotation>();
	private Equivalence equivalence = null;

	/**
	 * Creates a new annotation.
	 * @param annotationSet annotation set to which belongs this annotation.
	 * @param location location where this annotation has been read.
	 * @param id identifier of this annotation.
	 * @param type type of this annotation.
	 * @throws BioNLPSTException if either the specified annotation set or its parent already contain an annotation with the same identifier as this annotation.
	 * @throws NullPointerException if one of the specified parameters is null.
	 */
	protected Annotation(CheckLogger logger, AnnotationSet annotationSet, Location location, String id, String type) throws BioNLPSTException, NullPointerException {
		super();
		this.annotationSet = Util.notnull(annotationSet);
		this.location = Util.notnull(location);
		this.id = Util.notnull(id);
		this.type = Util.notnull(type);
		annotationSet.addAnnotation(logger, this);
	}

	/**
	 * Returns the annotation set to which this annotation belongs.
	 * @return the annotation set to which this annotation belongs.
	 */
	public AnnotationSet getAnnotationSet() {
		return annotationSet;
	}

	/**
	 * Returns the document to which belongs this annotation. This method is equivalent to getAnnotationSet().getDocument().
	 * @return the document to which belongs this annotation.
	 */
	public Document getDocument() {
		return annotationSet.getDocument();
	}

	/**
	 * Returns the location where this annotation has been read.
	 * @return the location where this annotation has been read.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Returns the identifier of this annotation.
	 * @return the identifier of this annotation.
	 */
	public String getId() {
		return id;
	}

	void setId(String id) {
		this.id = id;
	}

	/**
	 * Returns the type of this annotation.
	 * @return the type of this annotation.
	 */
	public String getType() {
		return type;
	}
	
	/**
	 * Returns the equivalence that contains this annotation.
	 * @return the equivalence that contains this annotation. Returns null if this annotation is not in any equivalence.
	 */
	public Equivalence getEquivalence() {
		return equivalence;
	}
	
	public Collection<Annotation> getEquivalents() {
		if (equivalence == null) {
			return Collections.singleton(this);
		}
		return equivalence.getAnnotations();
	}

	/**
	 * Sets the equivalence for this annotation. This method is called by {@link Equivalence#resolveReferences(CheckLogger)}, it is not meant to be called directly.
	 * @param equivalence the equivalence.
	 */
	void setEquivalence(Equivalence equivalence) {
		this.equivalence = equivalence;
	}

	/**
	 * Returns all annotations that reference this annotation.
	 * @return all annotations that reference this annotation. If there are no annotation that references this annotation, then the returned collection is empty. The returned collection is an unmodifiable view.
	 */
	public Collection<Annotation> getBackReferences() {
		return Collections.unmodifiableCollection(backReferences);
	}
	
	private void addBackReference(Annotation ann) {
		this.backReferences.add(ann);
	}

	/**
	 * Returns the annotation with the specified identifier in the same annotation set as this annotation. This annotation is added in the result annotation back references. If there is no annotation with the specified identifier, then a dummy annotation is returned.
	 * @param logger message container where to store warnings and errors.
	 * @param id reference identifier.
	 * @return the annotation with the specified identifier in the same annotation set as this annotation.
	 */
	protected Annotation resolveReference(CheckLogger logger, String id) {
		if (annotationSet.hasAnnotation(id)) {
			Annotation result = annotationSet.getAnnotation(id);
			result.addBackReference(this);
			return result;
		}
		logger.serious(location, "cannot resolve annotation reference " + id);
		return new DummyAnnotation(logger, annotationSet, getLocation(), id);
	}

	/**
	 * Resolve all references in this annotation.
	 * @param logger message container where to store warnings and errors.
	 */
	public abstract void resolveReferences(CheckLogger logger);
	
	@Override
	public String toString() {
		return String.format("%s:%s", id, type);
	}

	/**
	 * Runs the specified visitor.
	 * @param visitor the visitor.
	 * @param param visitor parameter.
	 * @return visitor result.
	 */
	public abstract <R,P> R accept(AnnotationVisitor<R,P> visitor, P param);
	
	/**
	 * Returns this annotation kind.
	 * @return this annotation kind.
	 */
	public abstract AnnotationKind getKind();

	/**
	 * Casts this annotation to {@link TextBound}.
	 * @return this object if it is a text bound, null otherwise.
	 */
	public abstract TextBound asTextBound();

	/**
	 * Casts this annotation to {@link Relation}.
	 * @return this object if it is a relation, null otherwise.
	 */
	public abstract Relation asRelation();

	public abstract SingleReferenceAnnotation asSingleReferenceAnnotation();
	
	/**
	 * Casts this annotation to {@link Modifier}.
	 * @return this object if it is a modifier, null otherwise.
	 */
	public abstract Modifier asModifier();

	/**
	 * Casts this annotation to {@link Normalization}.
	 * @return this object if it is a normalization, null otherwise.
	 */
	public abstract Normalization asNormalization();
}
