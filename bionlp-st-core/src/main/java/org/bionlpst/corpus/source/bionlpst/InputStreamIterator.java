package org.bionlpst.corpus.source.bionlpst;

import java.io.IOException;
import java.io.InputStream;

public interface InputStreamIterator extends AutoCloseable {
	boolean next() throws IOException;
	void closeEntry() throws IOException;
	String getName();
	InputStream getContents() throws IOException;
}
