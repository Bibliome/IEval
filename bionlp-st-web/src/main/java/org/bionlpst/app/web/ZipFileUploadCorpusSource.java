package org.bionlpst.app.web;

import java.io.IOException;
import java.io.InputStream;

import org.bionlpst.app.source.ZipCorpusSource;

class ZipFileUploadCorpusSource extends ZipCorpusSource {
	private final InputStream inputStream;
	private final String name;
	
	ZipFileUploadCorpusSource(InputStream inputStream, String name) {
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
