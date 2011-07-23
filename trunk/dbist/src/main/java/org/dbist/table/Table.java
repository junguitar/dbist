package org.dbist.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.common.util.MonitorUtils;
import net.sf.common.util.ReflectionUtils;

public class Table {
	private String name;
	private List<Field> pkField = new ArrayList<Field>();
	private List<Field> field = new ArrayList<Field>();
	private List<Column> pkColumn = new ArrayList<Column>();
	private List<Column> column = new ArrayList<Column>();
	private List<String> pkFieldName = new ArrayList<String>();
	private List<String> pkColumnName = new ArrayList<String>();
	private List<String> fieldName = new ArrayList<String>();
	private List<String> columnName = new ArrayList<String>();
	private static Map<Class<?>, Table> cache = new ConcurrentHashMap<Class<?>, Table>();

	public static <T> Table get(T data) {
		if (data == null)
			throw new NullPointerException("data parameter is null.");
		return get(data.getClass());
	}

	public static Table get(Class<?> clazz) {
		if (clazz == null)
			throw new NullPointerException("clazz parameter is null.");
		if (cache.containsKey(clazz))
			return cache.get(clazz);

		String monId = new StringBuffer(clazz.getName()).append(".getTable")
				.toString();
		synchronized (MonitorUtils.get(monId)) {
			try {
				if (cache.containsKey(clazz))
					return cache.get(clazz);
				Table table = new Table();
				table.setName(toName(clazz));
				for (Field field : ReflectionUtils.getFieldList(clazz, false)) {
					String fname = field.getName();
					String cname = toColumnName(field);

					table.field.add(field);
					table.fieldName.add(fname);
					table.columnName.add(cname);
				}
				cache.put(clazz, table);
				return table;
			} finally {
				MonitorUtils.remove(monId);
			}
		}
	}
	
	private static <T> String toName(Class<T> clazz) {
		// TODO <T> String toName(Class<T> clazz)
		return clazz.getSimpleName().toLowerCase();
	}

	private static String toColumnName(Field field) {
		// TODO String toColumnName(Field field)
		return field.getName();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public List<Field> getPkField() {
		return pkField;
	}

	public List<String> getPkFieldName() {
		return pkFieldName;
	}

	public List<Column> getPkColumn() {
		return pkColumn;
	}

	public List<Column> getColumn() {
		return column;
	}
	
	public String toColumnName(String name) {
		// TODO String toColumnName(String name)
		return name;
	}

	public List<String> getPkColumnName() {
		return pkColumnName;
	}

	public List<Field> getField() {
		return field;
	}

	public List<String> getFieldName() {
		return fieldName;
	}

	public List<String> getColumnName() {
		return columnName;
	}
}
