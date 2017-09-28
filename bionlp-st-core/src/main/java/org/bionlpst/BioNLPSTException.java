package org.bionlpst;


@SuppressWarnings("serial")
public class BioNLPSTException extends RuntimeException {
	public BioNLPSTException() {
		super();
	}

	public BioNLPSTException(String message, Throwable cause) {
		super(message, cause);
	}

	public BioNLPSTException(String message) {
		super(message);
	}

	public BioNLPSTException(Throwable cause) {
		super(cause);
	}
}
