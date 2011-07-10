package org.dbist.util;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Reflection {
	private static Map<Class<?>, List<Field>> fieldListCache = new ConcurrentHashMap<Class<?>, List<Field>>();
	public static List<Field> getFieldList(Class<?> clazz, boolean cache) {
		if (clazz == null)
			throw new NullPointerException("clazz parameter is null.");
		if (fieldListCache.containsKey(clazz))
			return fieldListCache.get(clazz);
		
		if (cache) {
			String monId = new StringBuffer(clazz.getName()).append(".getFieldList").toString();
			synchronized (Monitor.get(monId)) {
				try {
					if (fieldListCache.containsKey(clazz))
						return fieldListCache.get(clazz);
					List<Field> list = new ArrayList<Field>();
					populateFieldList(list, clazz);
					fieldListCache.put(clazz, list);
					return list;
				} finally {
					Monitor.remove(monId);
				}
			}
		}
		
		List<Field> list = new ArrayList<Field>();
		populateFieldList(list, clazz);
		return list;
	}
	private static void populateFieldList(List<Field> list, Class<?> clazz) {
		if (clazz == null || clazz.equals(Object.class))
			return;
		populateFieldList(list, clazz.getSuperclass());
		for (Field field : clazz.getDeclaredFields()) {
			if (Modifier.isStatic(field.getModifiers()))
				continue;
			list.add(field);
		}
	}
}
