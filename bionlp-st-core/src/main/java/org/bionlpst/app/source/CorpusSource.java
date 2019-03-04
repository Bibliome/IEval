package org.bionlpst.app.source;

import java.io.IOException;

public interface CorpusSource {
	EntryIterator getEntries() throws IOException;
	String getName();
}
