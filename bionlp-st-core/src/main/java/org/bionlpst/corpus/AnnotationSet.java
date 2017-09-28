package org.bionlpst.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.util.Filter;
import org.bionlpst.util.Location;
import org.bionlpst.util.Util;
import org.bionlpst.util.fragment.ImmutableFragment;
import org.bionlpst.util.message.CheckLogger;

/**
 * An annotation set object is a container for annotations.
 * @author rbossy
 *
 */
public class AnnotationSet {
	private final Document document;
	private final AnnotationSetSelector selector;
	private final AnnotationSet parent;
	private final Map<String,Annotation> annotations = new HashMap<String,Annotation>();
	private boolean parsed = false;
	
	/**
	 * Creates a new annotation set.
	 * @param document document to which all annotations in this object belong.
	 * @param parent parent annotation set. Output annotation sets have the input annotation set as parent.
	 * @throws NullPointerException if the specified document is null.
	 */
	public AnnotationSet(Document document, AnnotationSetSelector selector, AnnotationSet parent) throws NullPointerException {
		super();
		if (document == null) {
			throw new NullPointerException();
		}
		this.document = document;
		this.selector = selector;
		this.parent = parent;
		if (parent != null && parent.document != document) {
			throw new BioNLPSTException();
		}
	}

	/**
	 * Returns the document to which belongs this annotation set.
	 * @return the document to which belongs this annotation set.
	 */
	public Document getDocument() {
		return document;
	}

	public AnnotationSetSelector getSelector() {
		return selector;
	}

	/**
	 * Returns the parent annotation set.
	 * @return the parent annotation set. If this annotation set has no parent, then returns null.
	 */
	public AnnotationSet getParent() {
		return parent;
	}
	
	/**
	 * Returns all annotations in this annotation set.
	 * @return all annotations in this annotation set. If there are no annotations in this annotation set then the returned collection is empty. The returned collection is an unmodifiable view.
	 */
	public Collection<Annotation> getAnnotations() {
		return Collections.unmodifiableCollection(annotations.values());
	}

	/**
	 * Collects all annotations in this annotation set that satisfy the specified filter in the specified collection.
	 * @param filter annotation filter.
	 * @param target collection where to store annotations.
	 * @return target.
	 */
	public Collection<Annotation> collectAnnotations(Filter<Annotation> filter, Collection<Annotation> target) {
		return Util.filter(filter, annotations.values(), target);
	}

	/**
	 * Returns all annotations in this annotation set that satisfy the specified filter in the specified collection.
	 * @param filter annotation filter.
	 * @param target collection where to store annotations.
	 * @return all annotations that satisfy the specified filter in the specified collection.
	 */
	public Collection<Annotation> getAnnotations(Filter<Annotation> filter) {
		return Util.filter(filter, annotations.values());
	}
	
	/**
	 * Returns all annotations of the specified types in this annotation set.
	 * @param types types of annotations.
	 * @return all annotations of the specified types in this annotation set.
	 */
	public Collection<Annotation> getAnnotations(String... types) {
		return getAnnotations(new AnnotationTypeFilter<Annotation>(types));
	}
	
	/**
	 * Returns all annotations of the specified types in this annotation set.
	 * @param types types of annotations.
	 * @return all annotations of the specified types in this annotation set.
	 */
	public Collection<Annotation> getAnnotations(Collection<String> types) {
		return getAnnotations(new AnnotationTypeFilter<Annotation>(types));
	}

	/**
	 * Returns true if this annotation set contains an annotation with the specified identifier. This method searches for annotations in this annotation set, then in the parent annotation set.
	 * @param id annotation identifier.
	 * @return true if this annotation set or its parent contains an annotation with the specified identifier.
	 */
	public boolean hasAnnotation(String id) {
		if (annotations.containsKey(id)) {
			return true;
		}
		if (parent == null) {
			return false;
		}
		return parent.hasAnnotation(id);
	}
	
	/**
	 * Returns the annotation with the specified identifier in this annotation set or the parent annotation set.
	 * @param id annotation identifier.
	 * @return the annotation with the specified identifier in this annotation set or the parent annotation set.
	 * @throws BioNLPSTException if neither this annotation set or the parent contains an annotation with the specified identifier.
	 */
	public Annotation getAnnotation(String id) throws BioNLPSTException {
		if (annotations.containsKey(id)) {
			return annotations.get(id);
		}
		if (parent == null) {
			throw new BioNLPSTException("unknown annotation with id: " + id);
		}
		return parent.getAnnotation(id);
	}

