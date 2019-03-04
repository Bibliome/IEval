package org.bionlpst.corpus.parser.bionlpst;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ZipFileInputStreamCollection extends AbstractZipInputStreamCollection {
	private final File zipFile;

	public ZipFileInputStreamCollection(File zipFile) {
		super();
		this.zipFile = zipFile;
	}
	
	public ZipFileInputStreamCollection(String zipFile) {
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
