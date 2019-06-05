package org.bionlpst.app.xml;

import java.io.File;

import org.bionlpst.corpus.source.pubannotation.FileInputStreamFactory;
import org.bionlpst.corpus.source.pubannotation.InputStreamFactory;
import org.bionlpst.util.dom.DOMElementConverter;
import org.w3c.dom.Element;

public class InputStreamFactoryConverter implements DOMElementConverter<InputStreamFactory> {
	public InputStreamFactoryConverter() {
		super();
	}

	@Override
	public InputStreamFactory convert(Element element) throws Exception {
		String path = element.getTextContent();
		File file = new File(path);
		return new FileInputStreamFactory(file);
	}
}
