package org.bionlpst.util.fragment;

import java.util.Comparator;

/**
 * Fragment comparators.
 * @author rbossy
 *
 */
public enum FragmentComparator implements Comparator<ImmutableFragment> {
	/**
	 * Order by end position, then by inverse start position.
	 */
	END_INVERSE_START {
		@Override
		public int compare(ImmutableFragment o1, ImmutableFragment o2) {
			if (o1.getEnd() == o2.getEnd()) {
				return Integer.compare(o2.getStart(), o1.getStart());
			}
			return Integer.compare(o1.getEnd(), o2.getEnd());
		}
	},
	
	/**
	 * Order by start position, then by inverse end position.
	 */
	START_INVERSE_END {
		@Override
		public int compare(ImmutableFragment o1, ImmutableFragment o2) {
			if (o1.getStart() == o2.getStart()) {
				return Integer.compare(o2.getEnd(), o1.getEnd());
			}
			return Integer.compare(o1.getStart(), o2.getStart());
		}
	};
}
