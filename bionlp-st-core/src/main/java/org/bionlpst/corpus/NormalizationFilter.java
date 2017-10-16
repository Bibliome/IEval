package org.bionlpst.corpus;

import java.io.BufferedReader;
import java.io.IOException;

import org.bionlpst.util.Filter;

public class NormalizationFilter<T extends Annotation> implements Filter<T> {
	private final String type;
	private final Filter<String> acceptedReferents;

	public NormalizationFilter(String type, Filter<String> acceptedReferents) {
		super();
		this.type = type;
		this.acceptedReferents = acceptedReferents;
	}

	public NormalizationFilter(String type, BufferedReader acceptedReferencesReader) throws IOException {
		this(type, new CollectionFilter(acceptedReferencesReader));
	}

	@Override
	public boolean accept(T item) {
		Normalization norm = item.asNormalization();
		if (norm != null) {
			if (type.equals(norm.getType())) {
				String ref = norm.getReferent();
				return acceptedReferents.accept(ref);
			}
			return true;
		}
		for (Annotation backRef : item.getBackReferences()) {
			Normalization backRefNorm = backRef.asNormalization();
			if (backRefNorm != null) {
				if (type.equals(backRefNorm.getType())) {
					String ref = backRefNorm.getReferent();
					if (acceptedReferents.accept(ref)) {
						return true;
					}
				}
			}
		}
		return false;
	}

	@Override
	public Filter<T> reduce() {
		return new NormalizationFilter<T>(type, acceptedReferents.reduce());
	}
}
