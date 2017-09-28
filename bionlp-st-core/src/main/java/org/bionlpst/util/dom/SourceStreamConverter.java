package org.bionlpst.util.dom;

import org.bionlpst.util.FileSourceStream;
import org.bionlpst.util.ResourceSourceStream;
import org.bionlpst.util.SourceStream;
import org.bionlpst.util.URLSourceStream;
import org.bionlpst.util.Util;
import org.w3c.dom.Element;

public class SourceStreamConverter implements DOMElementConverter<SourceStream> {
	private final ClassLoader classLoader;
	
	public SourceStreamConverter(ClassLoader classLoader) {
		super();
		Util.notnull(classLoader);
		this.classLoader = classLoader;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	@Override
	public SourceStream convert(Element element) throws Exception {
		if (element.hasAttribute("file")) {
			return new FileSourceStream(element.getAttribute("file"));
		}
		if (element.hasAttribute("url")) {
			return new URLSourceStream(element.getAttribute("url"));
		}
		if (element.hasAttribute("href")) {
			return new URLSourceStream(element.getAttribute("href"));
		}
		if (element.hasAttribute("jar")) {
			return new ResourceSourceStream(classLoader, element.getAttribute("jar"));
		}
		String name = element.getTextContent().trim();
		return new FileSourceStream(name);
	}
}
