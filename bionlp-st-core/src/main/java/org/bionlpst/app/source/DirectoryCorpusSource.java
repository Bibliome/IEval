package org.bionlpst.app.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DirectoryCorpusSource extends CorpusSource {
	private final File directory;
	
	public DirectoryCorpusSource(File directory) {
		super();
		this.directory = directory;
	}
	
	public DirectoryCorpusSource(String directory) {
		this(new File(directory));
	}

	@Override
	protected EntryIterator getEntries() {
		String[] files = directory.list();
		return new DirectoryEntryIterator(files);
	}

	@Override
	public String getName() {
		return directory.getPath();
	}

	private class DirectoryEntryIterator implements EntryIterator {
		private final String[] files;
		private int currentIndex = -1;
		private InputStream currentStream = null;
		
		private DirectoryEntryIterator(String[] files) {
			super();
			this.files = files;
		}

		@Override
		public void close() throws Exception {
			closeEntry();
		}

		@Override
		public void closeEntry() throws IOException {
			if (currentStream != null) {
				currentStream.close();
				currentStream = null;
			}
		}

		@Override
		public boolean next() {
			currentIndex++;
			return currentIndex < files.length;
		}

		@Override
		public String getName() {
			return new File(directory, files[currentIndex]).getPath();
		}

		@Override
		public InputStream getContents() throws FileNotFoundException {
			currentStream = new FileInputStream(new File(directory, files[currentIndex]));
			return currentStream;
		}
	}
}
