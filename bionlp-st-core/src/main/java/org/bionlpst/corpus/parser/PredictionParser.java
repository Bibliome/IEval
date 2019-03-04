package org.bionlpst.corpus.parser;

import java.io.IOException;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.corpus.Corpus;
import org.bionlpst.util.Named;
import org.bionlpst.util.message.CheckLogger;

public interface PredictionParser extends Named {
	void getPredictions(CheckLogger logger, Corpus corpus) throws BioNLPSTException, IOException;
}