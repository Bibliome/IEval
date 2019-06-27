package org.bionlpst.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.bionlpst.util.Filter;

public class CollectionFilter implements Filter<String> {
	private final Collection<String> acceptedValues;

	public CollectionFilter(Collection<String> acceptedValues) {
		super();
		this.acceptedValues = acceptedValues;
	}
	
	public CollectionFilter(BufferedReader r) throws IOException {
		this(buildAcceptedValues(r));
	}

	private static Collection<String> buildAcceptedValues(BufferedReader r) throws IOException {
		Collection<String> result = new LinkedHashSet<String>();
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			line = line.trim();
			if (!line.isEmpty()) {
				result.add(line);
			}
		}
		return result;
	}

	@Override
	public boolean accept(String item) {
		return acceptedValues.contains(item);
	}

	@Override
	public Filter<String> reduce() {
		if (acceptedValues.isEmpty()) {
			return new RejectAll<String>();
		}
		return this;
	}

	@Override
	public void init() {
	}
}
