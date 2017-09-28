package org.bionlpst.app.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public abstract class ZipCorpusSource extends CorpusSource {
	@Override
	protected EntryIterator getEntries() throws IOException {
		InputStream is = getInputStream();
		ZipInputStream zis = new ZipInputStream(is);
		return new ZipEntryIterator(zis);
	}
	
	protected abstract InputStream getInputStream() throws IOException;

	private class ZipEntryIterator implements EntryIterator {
		private final ZipInputStream zis;
		private ZipEntry currentEntry = null;
		
		private ZipEntryIterator(ZipInputStream zis) {
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
			return ZipCorpusSource.this.getName() + File.separator + currentEntry.getName();
		}

		@Override
		public InputStream getContents() throws IOException {
			return zis;
		}
	}
}
