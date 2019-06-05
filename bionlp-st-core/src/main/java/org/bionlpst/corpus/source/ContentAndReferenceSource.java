package org.bionlpst.corpus.source;

import java.io.IOException;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.util.Named;
import org.bionlpst.util.message.CheckLogger;

public interface ContentAndReferenceSource extends Named {
	void fillContentAndReference(CheckLogger logger, Corpus corpus, boolean loadOutput) throws BioNLPSTException, IOException;
	Corpus fillContentAndReference(CheckLogger logger, boolean loadOutput) throws BioNLPSTException, IOException;
}