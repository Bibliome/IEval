package org.bionlpst.corpus.parser;

import java.io.IOException;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.util.Named;
import org.bionlpst.util.message.CheckLogger;

public interface CorpusAndReferenceParser extends Named {
	void getCorpusAndReference(CheckLogger logger, Corpus corpus, boolean loadOutput) throws BioNLPSTException, IOException;
	Corpus getCorpusAndReference(CheckLogger logger, boolean loadOutput) throws BioNLPSTException, IOException;
}