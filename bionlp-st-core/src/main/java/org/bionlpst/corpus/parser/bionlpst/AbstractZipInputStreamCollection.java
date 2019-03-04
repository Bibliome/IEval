package org.bionlpst.corpus.parser.bionlpst;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class AbstractZipInputStreamCollection implements InputStreamCollection {
	@Override
	public InputStreamIterator getIterator() throws IOException {
		InputStream is = getInputStream();
		ZipInputStream zis = new ZipInputStream(is);
		return new ZipInputStreamIterator(zis);
	}
	
	protected abstract InputStream getInputStream() throws IOException;

	private class ZipInputStreamIterator implements InputStreamIterator {
		private final ZipInputStream zis;
		private ZipEntry currentEntry = null;
		
		private ZipInputStreamIterator(ZipInputStream zis) {
			super();
			this.zis = zis;
		}

		@Override
		public void close() throws Exception {
			zis.close();
		}

		@Override
		public boolean next() throws IOException {
			currentEntry = zis.getNextEntry();
			return currentEntry != null;
		}

		@Override
		public void closeEntry() throws IOException {
		}

		@Override
		public String getName() {
			return AbstractZipInputStreamCollection.this.getName() + File.separator + currentEntry.getName();
		}

		@Override
		public InputStream getContents() throws IOException {
			return zis;
		}
	}
}
