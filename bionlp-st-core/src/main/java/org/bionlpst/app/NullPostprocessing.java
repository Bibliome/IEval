package org.bionlpst.app;

import org.bionlpst.corpus.Corpus;

public enum NullPostprocessing implements CorpusPostprocessing {
	INSTANCE;

	@Override
	public void postprocess(Corpus corpus) {
	}
}
