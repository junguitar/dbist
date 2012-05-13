/**
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dbist.processor.impl;

import java.util.Map;

import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;

import net.sf.common.util.ValueUtils;

import org.dbist.processor.Preprocessor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * @author Steve M. Jung
 * @since 2012. 5. 9. (version 1.0.10)
 */
public class ScriptPreprocessor implements Preprocessor, InitializingBean {
	private ScriptEngineFactory engineFactory;

	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notNull(engineFactory, "'engineFactory' is a required property.");
	}

	@Override
	public String process(String value, Map<String, ?> contextMap) throws Exception {
		ScriptEngine engine = engineFactory.getScriptEngine();
		Bindings bindings = engine.createBindings();
		if (contextMap != null)
			bindings.putAll(contextMap);
		Object result = engine.eval(value, bindings);
		return ValueUtils.toString(result);
	}

}
