package org.bionlpst.util;

/**
 * A location from which items are found in a stream.
 * @author rbossy
 *
 */
public class Location {
	public final String source;
	public final int lineno;

	/**
	 * Creates a new location.
	 * @param source name of the source stream.
	 * @param lineno line number.
	 */
	public Location(String source, int lineno) {
		super();
		this.source = Util.notnull(source);
		this.lineno = lineno;
	}

	/**
	 * Returns the name of the source stream.
	 * @return the name of the source stream
	 */
	public String getSource() {
		return source;
	}

	/**
	 * Returns the line number.
	 * @return the line number.
	 */
	public int getLineno() {
		return lineno;
	}
	
	/**
	 * Composes a message with the name of the source stream and the line number.
	 * The message will have the form "header name:lineno: body".
	 * @param header part of the message before the source.
	 * @param body part of the message after the source.
	 * @return a message with the location.
	 */
	public String getMessage(String header, String body) {
		Util.notnull(body);
		if (lineno < 1) {
			if (header == null) {
				return String.format("%s: %s", source, body);			
			}
			return String.format("%s %s %s", header, source, body);			
		}
		if (header == null) {
			return String.format("%s:%d %s", source, lineno, body);
		}
		return String.format("%s %s:%d %s", header, source, lineno, body);
	}

	/**
	 * Composes a message with the name of the source stream and the line number.
	 * The message will have the form "name:lineno: body".
	 * @param body part of the message after the source.
	 * @return a message with the location.
	 */
	public String getMessage(String body) {
		return getMessage(null, body);
	}
}
