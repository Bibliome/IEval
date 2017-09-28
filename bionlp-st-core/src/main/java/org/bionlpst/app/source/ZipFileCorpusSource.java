package org.bionlpst.app.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ZipFileCorpusSource extends ZipCorpusSource {
	private final File zipFile;

	public ZipFileCorpusSource(File zipFile) {
		super();
		this.zipFile = zipFile;
	}
	
	public ZipFileCorpusSource(String zipFile) {
		this(new File(zipFile));
	}

	@Override
	public String getName() {
		return zipFile.getPath();
	}

	@Override
	protected InputStream getInputStream() throws IOException {
		return new FileInputStream(zipFile);
	}
}
