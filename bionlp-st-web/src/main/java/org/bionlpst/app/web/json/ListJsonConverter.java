package org.bionlpst.app.web.json;

import java.util.Collection;

import org.codehaus.jettison.json.JSONArray;

public class ListJsonConverter<T> implements JsonConverter<Collection<? extends T>>{
	private final JsonConverter<T> converter;

	public ListJsonConverter(JsonConverter<T> converter) {
		super();
		this.converter = converter;
	}
	
	@Override
	public Object convert(Collection<? extends T> c) throws Exception {
		return convert(converter, c);
	}

	public static <T> JSONArray convert(JsonConverter<T> converter, Collection<? extends T> list) throws Exception {
		JSONArray result = new JSONArray();
		for (T obj : list) {
			result.put(converter.convert(obj));
		}
		return result;
	}
}
