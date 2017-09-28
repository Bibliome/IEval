package org.bionlpst.schema.lib;

import org.bionlpst.corpus.Corpus;
import org.bionlpst.corpus.Document;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

/**
 * Schema that checks each document of a corpus against a document schema.
 * @author rbossy
 *
 */
public class BasicCorpusSchema implements Schema<Corpus> {
	private final Schema<Document> documentSchema;

	public BasicCorpusSchema(Schema<Document> documentSchema) {
		super();
		this.documentSchema = documentSchema;
	}

	public Schema<Document> getDocumentSchema() {
		return documentSchema;
	}

	@Override
	public void check(CheckLogger logger, Corpus item) {
		for (Document doc : item.getDocuments()) {
			documentSchema.check(logger, doc);
		}
	}

	@Override
	public Schema<Corpus> reduce() {
		return new BasicCorpusSchema(documentSchema.reduce());
	}
}
