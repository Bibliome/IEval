package org.bionlpst.schema.lib;

import java.util.Collection;

import org.bionlpst.corpus.Normalization;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

/**
 * Schema that checks the referent of the checked normalization is in allowed values.
 * @author rbossy
 *
 */
public class NormalizationVocabularySchema implements Schema<Normalization> {
	private final Collection<String> allowedValues;

	public NormalizationVocabularySchema(Collection<String> allowedValues) {
		super();
		this.allowedValues = allowedValues;
	}

	@Override
	public void check(CheckLogger logger, Normalization item) {
		String value = item.getReferent();
		if (!allowedValues.contains(value)) {
			logger.serious(item.getLocation(), "referent " + value + " is not in the vocabulary");
		}
	}

	@Override
	public Schema<Normalization> reduce() {
		return this;
	}
}
