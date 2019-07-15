package org.bionlpst.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashSet;

import org.bionlpst.util.Filter;

public class DocumentIdFilter implements Filter<Annotation> {
	private final Collection<String> documentIds;
	private final boolean excludeIds;

	public DocumentIdFilter(Collection<String> documentIds, boolean excludeIds) {
		super();
		this.documentIds = documentIds;
		this.excludeIds = excludeIds;
	}
	
	public DocumentIdFilter(Collection<String> documentIds) {
		this(documentIds, false);
	}
	
	public DocumentIdFilter(BufferedReader r, boolean excludeIds) throws IOException {
		this(getIds(r), excludeIds);
	}
	
	public DocumentIdFilter(BufferedReader r) throws IOException {
		this(getIds(r));
	}

	private static Collection<String> getIds(BufferedReader r) throws IOException {
		Collection<String> result = new LinkedHashSet<String>();
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			result.add(line.trim());
		}
		return result;
	}

	@Override
	public boolean accept(Annotation item) {
		Document doc = item.getDocument();
		String docId = doc.getId();
		return this.documentIds.contains(docId) != excludeIds;
	}

	@Override
	public void init() {
	}

	@Override
	public Filter<Annotation> reduce() {
		if (documentIds.isEmpty()) {
			if (excludeIds) {
				return new Filter.AcceptAll<Annotation>();
			}
			return new Filter.RejectAll<Annotation>();
		}
		return this;
	}
}
