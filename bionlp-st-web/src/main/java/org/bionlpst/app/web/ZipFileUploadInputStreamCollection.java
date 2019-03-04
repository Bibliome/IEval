package org.bionlpst.app.web;

import java.io.IOException;
import java.io.InputStream;

import org.bionlpst.app.source.AbstractZipInputStreamCollection;

class ZipFileUploadInputStreamCollection extends AbstractZipInputStreamCollection {
	private final InputStream inputStream;
	private final String name;
	
	ZipFileUploadInputStreamCollection(InputStream inputStream, String name) {
		super();
		this.inputStream = inputStream;
		this.name = name;
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return inputStream;
	}

	@Override
	public String getName() {
		return name;
	}
}
