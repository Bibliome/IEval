package org.bionlpst.corpus.source.pubannotation;

import java.io.IOException;
import java.io.InputStream;

import org.bionlpst.util.Named;

public interface InputStreamFactory extends Named {
	InputStream getInputStream() throws IOException;
}
