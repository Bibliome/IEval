package org.bionlpst.schema.lib;

import java.util.List;

import org.bionlpst.corpus.TextBound;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.fragment.ImmutableFragment;
import org.bionlpst.util.message.CheckLogger;

/**
 * Schema that checks the number of fragments of the checked text bound annotation.
 * @author rbossy
 *
 */
public class FragmentCardinalityTextBoundSchema implements Schema<TextBound> {
	private final int maxFragments;

	/**
	 * 
	 * @param maxFragments maximum number of fragments expected.
	 */
	public FragmentCardinalityTextBoundSchema(int maxFragments) {
		super();
		this.maxFragments = maxFragments;
	}

	public FragmentCardinalityTextBoundSchema() {
		this(Integer.MAX_VALUE);
	}
	
	@Override
	public void check(CheckLogger logger, TextBound item) {
		List<ImmutableFragment> fragments = item.getFragments();
		if (fragments.size() > maxFragments) {
			logger.serious(item.getLocation(), "text-bound has too many fragments, max: " + maxFragments);
		}
	}

	@Override
	public Schema<TextBound> reduce() {
		return this;
	}
}
