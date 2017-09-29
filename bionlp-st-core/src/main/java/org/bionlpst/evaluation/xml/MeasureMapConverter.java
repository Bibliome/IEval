package org.bionlpst.evaluation.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bionlpst.evaluation.Measure;
import org.bionlpst.util.dom.DOMElementConverter;
import org.bionlpst.util.dom.DOMUtil;
import org.w3c.dom.Element;

public enum MeasureMapConverter implements DOMElementConverter<Map<String,Measure>> {
	INSTANCE;
	
	@Override
	public Map<String,Measure> convert(Element element) throws Exception {
		Map<String,Measure> result = new LinkedHashMap<String,Measure>();
		for (Element child : DOMUtil.getChildrenElements(element, false)) {
			String name = child.getTagName();
			String sMeasure = child.getTextContent();
			Measure measure = MeasureConverter.getMeasure(sMeasure);
			result.put(name, measure);
		}
		return result;
	}
}
