package org.bionlpst.app.xml;

import java.util.LinkedHashMap;
import java.util.Map;

import org.bionlpst.BioNLPSTException;
import org.bionlpst.app.Task;
import org.bionlpst.util.Util;
import org.bionlpst.util.dom.DOMElementConverter;
import org.w3c.dom.Element;

//import com.sun.xml.internal.ws.util.DOMUtil;

public class TaskMapConverter implements DOMElementConverter<Map<String,Task>> {
	private final TaskConverter taskConverter;
	private final Map<String,Task> result;
	
	public TaskMapConverter(ClassLoader classLoader, Map<String,Task> result) {
		super();
		Util.notnull(classLoader);
		this.result = result;
		this.taskConverter = new TaskConverter(classLoader);
	}
	
	public TaskMapConverter(ClassLoader classLoader) {
		this(classLoader, new LinkedHashMap<String,Task>());
	}

	@Override
	public Map<String,Task> convert(Element element) throws Exception {
	    for (Element child : /*DOMUtil.getChildElements(element)XXX*/new Element[0]) {
			Task task = taskConverter.convert(child);
			String name = task.getName();
			if (result.containsKey(name)) {
				throw new BioNLPSTException("duplicate task: " + name);
			}
			result.put(name, task);
		}
		if (result.isEmpty()) {
			throw new BioNLPSTException("missing task descriptions");
		}
		return result;
	}

	public Map<String,Task> getResult() {
		return result;
	}
}
