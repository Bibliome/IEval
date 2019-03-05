package org.bionlpst.app.xml;

import org.bionlpst.corpus.parser.ContentAndReferenceSource;
import org.bionlpst.corpus.parser.bionlpst.BioNLPSTSource;
import org.bionlpst.corpus.parser.bionlpst.InputStreamCollection;
import org.bionlpst.util.dom.DOMElementConverter;
import org.w3c.dom.Element;

public class ContentAndReferenceSourceConverter implements DOMElementConverter<ContentAndReferenceSource> {
	private final InputStreamCollectionConverter corpusSourceConverter;

	public ContentAndReferenceSourceConverter(ClassLoader classLoader) {
		super();
		this.corpusSourceConverter = new InputStreamCollectionConverter(classLoader);
	}

	@Override
	public ContentAndReferenceSource convert(Element element) throws Exception {
		InputStreamCollection corpusSource = corpusSourceConverter.convert(element);
		return new BioNLPSTSource(corpusSource);
	}
}
