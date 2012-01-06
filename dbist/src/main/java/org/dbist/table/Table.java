package org.dbist.table;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.common.util.Closure;
import net.sf.common.util.ReflectionUtils;
import net.sf.common.util.SyncCtrlUtils;
import net.sf.common.util.ValueUtils;

import org.dbist.dml.Dml;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Table {
	private static final Logger logger = LoggerFactory.getLogger(Table.class);

	private String name;
	private List<Field> pkField = new ArrayList<Field>();
	private List<Field> field = new ArrayList<Field>();
	private List<Column> pkColumn = new ArrayList<Column>();
	private List<Column> column = new ArrayList<Column>();
	private List<String> pkColumnName = new ArrayList<String>();
	private List<String> columnName = new ArrayList<String>();
	private List<String> pkFieldName = new ArrayList<String>();
	private List<String> fieldName = new ArrayList<String>();
	private static Map<Class<?>, Table> cache = new ConcurrentHashMap<Class<?>, Table>();

	public static Table get(Object obj, String dbType) {
		if (obj == null)
			throw new NullPointerException("obj parameter is null.");

		final Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();

		final boolean debug = logger.isDebugEnabled();

		if (cache.containsKey(clazz)) {
			if (debug)
				logger.debug("get table metadata from map cache by class: " + clazz.getName());
			return cache.get(clazz);
		}

		return SyncCtrlUtils.wrap("Table." + clazz.getName(), cache, clazz, new Closure() {
			@Override
			public Object execute() {
				if (debug)
					logger.debug("make table metadata by class: " + clazz.getName());
				Table table = new Table();
				table.setName(toName(clazz));
				for (Field field : ReflectionUtils.getFieldList(clazz, false)) {
					String fname = field.getName();
					String cname = toColumnName(field);

					table.field.add(field);
					table.fieldName.add(fname);
					table.columnName.add(cname);
				}
				return table;
			}
		});
	}

	private static <T> String toName(Class<T> clazz) {
		// TODO <T> String toName(Class<T> clazz)
		org.dbist.annotation.Table tableAnn = clazz.getAnnotation(org.dbist.annotation.Table.class);
		if (tableAnn != null && !ValueUtils.isEmpty(tableAnn.name()))
			return tableAnn.name();
		return clazz.getSimpleName().toLowerCase();
	}

	private static String toColumnName(Field field) {
		// TODO String toColumnName(Field field)
		return field.getName();
	}

	public String toColumnName(String name) {
		// TODO String toColumnName(String name)
		return name;
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
	public List<Field> getField() {
		return field;
	}
	public List<Column> getPkColumn() {
		return pkColumn;
	}
	public List<Column> getColumn() {
		return column;
	}
	public List<String> getPkColumnName() {
		return pkColumnName;
	}
	public List<String> getColumnName() {
		return columnName;
	}
	public List<String> getPkFieldName() {
		return pkFieldName;
	}
	public List<String> getFieldName() {
		return fieldName;
	}
}
