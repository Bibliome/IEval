package org.bionlpst.app.web.json;



public interface JsonConverter<T> {
	public abstract Object convert(T obj) throws Exception;
}
