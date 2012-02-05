package org.dbist.processor;

import java.util.Map;

public interface Processor {
	String process(String value, Map<String, Object> contextMap) throws Exception;
}
