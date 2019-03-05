package org.bionlpst.corpus.source.bionlpst;

import java.io.IOException;

import org.bionlpst.util.Named;

public interface InputStreamCollection extends Named {
	InputStreamIterator getIterator() throws IOException;
}