	public boolean isParsed() {
		return parsed;
	}

	/**
	 * Adds an annotation to this annotation set. This method is called by the Annotation constructor, it is not meant to be called directly.
	 * @param ann the annotation to add to this annotation set.
	 * @throws BioNLPSTException if either this annotation set or the parent annotation set already contain an annotation with the same identifier as the specified annotation.
	 */
	void addAnnotation(CheckLogger logger, Annotation ann) throws BioNLPSTException {
		String id = ann.getId();
		if (hasAnnotation(id)) {
			Annotation prev = getAnnotation(id);
			logger.serious(ann.getLocation(), "duplicate annotation identifier: " + id + " (" + ann.getLocation().getMessage("") + "/" + ann.getAnnotationSet().selector + ", " + prev.getLocation().getMessage("") + "/" + prev.getAnnotationSet().selector + ") -- we assign a random identifier");
			id = id + "-" + UUID.randomUUID();
			ann.setId(id);
		}
		annotations.put(id, ann);
	}

	/**
	 * Resolve all references of all annotations contained in this annotation set.
	 * @param logger message container where to store warnings and errors.
	 */
	public void resolveReferences(CheckLogger logger) {
		Collection<Annotation> annotations = new ArrayList<Annotation>(this.annotations.values());
		for (Annotation ann : annotations) {
			ann.resolveReferences(logger);
		}
	}

	/**
	 * Parse annotations from the specified stream and adds the annotations in this annotation set. The specified stream must contain annotation specifications in the BioNLP-ST format.
	 * @param logger message container where to store warnings and errors.
	 * @param source name of the source that must follow the BioNLP-ST file name conventions.
	 * @param reader stream to parse.
	 * @throws IOException if there is an I/O error reading the stream.
	 */
	public void parseAnnotations(CheckLogger logger, String source, BufferedReader reader) throws IOException {
		parsed = true;
		int lineno = 0;
		while (true) {
			String line = reader.readLine();
			if (line == null) {
				break;
			}
			lineno++;
			line = line.trim();
			if (line.isEmpty()) {
				continue;
			}
			parseAnnotation(logger, new Location(source, lineno), line);
		}
	}
	
	private void parseAnnotation(CheckLogger logger, Location location, String line) {
		int tab = line.indexOf('\t');
		if (tab == -1) {
			logger.serious(location, "expected tab character");
			return;
		}
		String id = line.substring(0, tab);
		String rest = line.substring(tab+1);
		if (id.equals("*")) {
			if (selector == AnnotationSetSelector.PREDICTION) {
				logger.suspicious(location, "predictions are not supposed to provide equivalences");
			}
			else {
				parseEquivalence(logger, location, rest);
			}
			return;
		}
		char idType = id.charAt(0);
		switch (idType) {
			case 'T':
			case 'W':
				parseTextBound(logger, location, id, rest);
				break;
			case 'R':
			case 'E':
				parseRelation(logger, location, id, rest);
				break;
			case 'N':
				parseNormalization(logger, location, id, rest);
				break;
			case 'M':
				parseModifier(logger, location, id, rest);
				break;
			default:
				logger.serious(location, "unknown annotation kind " + idType);
				new DummyAnnotation(logger, this, location, id, getDummyType(rest));
		}
	}

	private void parseEquivalence(CheckLogger logger, Location location, String rest) {
		if (!rest.startsWith("Equiv ")) {
			logger.serious(location, "ill formed equivalence, expected 'Equiv'");
			return;
		}
		Collection<String> annotationReferences = Util.split(rest.substring(6), ' ');
		new Equivalence(logger, document, location, annotationReferences);
	}

	private static Pattern TEXT_BOUND_PARSER = Pattern.compile("(?<type>\\S+) (?<fragments>\\d+ \\d+(?:;\\d+ \\d+)*)\t(?<control>.*)");

