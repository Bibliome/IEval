package org.bionlpst.corpus.parser.bionlpst;

import java.io.IOException;
import java.io.InputStream;

public class ZipResourceInputStreamCollection extends AbstractZipInputStreamCollection {
	private final ClassLoader classLoader;
	private final String resourceName;

	public ZipResourceInputStreamCollection(ClassLoader classLoader, String resourceName) {
		super();
		this.classLoader = classLoader;
		this.resourceName = resourceName;
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		if (classLoader == null) {
			return ClassLoader.getSystemResourceAsStream(resourceName);
		}
		return classLoader.getResourceAsStream(resourceName);
	}

	@Override
	public String getName() {
		return resourceName;
	}
}
