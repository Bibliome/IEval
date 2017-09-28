package org.bionlpst.app.source;

import java.io.IOException;
import java.io.InputStream;

public interface EntryIterator extends AutoCloseable {
	boolean next() throws IOException;
	void closeEntry() throws IOException;
	String getName();
	InputStream getContents() throws IOException;
}
