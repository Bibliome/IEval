package org.bionlpst.util;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;

public class URLSourceStream extends SourceStream {
	private final URL url;

	public URLSourceStream(URL url) {
		super();
		this.url = Util.notnull(url);
	}
	
	public URLSourceStream(String url) throws MalformedURLException {
		this(new URL(url));
	}

	public URL getURL() {
		return url;
	}

	@Override
	public InputStream open() throws IOException {
		return url.openStream();
	}

	@Override
	public String getName() {
		return url.toString();
	}
}
