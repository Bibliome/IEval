package org.bionlpst.util.fragment;

/**
 * Immutable fragment.
 * @author rbossy
 *
 */
public class ImmutableFragment implements Fragment {
	private final int start;
	private final int end;

	/**
	 * Creates a new immutable fragment.
	 * @param start start position.
	 * @param end end position.
	 * @throws IllegalArgumentException if start < 0, or end < start.
	 */
	public ImmutableFragment(int start, int end) throws IllegalArgumentException {
		super();
		if (start < 0) {
			throw new IllegalArgumentException();
		}
		if (end < start) {
			throw new IllegalArgumentException();
		}
		this.start = start;
		this.end = end;
	}

	@Override
	public int getStart() {
		return start;
	}

	@Override
	public int getEnd() {
		return end;
	}

	/**
	 * Returns end - start.
	 * @return end - start.
	 */
	public int getLength() {
		return end - start;
	}

	@Override
	public String toString() {
		return String.format("%d-%d", start, end);
	}
}
