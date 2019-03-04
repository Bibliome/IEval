package org.bionlpst.app.xml;

import org.bionlpst.corpus.parser.bionlpst.DirectoryInputStreamCollection;
import org.bionlpst.corpus.parser.bionlpst.InputStreamCollection;
import org.bionlpst.corpus.parser.bionlpst.ZipFileInputStreamCollection;
import org.bionlpst.corpus.parser.bionlpst.ZipResourceInputStreamCollection;
import org.bionlpst.util.dom.DOMElementConverter;
import org.w3c.dom.Element;

public class CorpusSourceConverter implements DOMElementConverter<InputStreamCollection> {
	private final ClassLoader classLoader;
	
	public CorpusSourceConverter(ClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}

	@Override
	public InputStreamCollection convert(Element element) throws Exception {
		if (element.hasAttribute("zipfile")) {
			return new ZipFileInputStreamCollection(element.getAttribute("zipfile"));
		}
		if (element.hasAttribute("dir")) {
			return new DirectoryInputStreamCollection(element.getAttribute("dir"));
		}
		if (element.hasAttribute("zipresource")) {
			return new ZipResourceInputStreamCollection(classLoader, element.getAttribute("zipresource"));
		}
		return new ZipResourceInputStreamCollection(classLoader, element.getTextContent());
	}
}
