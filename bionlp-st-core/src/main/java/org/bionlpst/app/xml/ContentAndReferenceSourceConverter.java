package org.bionlpst.app.xml;

import org.bionlpst.corpus.source.ContentAndReferenceSource;
import org.bionlpst.corpus.source.bionlpst.BioNLPSTSource;
import org.bionlpst.corpus.source.bionlpst.InputStreamCollection;
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
		InputStreamCollection inputStreamCollection = corpusSourceConverter.convert(element);
		return new BioNLPSTSource(inputStreamCollection);
	}
}
