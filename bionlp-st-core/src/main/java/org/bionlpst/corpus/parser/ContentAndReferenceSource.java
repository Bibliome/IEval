package org.bionlpst.corpus.parser;

import java.io.IOException;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.util.Named;
import org.bionlpst.util.message.CheckLogger;

public interface ContentAndReferenceSource extends Named {
	void getContentAndReference(CheckLogger logger, Corpus corpus, boolean loadOutput) throws BioNLPSTException, IOException;
	Corpus getContentAndReference(CheckLogger logger, boolean loadOutput) throws BioNLPSTException, IOException;
}