	private void parseTextBound(CheckLogger logger, Location location, String id, String rest) {
		Matcher m = TEXT_BOUND_PARSER.matcher(rest);
		if (!m.matches()) {
			logger.serious(location, "ill formed text-bound annotation: '" + rest + "'");
			new DummyAnnotation(logger, this, location, id, getDummyType(rest));
			return;
		}
		String type = m.group("type");
		String sFragments = m.group("fragments");
		List<ImmutableFragment> fragments = parseFragments(sFragments);
		new TextBound(logger, this, location, id, type, fragments);
	}

	private static List<ImmutableFragment> parseFragments(String sFragments) {
		List<ImmutableFragment> result = new ArrayList<ImmutableFragment>();
		for (String sFrag : Util.split(sFragments, ';')) {
			ImmutableFragment frag = parseFragment(sFrag);
			result.add(frag);
		}
		return result;
	}

	private static ImmutableFragment parseFragment(String sFrag) {
		int space = sFrag.indexOf(' ');
		int start = Integer.parseInt(sFrag.substring(0, space));
		int end = Integer.parseInt(sFrag.substring(space+1));
		return new ImmutableFragment(start, end);
	}

	private static Pattern RELATION_PARSER = Pattern.compile("(?<type>\\S+) (?<args>.+)");
	
	private void parseRelation(CheckLogger logger, Location location, String id, String rest) {
		Matcher m = RELATION_PARSER.matcher(rest);
		if (!m.matches()) {
			logger.serious(location, "ill formed relation, expected type and arguments: '" + rest + "'");
			new DummyAnnotation(logger, this, location, id, getDummyType(rest));
			return;
		}
		String type = m.group("type");
		String sArgs = m.group("args");
		Map<String,String> args = parseArgs(logger, location, sArgs);
		new Relation(logger, this, location, id, type, args);
	}

	private static Pattern ARGUMENT_PARSER = Pattern.compile("\\s*(?<role>[^:]+):(?<ref>\\S+)\\s*");
 
	private static Map<String,String> parseArgs(CheckLogger logger, Location location, String sArgs) {
		Map<String,String> result = new LinkedHashMap<String,String>();
		Matcher m = ARGUMENT_PARSER.matcher(sArgs);
		while (m.find()) {
			String role = m.group("role");
			String ref = m.group("ref");
			if (result.containsKey(role)) {
				logger.suspicious(location, "duplicate argument: " + role);
			}
			result.put(role, ref);
		}
		if (result.isEmpty()) {
			logger.serious(location, "no arguments");
		}
		return result;
	}

	private static final Pattern NORMALIZATION_PARSER = Pattern.compile("(?<type>\\S+) Annotation:(?<ann>\\S+) Referent:(?<ref>\\S+)");
	
	private void parseNormalization(CheckLogger logger, Location location, String id, String rest) {
		Matcher m = NORMALIZATION_PARSER.matcher(rest);
		if (!m.matches()) {
			logger.serious(location, "ill formed normalization, expected type, Annotation and Referent");
			new DummyAnnotation(logger, this, location, id, getDummyType(rest));
			return;
		}
		String type = m.group("type");
		String annRef = m.group("ann");
		String dbRef = m.group("ref");
		new Normalization(logger, this, location, id, type, annRef, dbRef);
	}

	private static final Pattern MODIFIER_PARSER = Pattern.compile("(?<type>\\S+) Annotation:(?<ann>\\S+)");

	private void parseModifier(CheckLogger logger, Location location, String id, String rest) {
		Matcher m = MODIFIER_PARSER.matcher(rest);
		if (!m.matches()) {
			logger.serious(location, "ill formed modfier, expected type and Annotation");
			new DummyAnnotation(logger, this, location, id, getDummyType(rest));
			return;
		}
		String type = m.group("type");
		String annRef = m.group("ann");
		new Modifier(logger, this, location, id, type, annRef);
	}
	
	private static final Pattern DUMMY_TYPE_PARSER = Pattern.compile("(?<type>\\S+)\\s*.*");

	private static String getDummyType(String rest) {
		Matcher m = DUMMY_TYPE_PARSER.matcher(rest);
		if (m.matches()) {
			return m.group("type");
		}
		return DummyAnnotation.DUMMY_TYPE;
	}
	
	public void removeAnnotation(String id) {
		annotations.remove(id);
	}
}
