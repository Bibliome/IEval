package org.bionlpst.corpus.source.pubannotation;

import java.io.IOException;
import java.io.InputStream;

public class DisposableInputStreamFactory implements InputStreamFactory {
	private final String name;
	private final InputStream inputStream;

	public DisposableInputStreamFactory(String name, InputStream inputStream) {
		super();
		this.name = name;
		this.inputStream = inputStream;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return inputStream;
	}

	@Override
	public String getName() {
		return name;
	}
}
