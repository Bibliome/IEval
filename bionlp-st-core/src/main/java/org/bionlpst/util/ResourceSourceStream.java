package org.bionlpst.util;

import java.io.IOException;
import java.io.InputStream;

public class ResourceSourceStream extends SourceStream {
	private final ClassLoader classLoader;
	private final String resourceName;

	public ResourceSourceStream(ClassLoader classLoader, String resourceName) {
		super();
		Util.notnull(classLoader);
		Util.notnull(resourceName);
		this.classLoader = classLoader;
		this.resourceName = resourceName;
	}

	public ClassLoader getClassLoader() {
		return classLoader;
	}

	public String getResourceName() {
		return resourceName;
	}

	@Override
	public InputStream open() throws IOException {
		InputStream result = classLoader.getResourceAsStream(resourceName);
		if (result == null) {
			throw new IOException("resource not found: " + resourceName);
		}
		return result;
	}

	@Override
	public String getName() {
		return resourceName;
	}
}
