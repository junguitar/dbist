/**
 * Copyright 2011 the original author or authors.
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
package com.googlecode.dbist.util;

import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.BeansException;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class Beans implements ApplicationContextAware {
	public static <T> T get(Class<T> requiredType) {
		return getApplicationContext().getBean(requiredType);
	}
	public static Object get(String name) {
		return getApplicationContext().getBean(name);
	}
	public static <T> T get(String name, Class<T> requiredType) {
		return getApplicationContext().getBean(name, requiredType);
	}

	private static String applicationContextName;
	private static ApplicationContext applicationContext;
	public void setApplicationContextName(String name) {
		if (Beans.applicationContext == null) {
			Beans.applicationContextName = name;
			return;
		}
		acMap.put(name, Beans.applicationContext);
		Beans.applicationContext = null;
	}
	private static Map<String, ApplicationContext> acMap = new HashMap<String, ApplicationContext>();
	private static ApplicationContext getApplicationContext() {
		if (Com.isEmpty(Beans.applicationContextName))
			return Beans.applicationContext;
		return acMap.get(Beans.applicationContextName);
	}
	@Override
	public void setApplicationContext(ApplicationContext applicationContext)
			throws BeansException {
		if (Com.isEmpty(Beans.applicationContextName)) {
			Beans.applicationContext = applicationContext;
			return;
		}
		acMap.put(Beans.applicationContextName, applicationContext);
		Beans.applicationContextName = null;
	}
}
