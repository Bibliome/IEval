package org.bionlpst.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

import org.bionlpst.util.Filter;
import org.bionlpst.util.Util;

public class IdFilter implements Filter<Annotation> {
	private final Map<String,Collection<String>> annotationIds;
	private final boolean excludeIds;

	public IdFilter(Map<String, Collection<String>> annotationIds, boolean excludeIds) {
		super();
		this.annotationIds = annotationIds;
		this.excludeIds = excludeIds;
	}
	
	public IdFilter(Map<String, Collection<String>> annotationIds) {
		this(annotationIds, false);
	}
	
	public IdFilter(BufferedReader r, boolean excludeIds) throws IOException {
		this(getIds(r), excludeIds);
	}
	
	public IdFilter(BufferedReader r) throws IOException {
		this(getIds(r));
	}

	private static Map<String,Collection<String>> getIds(BufferedReader r) throws IOException {
		Map<String,Collection<String>> result = new LinkedHashMap<String,Collection<String>>();
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			List<String> cols = Util.split(line, '\t');
			String docId = cols.get(0);
			Collection<String> ids = ensureIds(result, docId);
			String annId = cols.get(1);
			ids.add(annId);
		}
		return result;
	}
	
	private static Collection<String> ensureIds(Map<String,Collection<String>> annotationIds, String docId) {
		if (annotationIds.containsKey(docId)) {
			return annotationIds.get(docId);
		}
		Collection<String> result = new LinkedHashSet<String>();
		annotationIds.put(docId, result);
		return result;
	}

	@Override
	public boolean accept(Annotation item) {
		return find(item) == excludeIds;
	}
	
	private boolean find(Annotation item) {
		String docId = item.getDocument().getId();
		if (annotationIds.containsKey(docId)) {
			Collection<String> ids = annotationIds.get(docId);
			return ids.contains(item.getId());
		}
		return false;
	}

	@Override
	public Filter<Annotation> reduce() {
		if (annotationIds.isEmpty()) {
			if (excludeIds) {
				return new AcceptAll<Annotation>();
			}
			return new RejectAll<Annotation>();
		}
		return this;
	}

	@Override
	public void init() {
	}
}
