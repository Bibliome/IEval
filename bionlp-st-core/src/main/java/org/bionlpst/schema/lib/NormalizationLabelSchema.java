package org.bionlpst.schema.lib;

import java.util.Map;

import org.bionlpst.corpus.Normalization;
import org.bionlpst.schema.Schema;
import org.bionlpst.util.message.CheckLogger;

public class NormalizationLabelSchema implements Schema<Normalization> {
	private final Map<String,String> labels;

	public NormalizationLabelSchema(Map<String, String> labels) {
		super();
		this.labels = labels;
	}

	@Override
	public void check(CheckLogger logger, Normalization item) {
		String ref = item.getReferent();
		if (labels.containsKey(ref)) {
			String label = labels.get(ref);
			item.setLabel(label);
		}
	}

	@Override
	public Schema<Normalization> reduce() {
		return this;
	}
}
