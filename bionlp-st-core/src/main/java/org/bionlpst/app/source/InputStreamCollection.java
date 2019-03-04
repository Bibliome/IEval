package org.bionlpst.app.source;

import java.io.IOException;

public interface InputStreamCollection {
	InputStreamIterator getIterator() throws IOException;
	String getName();
}
