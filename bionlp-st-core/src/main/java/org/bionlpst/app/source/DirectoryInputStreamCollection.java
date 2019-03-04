package org.bionlpst.app.source;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class DirectoryInputStreamCollection implements InputStreamCollection {
	private final File directory;
	
	public DirectoryInputStreamCollection(File directory) {
		super();
		this.directory = directory;
	}
	
	public DirectoryInputStreamCollection(String directory) {
		this(new File(directory));
	}

	@Override
	public InputStreamIterator getIterator() {
		String[] files = directory.list();
		return new DirectoryInputStreamIterator(files);
	}

	@Override
	public String getName() {
		return directory.getPath();
	}

	private class DirectoryInputStreamIterator implements InputStreamIterator {
		private final String[] files;
		private int currentIndex = -1;
		private InputStream currentStream = null;
		
		private DirectoryInputStreamIterator(String[] files) {
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
