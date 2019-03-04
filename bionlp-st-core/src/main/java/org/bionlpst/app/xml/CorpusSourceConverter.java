package org.bionlpst.app.xml;

import org.bionlpst.app.source.InputStreamCollection;
import org.bionlpst.app.source.DirectoryInputStreamCollection;
import org.bionlpst.app.source.ZipFileInputStreamCollection;
import org.bionlpst.app.source.ZipResourceInputStreamCollection;
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
