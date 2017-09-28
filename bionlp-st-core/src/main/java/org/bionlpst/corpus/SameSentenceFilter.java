package org.bionlpst.corpus;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import org.bionlpst.util.Filter;
import org.bionlpst.util.Util;
import org.bionlpst.util.fragment.Fragment;
import org.bionlpst.util.fragment.ImmutableFragment;

public class SameSentenceFilter implements Filter<Annotation>, AnnotationVisitor<Void,Collection<Fragment>> {
	private final Map<String,List<Fragment>> sentences;

	public SameSentenceFilter(Map<String,List<Fragment>> sentences) {
		super();
		this.sentences = sentences;
	}
	
	public SameSentenceFilter(BufferedReader r) throws IOException {
		this(getSentences(r));
	}
	
	private static Map<String,List<Fragment>> getSentences(BufferedReader r) throws IOException {
		Map<String,List<Fragment>> result = new HashMap<String,List<Fragment>>();
		while (true) {
			String line = r.readLine();
			if (line == null) {
				break;
			}
			List<String> cols = Util.split(line, '\t');
			String docId = cols.get(0);
			int start = Integer.parseInt(cols.get(1));
			int end = Integer.parseInt(cols.get(2));
			Fragment sent = new ImmutableFragment(start, end);
			List<Fragment> sentences = ensure(result, docId);
			sentences.add(sent);
		}
		return result;
	}
	
	private static List<Fragment> ensure(Map<String,List<Fragment>> sentences, String docId) {
		if (sentences.containsKey(docId)) {
			return sentences.get(docId);
		}
		List<Fragment> result = new ArrayList<Fragment>();
		sentences.put(docId, result);
		return result;
	}

	@Override
	public boolean accept(Annotation item) {
		Collection<Fragment> coveredSentences = new HashSet<Fragment>();
		item.accept(this, coveredSentences);
		return coveredSentences.size() <= 1;
	}

	@Override
	public Filter<Annotation> reduce() {
		return this;
	}
	
	private List<Fragment> getDocumentSentences(Document doc) {
		String docId = doc.getId();
		if (!sentences.containsKey(docId)) {
			String contents = doc.getContents();
			Fragment fakeSentence = new ImmutableFragment(0, contents.length());
			List<Fragment> sentenceList = Collections.singletonList(fakeSentence);
			sentences.put(docId, sentenceList);
			return sentenceList;
		}
		return sentences.get(docId);
	}

	@Override
	public Void visit(TextBound textBound, Collection<Fragment> param) {
		Document doc = textBound.getDocument();
		List<Fragment> docSentences = getDocumentSentences(doc);
		int start = textBound.getStart();
		int end = textBound.getEnd();
		for (Fragment sent : docSentences) {
			if (start >= sent.getStart() && end <= sent.getEnd()) {
				param.add(sent);
			}
		}
		return null;
	}

	@Override
	public Void visit(Relation relation, Collection<Fragment> param) {
		for (Annotation arg : relation.getArguments()) {
			arg.accept(this, param);
		}
		return null;
	}

	@Override
	public Void visit(Normalization normalization, Collection<Fragment> param) {
		return normalization.getAnnotation().accept(this, param);
	}

	@Override
	public Void visit(Modifier modifier, Collection<Fragment> param) {
		return modifier.getAnnotation().accept(this, param);
	}

	@Override
	public Void visit(DummyAnnotation dummy, Collection<Fragment> param) {
		return null;
	}
}
