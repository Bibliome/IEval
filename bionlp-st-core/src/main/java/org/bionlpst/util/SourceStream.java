package org.bionlpst.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;

public abstract class SourceStream {
	protected SourceStream() {
		super();
	}
	
	/**
	 * Opens an input stream.
	 * @return an input stream.
	 * @throws IOException
	 */
	public abstract InputStream open() throws IOException;
	
	/**
	 * Opens a reader.
	 * @param name name of the stream.
	 * @return a reader.
	 * @throws IOException
	 */
	public Reader openReader() throws IOException {
		return new InputStreamReader(open());
	}

	/**
	 * Opens a buffered reader.
	 * @param name name of the stream.
	 * @return a buffered reader.
	 * @throws IOException
	 */
	public BufferedReader openBufferedReader() throws IOException {
		return new BufferedReader(openReader());
	}
	
	public abstract String getName();
}
