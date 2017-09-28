package org.bionlpst.util.message;

/**
 * Message severity level.
 * @author rbossy
 *
 */
public enum CheckMessageLevel {
	/**
	 * Serious message: will have unexpected consequences.
	 */
	SERIOUS(1000),
	
	/**
	 * Suspicious message: may have unexpected consequences.
	 */
	SUSPICIOUS(100),
	
	/**
	 * Tolerable message: will not have unexpected consequences, but may point problems in user input.
	 */
	TOLERABLE(10),
	
	/**
	 * Information message: totally okay.
	 */
	INFORMATION(1);
	
	/**
	 * Severity level. The highest the more severe.
	 */
	public final int severity;

	private CheckMessageLevel(int severity) {
		this.severity = severity;
	}
}
