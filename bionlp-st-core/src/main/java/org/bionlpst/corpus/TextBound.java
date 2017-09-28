package org.bionlpst.corpus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;

import org.bionlpst.util.Location;
import org.bionlpst.util.fragment.Fragment;
import org.bionlpst.util.fragment.FragmentComparator;
import org.bionlpst.util.fragment.ImmutableFragment;
import org.bionlpst.util.message.CheckLogger;

/**
 * Text bound annotations.
 * @author rbossy
 *
 */
public class TextBound extends Annotation implements Fragment {
	private final List<ImmutableFragment> fragments;


	/**
	 * Creates a new text bound annotation.
	 * @param logger message container where to store warnings and errors.
	 * @param annotationSet annotation set to which belongs this annotation.
	 * @param location location where this annotation has been read.
	 * @param id identifier of this annotation.
	 * @param type type of this annotation.
	 * @param fragments this text bound annotation position.
	 * @throws NullPointerException if one of the specified parameters is null.
	 */
	public TextBound(CheckLogger logger, AnnotationSet annotationSet, Location location, String id, String type, List<ImmutableFragment> fragments) throws NullPointerException {
		super(logger, annotationSet, location, id, type);
		if (fragments == null) {
			throw new NullPointerException();
		}
		this.fragments = new ArrayList<ImmutableFragment>(fragments);
		if (fragments.isEmpty()) {
			logger.serious(location, "no fragments");
			this.fragments.add(new ImmutableFragment(0, 0));
			return;
		}
		int prevStart = Integer.MIN_VALUE;
		for (ImmutableFragment frag : fragments) {
			if (frag.getStart() < prevStart) {
				logger.suspicious(location, "fragments not in order");
				break;
			}
			prevStart = frag.getStart();
		}
		Collections.sort(this.fragments, FragmentComparator.START_INVERSE_END);
		Iterator<ImmutableFragment> fragIt = this.fragments.iterator();
		int docLen = annotationSet.getDocument().getContents().length();
		int reach = -1;
		while (fragIt.hasNext()) {
			ImmutableFragment frag = fragIt.next();
			if (frag.getEnd() > docLen || frag.getStart() < 0) {
				logger.serious(location, "overreaching fragment");
				fragIt.remove();
				continue;
			}
			if (frag.getStart() <= reach) {
				logger.serious(location, "overlapping fragments: " + this.fragments);
				fragIt.remove();
				continue;
			}
			reach = Math.max(reach, frag.getEnd());
		}
	}

	/**
	 * Returns this text bound annotation fragments.
	 * @return this text bound annotation fragments. The returned list is an unmodifiable view.
	 */
	public List<ImmutableFragment> getFragments() {
		return Collections.unmodifiableList(fragments);
	}

	/**
	 * Returns the sum of the length of all fragments in this text bound annotation.
	 * @return the sum of the length of all fragments in this text bound annotation.
	 */
	public int getLength() {
		int result = 0;
		for (ImmutableFragment frag : fragments) {
			result += frag.getEnd() - frag.getStart();
		}
		return result;
	}
	
	public void getForm(StringBuilder sb, String separator) {
		Document doc = getDocument();
		String contents = doc.getContents();
		boolean notFirst = false;
		for (Fragment frag : fragments) {
			if (notFirst) {
				sb.append(separator);
			}
			else {
				notFirst = true;
			}
			sb.append(contents.substring(frag.getStart(), frag.getEnd()));
		}
	}
	
	public void getForm(StringBuilder sb) {
		getForm(sb, " ");
	}
	
	public String getForm(String separator) {
		StringBuilder sb = new StringBuilder();
		getForm(sb, separator);
		return sb.toString();
	}
	
	public String getForm() {
		StringBuilder sb = new StringBuilder();
		getForm(sb);
		return sb.toString();
	}
	
	@Override
	public int getStart() {
		return fragments.get(0).getStart();
	}
	
	@Override
	public int getEnd() {
		return fragments.get(fragments.size() - 1).getEnd();
	}

	@Override
	public void resolveReferences(CheckLogger logger) {
	}

	@Override
	public <R,P> R accept(AnnotationVisitor<R,P> visitor, P param) {
		return visitor.visit(this, param);
	}

	@Override
	public AnnotationKind getKind() {
		return AnnotationKind.TEXT_BOUND;
	}

	@Override
	public TextBound asTextBound() {
		return this;
	}

	@Override
	public Relation asRelation() {
		return null;
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
}
