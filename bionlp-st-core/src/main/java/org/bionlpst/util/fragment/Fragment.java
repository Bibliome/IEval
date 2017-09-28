package org.bionlpst.util.fragment;

/**
 * A fragment is a token in a text.
 * @author rbossy
 *
 */
public interface Fragment {
	/**
	 * Returns the start position.
	 * @return the start position.
	 */
	int getStart();

	/**
	 * Returns the end position.
	 * @return the end position.
	 */
	int getEnd();
}
