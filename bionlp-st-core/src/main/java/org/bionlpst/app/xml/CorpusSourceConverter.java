package org.bionlpst.app.xml;

import org.bionlpst.app.source.CorpusSource;
import org.bionlpst.app.source.DirectoryCorpusSource;
import org.bionlpst.app.source.ZipFileCorpusSource;
import org.bionlpst.app.source.ZipResourceCorpusSource;
import org.bionlpst.util.dom.DOMElementConverter;
import org.w3c.dom.Element;

public class CorpusSourceConverter implements DOMElementConverter<CorpusSource> {
	private final ClassLoader classLoader;
	
	public CorpusSourceConverter(ClassLoader classLoader) {
		super();
		this.classLoader = classLoader;
	}

	@Override
	public CorpusSource convert(Element element) throws Exception {
		if (element.hasAttribute("zipfile")) {
			return new ZipFileCorpusSource(element.getAttribute("zipfile"));
		}
		if (element.hasAttribute("dir")) {
			return new DirectoryCorpusSource(element.getAttribute("dir"));
		}
		if (element.hasAttribute("zipresource")) {
			return new ZipResourceCorpusSource(classLoader, element.getAttribute("zipresource"));
		}
		return new ZipResourceCorpusSource(classLoader, element.getTextContent());
	}
}
