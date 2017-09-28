package org.bionlpst.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

public class FileSourceStream extends SourceStream {
	private final File file;

	public FileSourceStream(File file) {
		super();
		this.file = Util.notnull(file);
	}
	
	public FileSourceStream(String file) {
		this(new File(Util.notnull(file)));
	}

	public File getFile() {
		return file;
	}

	@Override
	public InputStream open() throws IOException {
		return new FileInputStream(file);
	}

	@Override
	public Reader openReader() throws IOException {
		return new FileReader(file);
	}

	@Override
	public String getName() {
		return file.getAbsolutePath();
	}
}
