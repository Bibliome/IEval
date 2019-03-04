package org.bionlpst.app.source;

import java.io.IOException;

import org.bionlpst.util.Named;

public interface InputStreamCollection extends Named {
	InputStreamIterator getIterator() throws IOException;
}
