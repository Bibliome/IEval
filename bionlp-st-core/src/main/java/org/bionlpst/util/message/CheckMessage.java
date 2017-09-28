package org.bionlpst.util.message;

import org.bionlpst.util.Location;
import org.bionlpst.util.Util;

/**
 * An individual message.
 * @author rbossy
 *
 */
public class CheckMessage {
	private final CheckMessageLevel level;
	private final Location location;
	private final String body;
	
	/**
	 * Creates a new message.
	 * @param level level of the message.
	 * @param location location referenced by the message.
	 * @param body message body.
	 */
	public CheckMessage(CheckMessageLevel level, Location location, String body) {
		super();
		this.level = Util.notnull(level);
		this.location = Util.notnull(location);
		this.body = Util.notnull(body);
	}

	/**
	 * Returns this message level.
	 * @return this message level.
	 */
	public CheckMessageLevel getLevel() {
		return level;
	}

	/**
	 * Returns this message location.
	 * @return this message location.
	 */
	public Location getLocation() {
		return location;
	}

	/**
	 * Returns this message body.
	 * @return this message body.
	 */
	public String getBody() {
		return body;
	}
	
	/**
	 * Composes a message with the level, location and body of this message.
	 * @return message text.
	 */
	public String getCompleteMessage() {
		return location.getMessage(level.toString(), body);
	}
}
