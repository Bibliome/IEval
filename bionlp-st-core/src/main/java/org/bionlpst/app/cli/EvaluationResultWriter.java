package org.bionlpst.app.cli;

import org.bionlpst.corpus.Annotation;
import org.bionlpst.corpus.Document;
import org.bionlpst.evaluation.EvaluationResult;
import org.bionlpst.util.Named;

public interface EvaluationResultWriter {
	void displayEvaluationResult(EvaluationResult<Annotation> eval, boolean detailedEvaluation, double confidence);
	void displayCorpusHeader(Named named, String defaultName);
	void displayDocumentHeader(Document doc);
}
