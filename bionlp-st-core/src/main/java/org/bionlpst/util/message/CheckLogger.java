package org.bionlpst.util.message;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import org.bionlpst.util.Location;
import org.bionlpst.util.Util;

/**
 * Message container.
 * @author rbossy
 *
 */
public class CheckLogger {
	private final Collection<CheckMessage> messages = new ArrayList<CheckMessage>();
	private CheckMessageLevel highestLevel = null;

	/**
	 * Returns all messages.
	 * @return all messages. The returned collection is an unmodifiable view.
	 */
	public Collection<CheckMessage> getMessages() {
		return Collections.unmodifiableCollection(messages);
	}
	
	/**
	 * Adds a message to this logger.
	 * @param msg message to add.
	 */
	public void addMessage(CheckMessage msg) {
		messages.add(Util.notnull(msg));
		CheckMessageLevel level = msg.getLevel();
		if (highestLevel == null || level.severity > highestLevel.severity) {
			highestLevel = level;
		}
	}

	/**
	 * Returns the highest level found in messages in this logger.
	 * @return the highest level found in messages in this logger. null if there are no messages.
	 */
	public CheckMessageLevel getHighestLevel() {
		return highestLevel;
	}
	
	/**
	 * Removes all messages.
	 */
	public void clear() {
		messages.clear();
		highestLevel = null;
	}
	
	/**
	 * Adds a message to this logger.
	 * @param level level of the message.
	 * @param location location referenced by the message.
	 * @param body message body.
	 */
	public void addMessage(CheckMessageLevel level, Location location, String body) {
		addMessage(new CheckMessage(level, location, body));
	}
	
	/**
	 * Adds an information-level message to this logger.
	 * @param location location referenced by the message.
	 * @param body message body.
	 */
	public void information(Location location, String body) {
		addMessage(new CheckMessage(CheckMessageLevel.INFORMATION, location, body));
	}

	/**
	 * Adds a tolerable-level message to this logger.
	 * @param location location referenced by the message.
	 * @param body message body.
	 */
	public void tolerable(Location location, String body) {
		addMessage(new CheckMessage(CheckMessageLevel.TOLERABLE, location, body));
	}
	
	/**
	 * Adds a suspicious-level message to this logger.
	 * @param location location referenced by the message.
	 * @param body message body.
	 */
	public void suspicious(Location location, String body) {
		addMessage(new CheckMessage(CheckMessageLevel.SUSPICIOUS, location, body));
	}
	
	/**
	 * Adds a serious-level message to this logger.
	 * @param location location referenced by the message.
	 * @param body message body.
	 */
	public void serious(Location location, String body) {
		addMessage(new CheckMessage(CheckMessageLevel.SERIOUS, location, body));
	}
}
