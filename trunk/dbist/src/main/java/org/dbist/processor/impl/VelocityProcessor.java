package org.dbist.processor.impl;

import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.dbist.processor.Processor;

public class VelocityProcessor implements Processor {
	VelocityEngine ve;

	@Override
	public String process(String value, Map<String, Object> contextMap) throws Exception {
		if (ve == null) {
			synchronized (this) {
				if (ve == null) {
					ve = new VelocityEngine();
					ve.init();
				}
			}
		}

		StringWriter writer = new StringWriter();
		VelocityContext context = new VelocityContext(contextMap);
		ve.evaluate(context, writer, value, value);
		return writer.toString();
	}

}
