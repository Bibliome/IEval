package org.bionlpst.app.source;

import java.io.IOException;
import java.io.InputStream;

public class ZipResourceCorpusSource extends ZipCorpusSource {
	private final ClassLoader classLoader;
	private final String resourceName;

	public ZipResourceCorpusSource(ClassLoader classLoader, String resourceName) {
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
