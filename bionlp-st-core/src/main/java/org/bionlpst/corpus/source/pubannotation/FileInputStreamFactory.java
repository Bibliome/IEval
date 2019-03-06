package org.bionlpst.corpus.source.pubannotation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class FileInputStreamFactory implements InputStreamFactory {
	private final File file;

	public FileInputStreamFactory(File file) {
		super();
		this.file = file;
	}

	@Override
	public InputStream getInputStream() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public String getName() {
		return file.getPath();
	}
}
