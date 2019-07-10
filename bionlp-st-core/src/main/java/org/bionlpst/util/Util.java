package org.bionlpst.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Miscellaneous utilities.
 * @author rbossy
 *
 */
public enum Util {
	;
	
	/**
	 * Split a string using the specified character separator.
	 * @param s string to split.
	 * @param sep separator.
	 * @return list of columns.
	 */
	public static List<String> split(String s, char sep) {
		notnull(s);
		List<String> result = new ArrayList<String>();
		int last = 0;
		for (int i = 0; i < s.length(); ++i) {
			char c = s.charAt(i);
			if (c == sep) {
				result.add(s.substring(last, i).trim());
				last = i + 1;
			}
		}
		result.add(s.substring(last));
		return result;
	}
	
	public static CharSequence join(Collection<? extends CharSequence> strings, CharSequence sep) {
		if (strings.isEmpty()) {
			return "";
		}
		if (strings.size() == 1) {
			return strings.iterator().next();
		}
		StringBuilder result = new StringBuilder();
		boolean first = true;
		for (CharSequence s : strings) {
			if (first) {
				first = false;
			}
			else {
				result.append(sep);
			}
			result.append(s);
		}
		return result;
	}
	
	/**
	 * Reads the specified stream as a string.
	 * @param reader the stream.
	 * @return the contents of the stream.
	 * @throws IOException
	 */
	public static final String readWholeStream(Reader reader) throws IOException {
		notnull(reader);
		StringBuilder result = new StringBuilder();
		char[] buf = new char[1024];
		while (true) {
			int n = reader.read(buf);
			if (n == -1)
				break;
			result.append(buf, 0, n);
		}
		return result.toString();
	}
	
	public static final String readWholeStream(InputStream is) throws IOException {
		notnull(is);
		try (Reader r = new InputStreamReader(is)) {
			return readWholeStream(r);
		}
	}
	
	/**
	 * Fills target with items in source that satisfy filter.
	 * @param filter the filter.
	 * @param source the source collection.
	 * @param target the target collection.
	 * @return target.
	 */
	public static <T,C extends Collection<T>> C filter(Filter<T> filter, Collection<T> source, C target) {
		notnull(filter);
		notnull(source);
		notnull(target);
		filter.init();
		for (T item : source) {
			if (filter.accept(item)) {
				target.add(item);
			}
		}
		return target;
	}

	/**
	 * Returns a list of items in the specified collection that satisfy the specified filter.
	 * @param filter the filter.
	 * @param source the source collection.
	 * @return a list of items in source that satisfy filter.
	 */
	public static <T> List<T> filter(Filter<T> filter, Collection<T> source) {
		return filter(filter, source, new ArrayList<T>(source.size()));
	}
	
	/**
	 * Returns the specified object.
	 * @param obj the object.
	 * @return the specified object.
	 * @throws NullPointerException if the specified object is null.
	 */
	public static <T> T notnull(T obj) throws NullPointerException {
		if (obj == null) {
			throw new NullPointerException();
		}
		return obj;
	}
	
	/**
	 * Returns the specified collection.
	 * @param c the collection.
	 * @return the specified collection.
	 * @throws NullPointerException if the specified collection or any of its elements is null.
	 */
	public static <T,C extends Collection<T>> C nonenull(C c) {
		for (T obj : notnull(c)) {
			notnull(obj);
		}
		return c;
	}
	
	public static <T> T instantiateAndCast(String className, Class<T> superClass) throws InstantiationException, IllegalAccessException, ClassNotFoundException {
		Class<?> klass = Class.forName(className);
		Object obj = klass.newInstance();
		return superClass.cast(obj);
	}
}
