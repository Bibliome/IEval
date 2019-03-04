package org.bionlpst.corpus.parser;

import org.bionlpst.BioNLPSTException;

@SuppressWarnings("serial")
public class BioNLPSTParseException extends BioNLPSTException {
	public BioNLPSTParseException() {
		super();
	}

	public BioNLPSTParseException(String message, Throwable cause) {
		super(message, cause);
	}

	public BioNLPSTParseException(String message) {
		super(message);
	}

	public BioNLPSTParseException(Throwable cause) {
		super(cause);
	}
}
