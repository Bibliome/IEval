package org.bionlpst.app.source;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.zip.ZipEntry;

import org.apache.commons.compress.archivers.ArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;

public abstract class ZipCorpusSource extends CorpusSource {
	@Override
	protected EntryIterator getEntries() throws IOException {
		InputStream is = getInputStream();
		System.err.println("is = " + is);
		byte[] b = new byte[2048];
		is.read(b);
		System.err.println("b = " + b);
		ZipArchiveInputStream zis = new ZipArchiveInputStream(is);
		return new ZipEntryIterator(zis);
	}
	
	protected abstract InputStream getInputStream() throws IOException;

	private class ZipEntryIterator implements EntryIterator {
		private final ZipArchiveInputStream zis;
		private ArchiveEntry currentEntry = null;
		
		private ZipEntryIterator(ZipArchiveInputStream zis) {
			super();
			this.zis = zis;
		}

		@Override
		public void close() throws Exception {
			zis.close();
		}

		@Override
		public boolean next() throws IOException {
			currentEntry = zis.getNextZipEntry();
			System.err.println("currentEntry = " + currentEntry);
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
