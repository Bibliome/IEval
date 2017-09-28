package org.bionlpst.util.dom;

import org.w3c.dom.Element;

public interface DOMElementConverter<T> {
	T convert(Element element) throws Exception;
}
