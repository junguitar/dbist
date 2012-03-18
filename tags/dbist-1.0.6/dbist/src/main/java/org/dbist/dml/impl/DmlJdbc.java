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
package org.dbist.dml.impl;

import java.io.IOException;
import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import javassist.CannotCompileException;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.NotFoundException;

import javax.sql.DataSource;

import net.sf.common.util.Closure;
import net.sf.common.util.ReflectionUtils;
import net.sf.common.util.ResourceUtils;
import net.sf.common.util.SyncCtrlUtils;
import net.sf.common.util.ValueUtils;

import org.apache.commons.collections.map.LRUMap;
import org.apache.commons.collections.map.ListOrderedMap;
import org.dbist.annotation.Ignore;
import org.dbist.dml.AbstractDml;
import org.dbist.dml.Dml;
import org.dbist.dml.Filter;
import org.dbist.dml.Filters;
import org.dbist.dml.Order;
import org.dbist.dml.Page;
import org.dbist.dml.Query;
import org.dbist.exception.DataNotFoundException;
import org.dbist.exception.DbistRuntimeException;
import org.dbist.metadata.Column;
import org.dbist.metadata.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.StringUtils;

/**
 * @author Steve M. Jung
 * @since 2011. 6. 2. (version 0.0.1)
 */
public class DmlJdbc extends AbstractDml implements Dml {
	private static final Logger logger = LoggerFactory.getLogger(DmlJdbc.class);

	private static final String DBTYPE_MYSQL = "mysql";
	private static final String DBTYPE_ORACLE = "oracle";
	private static final String DBTYPE_SQLSERVER = "sqlserver";
	private static final List<String> DBTYPE_SUPPORTED_LIST = ValueUtils.toList(DBTYPE_MYSQL, DBTYPE_ORACLE, DBTYPE_SQLSERVER);
	private static final List<String> DBTYPE_PAGINATIONQUERYSUPPORTED_LIST = ValueUtils.toList(DBTYPE_MYSQL, DBTYPE_ORACLE);
	//	private static final List<String> DBTYPE_SUPPORTED_LIST = ValueUtils.toList("hsqldb", "mysql", "postgresql", "oracle", "sqlserver", "db2");

	private String domain;
	private List<String> domainList = new ArrayList<String>(2);
	private DataSource dataSource;
	private JdbcOperations jdbcOperations;
	private NamedParameterJdbcOperations namedParameterJdbcOperations;
	private int maxSqlByPathCacheSize = 1000;

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		boolean debug = logger.isDebugEnabled();
		super.afterPropertiesSet();
		ValueUtils.assertNotEmpty("domain", getDomain());
		ValueUtils.assertNotEmpty("dataSource", getDataSource());
		ValueUtils.assertNotEmpty("jdbcOperations", getJdbcOperations());
		ValueUtils.assertNotEmpty("namedParameterJdbcOperations", getNamedParameterJdbcOperations());
		DatabaseMetaData metadata = dataSource.getConnection().getMetaData();
		if (ValueUtils.isEmpty(getDbType()))
			setDbType(metadata.getDatabaseProductName().toLowerCase());
		if (getDbType().startsWith("microsoft sql server"))
			setDbType(DBTYPE_SQLSERVER);
		if (!DBTYPE_SUPPORTED_LIST.contains(getDbType()))
			throw new IllegalArgumentException("Unsupported dbType: " + getDbType());
		if (DBTYPE_SQLSERVER.equals(getDbType())) {
			List<String> domainList = new ArrayList<String>(this.domainList.size());
			for (String domain : this.domainList) {
				if (domain.endsWith("."))
					continue;
				domainList.add(domain + ".");
			}
			this.domainList = domainList;
		}
		if (maxSqlByPathCacheSize > 0)
			sqlByPathCache = Collections.synchronizedMap(new LRUMap(maxSqlByPathCacheSize));
		if (debug)
			logger.debug("dml loaded (dbType: " + getDbType() + ")");
	}

	@Override
	public <T> T insert(T data) throws Exception {
		_insert(data);
		return data;
	}

	@Override
	public void insertBatch(List<?> list) throws Exception {
		_insertBatch(list);
	}

	@Override
	public void insert(Object data, String... fieldNames) throws Exception {
		_insert(data, fieldNames);
	}

	@Override
	public void insertBatch(List<?> list, String... fieldNames) throws Exception {
		_insertBatch(list, fieldNames);
	}

	private <T> void _insert(T data, String... fieldNames) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		String sql = table.getInsertSql(fieldNames);
		Map<String, Object> paramMap = toParamMap(table, data, fieldNames);
		updateBySql(sql, paramMap);
	}

	private <T> void _insertBatch(List<T> list, String... fieldNames) throws Exception {
		if (ValueUtils.isEmpty(list))
			return;
		Table table = getTable(list.get(0));
		String sql = table.getInsertSql(fieldNames);
		List<Map<String, ?>> paramMapList = toParamMapList(table, list, fieldNames);
		updateBatchBySql(sql, paramMapList);
	}

	@Override
	public void update(Object data) throws Exception {
		_update(data);
	}

	@Override
	public void updateBatch(List<?> list) throws Exception {
		_updateBatch(list);
	}

	@Override
	public void update(Object data, String... fieldNames) throws Exception {
		_update(data, fieldNames);
	}

	@Override
	public void updateBatch(List<?> list, String... fieldNames) throws Exception {
		_updateBatch(list, fieldNames);
	}

	private <T> void _update(T data, String... fieldNames) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		String sql = table.getUpdateSql(fieldNames);
		if (!ValueUtils.isEmpty(fieldNames)) {
			List<String> fieldNameList = ValueUtils.toList(fieldNames);
			for (String fieldName : table.getPkFieldNames())
				fieldNameList.add(fieldName);
			fieldNames = fieldNameList.toArray(new String[fieldNameList.size()]);
		}
		Map<String, Object> paramMap = toParamMap(table, data, fieldNames);
		if (updateBySql(sql, paramMap) != 1)
			throw new DataNotFoundException(toNotFoundErrorMessage(table, data, toParamMap(table, data, table.getPkFieldNames())));
	}
	private static <T> String toNotFoundErrorMessage(Table table, T data, Map<String, ?> paramMap) {
		StringBuffer buf = new StringBuffer("Couldn't find data for update ").append(data.getClass().getName());
		int i = 0;
		for (String key : paramMap.keySet())
			buf.append(i++ == 0 ? " " : ", ").append(key).append(":").append(paramMap.get(key));
		return buf.toString();
	}

	private <T> void _updateBatch(List<T> list, String... fieldNames) throws Exception {
		if (ValueUtils.isEmpty(list))
			return;
		Table table = getTable(list.get(0));
		String sql = table.getUpdateSql(fieldNames);
		List<Map<String, ?>> paramMapList = toParamMapList(table, list, fieldNames);
		updateBatchBySql(sql, paramMapList);
	}

	@Override
	public void delete(Object data) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		String sql = table.getDeleteSql();
		Map<String, Object> paramMap = toParamMap(table, data, table.getPkFieldNames());
		if (updateBySql(sql, paramMap) != 1)
			throw new DataNotFoundException(toNotFoundErrorMessage(table, data, paramMap));
	}

	@Override
	public void deleteBatch(List<?> list) throws Exception {
		if (ValueUtils.isEmpty(list))
			return;
		Table table = getTable(list.get(0));
		String sql = table.getDeleteSql();
		List<Map<String, ?>> paramMapList = toParamMapList(table, list, table.getPkFieldNames());
		updateBatchBySql(sql, paramMapList);
	}

	private int updateBySql(String sql, Map<String, ?> paramMap) {
		return this.namedParameterJdbcOperations.update(sql, paramMap);
	}

	@SuppressWarnings("unchecked")
	private int[] updateBatchBySql(String sql, List<Map<String, ?>> paramMapList) {
		return this.namedParameterJdbcOperations.batchUpdate(sql, paramMapList.toArray(new Map[paramMapList.size()]));
	}

	private <T> List<Map<String, ?>> toParamMapList(Table table, List<T> list, String... fieldNames) throws Exception {
		List<Map<String, ?>> paramMapList = new ArrayList<Map<String, ?>>();
		for (T data : list)
			paramMapList.add(toParamMap(table, data, fieldNames));
		return paramMapList;
	}
	private <T> Map<String, Object> toParamMap(Table table, T data, String... fieldNames) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> paramMap = new ListOrderedMap();

		// All fields
		if (ValueUtils.isEmpty(fieldNames)) {
			for (Column column : table.getColumnList()) {
				Field field = column.getField();
				paramMap.put(field.getName(), toParamData(field.get(data)));
			}
			return paramMap;
		}

		// Some fields
		for (String fieldName : fieldNames) {
			Field field = table.getField(fieldName);
			if (field == null)
				throw new DbistRuntimeException("Couldn't find column of table[" + table.getDomain() + "." + table.getName() + "] by fieldName["
						+ fieldName + "]");
			paramMap.put(fieldName, toParamData(field.get(data)));
		}
		return paramMap;
	}
	private static Object toParamData(Object data) {
		if (data instanceof Character)
			return data.toString();
		return data;
	}

	private void appendFromWhere(Table table, Query query, boolean lock, StringBuffer buf, Map<String, Object> paramMap) {
		// From
		buf.append(" from ").append(table.getDomain()).append(".").append(table.getName());
		if (lock && DBTYPE_SQLSERVER.equals(getDbType()))
			buf.append(" with (updlock, rowlock)");

		// Where
		appendWhere(buf, table, query, 0, paramMap);
	}

	@Override
	public int selectSize(Class<?> clazz, Object condition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("condition", condition);

		final Table table = getTable(clazz);
		Query query = toQuery(table, condition);

		StringBuffer buf = new StringBuffer("select count(*)");
		@SuppressWarnings("unchecked")
		Map<String, Object> paramMap = new ListOrderedMap();
		appendFromWhere(table, query, false, buf, paramMap);

		return this.namedParameterJdbcOperations.queryForInt(buf.toString(), paramMap);
	}

	@Override
	public <T> List<T> selectList(final Class<T> clazz, Object condition) throws Exception {
		return _selectList(clazz, condition, false);
	}

	@Override
	public <T> List<T> selectListWithLock(Class<T> clazz, Object condition) throws Exception {
		return _selectList(clazz, condition, true);
	}

	public <T> List<T> _selectList(final Class<T> clazz, Object condition, boolean lock) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("condition", condition);

		final Table table = getTable(clazz);
		final Query query = toQuery(table, condition);

		StringBuffer buf = new StringBuffer();
		final List<String> columnNameList = new ArrayList<String>();

		// Select
		buf.append("select");
		// Grouping fields
		if (!ValueUtils.isEmpty(query.getGroup())) {
			if (!ValueUtils.isEmpty(query.getField())) {
				List<String> group = query.getGroup();
				for (String fieldName : query.getField()) {
					if (group.contains(fieldName))
						continue;
					throw new DbistRuntimeException("Grouping query cannot be executed with some other fields: " + clazz.getName() + "." + fieldName);
				}
			}
			int i = 0;
			for (String fieldName : query.getGroup()) {
				String columnName = table.toColumnName(fieldName);
				if (columnName == null)
					throw new DbistRuntimeException("Couldn't find fieldName[" + fieldName + "] of class[" + clazz.getName() + "]");
				buf.append(i++ == 0 ? " " : ", ").append(fieldName);
			}
		}
		// All fields
		else if (ValueUtils.isEmpty(query.getField())) {
			int i = 0;
			for (Column column : table.getColumnList())
				buf.append(i++ == 0 ? " " : ", ").append(column.getName());
		}
		// Some fields
		else {
			int i = 0;
			for (String fieldName : query.getField()) {
				String columnName = table.toColumnName(fieldName);
				if (columnName == null)
					throw new DbistRuntimeException("Couldn't find fieldName[" + fieldName + "] of class[" + clazz.getName() + "]");
				buf.append(i++ == 0 ? " " : ", ").append(columnName);
				columnNameList.add(columnName);
			}
		}

		@SuppressWarnings("unchecked")
		Map<String, Object> paramMap = new ListOrderedMap();
		appendFromWhere(table, query, lock, buf, paramMap);

		// Group by
		if (!ValueUtils.isEmpty(query.getGroup())) {
			buf.append(" group by");
			int i = 0;
			for (String group : query.getGroup())
				buf.append(i++ == 0 ? " " : ", ").append(table.toColumnName(group));
		}

		// Order by
		if (!ValueUtils.isEmpty(query.getOrder())) {
			buf.append(" order by");
			int i = 0;
			for (Order order : query.getOrder())
				buf.append(i++ == 0 ? " " : ", ").append(table.toColumnName(order.getField())).append(order.isAscending() ? " asc" : " desc");
		}

		if (ValueUtils.isEmpty(query.getGroup()))
			appendLock(buf, lock);
		else if (lock)
			throw new DbistRuntimeException("Grouping query cannot be executed with lock.");

		String sql = applyPagination(buf.toString(), paramMap, query.getPageIndex(), query.getPageSize());

		List<T> list = query(sql, paramMap, clazz, table, query.getPageIndex(), query.getPageSize());
		return list;
	}
	private void appendLock(StringBuffer buf, boolean lock) {
		if (!lock || DBTYPE_SQLSERVER.equals(getDbType()))
			return;
		buf.append(" for update");
	}
	public String applyPagination(String sql, Map<String, ?> paramMap, int pageIndex, int pageSize) {
		if (pageIndex < 0 || pageSize <= 0)
			return sql;
		if (DBTYPE_PAGINATIONQUERYSUPPORTED_LIST.contains(getDbType())) {
			@SuppressWarnings("unchecked")
			Map<String, Object> _paramMap = (Map<String, Object>) paramMap;
			String subsql = null;
			int forUpdateIndex = sql.toLowerCase().lastIndexOf("for update");
			if (forUpdateIndex > -1) {
				subsql = sql.substring(forUpdateIndex - 1);
				sql = sql.substring(0, forUpdateIndex - 1);
			}

			StringBuffer buf = new StringBuffer();
			// Oracle
			if (DBTYPE_ORACLE.equals(getDbType())) {
				int fromIndex = pageIndex * pageSize;
				int toIndex = fromIndex + pageSize;
				if (pageIndex > 0) {
					_paramMap.put("__fromIndex", fromIndex);
					_paramMap.put("__toIndex", toIndex);
					buf.append("select * from (select data.*, rownum rownum_ from (").append(sql)
							.append(") data where rownum <= :__toIndex) where rownum_ > :__fromIndex");
				} else {
					_paramMap.put("__toIndex", toIndex);
					buf.append("select * from (").append(sql).append(") where rownum <= :__toIndex");
				}

			}
			// MySQL
			else if (DBTYPE_MYSQL.equals(getDbType())) {
				int fromIndex = pageIndex * pageSize;
				if (pageIndex > 0) {
					_paramMap.put("__fromIndex", fromIndex);
					_paramMap.put("__pageSize", pageSize);
					buf.append(sql).append(" limit :__fromIndex, :__pageSize");
				} else {
					_paramMap.put("__pageSize", pageSize);
					buf.append(sql).append(" limit :__pageSize");
				}
			}
			if (subsql != null)
				buf.append(subsql);
			return buf.toString();
		}
		// SQLServer
		else if (DBTYPE_SQLSERVER.equals(getDbType())) {
			String lowerSql = sql.toLowerCase();
			int selectIndex = lowerSql.indexOf("select");
			int distinctIndex = lowerSql.indexOf("distinct");
			int topIndex = distinctIndex > 0 && distinctIndex < selectIndex + 13 ? distinctIndex + 8 : selectIndex + 6;
			return new StringBuffer(sql).insert(topIndex, " top " + ((pageIndex + 1) * pageSize)).toString();
		}
		return sql;
	}

	private <T> List<T> query(String sql, Map<String, ?> paramMap, final Class<T> requiredType, final Table table, final int pageIndex,
			final int pageSize) throws Exception {
		List<T> list = null;
		if (DBTYPE_PAGINATIONQUERYSUPPORTED_LIST.contains(getDbType()) || pageIndex < 0 || pageSize <= 0) {
			list = this.namedParameterJdbcOperations.query(sql, paramMap, new RowMapper<T>() {
				@Override
				public T mapRow(ResultSet rs, int rowNum) throws SQLException {
					return newInstance(rs, requiredType, table);
				}
			});
		} else {
			list = this.namedParameterJdbcOperations.query(sql, paramMap, new ResultSetExtractor<List<T>>() {
				@Override
				public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<T> list = new ArrayList<T>();
					int fromIndex = pageIndex * pageSize;
					for (int i = 0; i < fromIndex; i++) {
						if (rs.next())
							continue;
						return list;
					}
					int i = 0;
					while (rs.next()) {
						if (i++ == pageSize)
							break;
						list.add(newInstance(rs, requiredType, table));
					}
					return list;
				}
			});
		}
		return list;
	}
	private static Map<Class<?>, Map<String, Field>> classFieldCache = new ConcurrentHashMap<Class<?>, Map<String, Field>>();

	@SuppressWarnings("unchecked")
	private <T> T newInstance(ResultSet rs, Class<T> clazz, Table table) throws SQLException {
		if (ValueUtils.isPrimitive(clazz))
			return (T) toRequiredType(rs, 1, clazz);

		ResultSetMetaData metadata = rs.getMetaData();
		Map<String, Field> fieldCache;
		if (classFieldCache.containsKey(clazz)) {
			fieldCache = classFieldCache.get(clazz);
		} else {
			fieldCache = new ConcurrentHashMap<String, Field>();
			classFieldCache.put(clazz, fieldCache);
		}

		T data;
		try {
			data = newInstance(clazz);
		} catch (InstantiationException e) {
			throw new DbistRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new DbistRuntimeException(e);
		}
		if (data instanceof Map) {
			Map<String, Object> map = (Map<String, Object>) data;
			for (int i = 0; i < metadata.getColumnCount();) {
				i++;
				String name = metadata.getColumnName(i);
				map.put(name, toRequiredType(rs, i, null));
			}
		} else {
			for (int i = 0; i < metadata.getColumnCount();) {
				i++;
				String name = metadata.getColumnName(i);
				Field field = null;
				if (fieldCache.containsKey(name)) {
					field = fieldCache.get(name);
				} else {
					if (table != null) {
						field = table.getFieldByColumnName(name);
						if (field == null)
							field = table.getField(name);
					}
					if (field == null) {
						field = ReflectionUtils.getField(clazz, ValueUtils.toCamelCase(name, '_'));
						if (field == null) {
							for (Field f : ReflectionUtils.getFieldList(clazz, false)) {
								if (!f.getName().equalsIgnoreCase(name))
									continue;
								field = f;
								break;
							}
						}
					}
					fieldCache.put(name, field == null ? ReflectionUtils.NULL_FIELD : field);
				}
				if (field == null || ReflectionUtils.NULL_FIELD.equals(field))
					continue;
				setFieldValue(rs, i, data, field);
			}
		}
		return data;
	}
	private static void setFieldValue(ResultSet rs, int index, Object data, Field field) throws SQLException {
		try {
			field.set(data, toRequiredType(rs, index, field.getType()));
		} catch (IllegalArgumentException e) {
			throw new DbistRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new DbistRuntimeException(e);
		}
	}
	private static Object toRequiredType(ResultSet rs, int index, Class<?> requiredType) throws SQLException {
		if (requiredType == null)
			return rs.getObject(index);
		if (ValueUtils.isPrimitive(requiredType)) {
			if (requiredType.equals(String.class))
				return rs.getString(index);
			if (requiredType.equals(Character.class) || requiredType.equals(char.class)) {
				String str = rs.getString(index);
				if (str == null || str.isEmpty())
					return null;
				return str.charAt(0);
			}
			if (requiredType.equals(BigDecimal.class))
				return rs.getBigDecimal(index);
			if (requiredType.equals(Date.class))
				return rs.getTimestamp(index);
			if (requiredType.equals(Double.class) || requiredType.equals(double.class))
				return rs.getDouble(index);
			if (requiredType.equals(Float.class) || requiredType.equals(float.class))
				return rs.getFloat(index);
			if (requiredType.equals(Long.class) || requiredType.equals(long.class))
				return rs.getLong(index);
			if (requiredType.equals(Integer.class) || requiredType.equals(int.class))
				return rs.getInt(index);
			if (requiredType.equals(Boolean.class) || requiredType.equals(boolean.class))
				return rs.getBoolean(index);
			if (requiredType.equals(Byte[].class) || requiredType.equals(byte[].class))
				return rs.getBytes(index);
			if (requiredType.equals(Byte.class) || requiredType.equals(byte.class))
				return rs.getByte(index);
		}
		return rs.getObject(index);
	}

	private static final String DBFUNC_LOWERCASE_MYSQL = "lower";
	private static final String DBFUNC_LOWERCASE_ORACLE = "lower";
	private static final String DBFUNC_LOWERCASE_SQLSERVER = "lower";
	private static final Map<String, String> DBFUNC_LOWERCASE_MAP;
	static {
		DBFUNC_LOWERCASE_MAP = new HashMap<String, String>();
		DBFUNC_LOWERCASE_MAP.put(DBTYPE_MYSQL, DBFUNC_LOWERCASE_MYSQL);
		DBFUNC_LOWERCASE_MAP.put(DBTYPE_ORACLE, DBFUNC_LOWERCASE_ORACLE);
		DBFUNC_LOWERCASE_MAP.put(DBTYPE_SQLSERVER, DBFUNC_LOWERCASE_SQLSERVER);
	}
	@SuppressWarnings("unchecked")
	private static final List<?> CASECHECK_TYPELIST = ValueUtils.toList(String.class, Character.class, char.class);
	private int appendWhere(StringBuffer buf, Table table, Filters filters, int i, Map<String, Object> paramMap) {
		String logicalOperator = " " + ValueUtils.toString(filters.getOperator(), "and").trim() + " ";

		int j = 0;
		if (!ValueUtils.isEmpty(filters.getFilter())) {
			for (Filter filter : filters.getFilter()) {
				String operator = ValueUtils.toString(filter.getOperator(), "=").trim();
				if ("!=".equals(operator))
					operator = "<>";
				String lo = filter.getLeftOperand();
				String columnName = table.toColumnName(lo);
				if (columnName == null) {
					lo = lo.toLowerCase();
					if (table.getColumn(lo) == null)
						throw new DbistRuntimeException("Unknown field: " + filter.getLeftOperand() + " of table: " + table.getDomain() + "."
								+ table.getName());
					columnName = lo;
				}
				buf.append(i++ == 0 ? " where " : j == 0 ? "" : logicalOperator);
				j++;

				List<?> rightOperand = filter.getRightOperand();

				// case: 'is null' or 'is not null'
				if (ValueUtils.isEmpty(rightOperand)) {
					appendNullCondition(buf, table, columnName, operator);
					continue;
				}

				Class<?> type = table.getFieldByColumnName(columnName).getType();

				// check and process case sensitive
				List<Object> newRightOperand = new ArrayList<Object>(rightOperand.size());
				if (!filter.isCaseSensitive() && CASECHECK_TYPELIST.contains(type)) {
					columnName = DBFUNC_LOWERCASE_MAP.get(getDbType()) + "(" + columnName + ")";
					for (Object ro : rightOperand) {
						if (ro == null)
							;
						else if (ro instanceof String)
							ro = ((String) ro).toLowerCase();
						else
							ro = ro.toString().toLowerCase();
						newRightOperand.add(ro);
					}
				} else {
					for (Object ro : rightOperand)
						newRightOperand.add(toParamValue(ro, type));
				}
				rightOperand = newRightOperand;

				// case only one filter
				if (rightOperand.size() == 1) {
					Object value = rightOperand.get(0);

					// case: is null or is not null
					if (value == null) {
						appendNullCondition(buf, table, columnName, operator);
						continue;
					}

					// case x = 'l' or x != 'l'
					String key = lo + i;
					paramMap.put(key, value);
					buf.append(columnName).append(" ").append(operator).append(" :").append(key);
					continue;
				}

				// case: has null so... (x = 'l' or x is null or...)
				if (rightOperand.contains(null)) {
					if ("in".equals(operator))
						operator = "=";
					else if ("not in".equals(operator))
						operator = "<>";
					String subLogicalOperator = "<>".equals(operator) ? " and " : " or ";
					buf.append("(");
					int k = 0;
					for (Object value : rightOperand) {
						buf.append(k++ == 0 ? "" : subLogicalOperator);
						if (value == null) {
							appendNullCondition(buf, table, columnName, operator);
							continue;
						}
						String key = lo + i++;
						paramMap.put(key, value);
						buf.append(columnName).append(" ").append(operator).append(" :").append(key);
					}
					buf.append(")");
					continue;
				}

				// case: in ('x', 'y', 'z')
				String key = lo + i;
				paramMap.put(key, rightOperand);
				if ("=".equals(operator))
					operator = "in";
				else if ("<>".equals(operator))
					operator = "not in";
				buf.append(columnName).append(" ").append(operator).append(" (:").append(key).append(")");
			}
		}

		if (!ValueUtils.isEmpty(filters.getFilters())) {
			buf.append(i++ == 0 ? " where " : logicalOperator);
			int k = 0;
			for (Filters subFilters : filters.getFilters()) {
				buf.append(k++ == 0 ? "" : logicalOperator).append("(");
				i = appendWhere(buf, table, subFilters, i, paramMap);
				buf.append(")");
			}
		}

		return i;
	}

	private Object toParamValue(Object value, Class<?> type) {
		if (value == null)
			return null;
		if (value instanceof String && ((String) value).contains("%"))
			return value;
		if (!ValueUtils.isPrimitive(type))
			return value;
		value = ValueUtils.toRequiredType(value, type);
		return value instanceof Character ? value.toString() : value;
	}

	private void appendNullCondition(StringBuffer buf, Table table, String columnName, String operator) {
		buf.append(columnName);
		if ("=".equals(operator) || "in".equals(operator))
			operator = "is";
		else if ("<>".equals(operator) || "not in".equals(operator))
			operator = "is not";
		buf.append(" ").append(operator).append(" null");
	}

	@Override
	public <T> List<T> selectListByQl(String ql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception {
		ValueUtils.assertNotEmpty("ql", ql);
		ValueUtils.assertNotEmpty("requiredType", requiredType);
		paramMap = paramMap == null ? new HashMap<String, Object>() : paramMap;
		ql = ql.trim();
		if (getPreprocessor() != null)
			ql = getPreprocessor().process(ql, paramMap);
		ql = applyPagination(ql, paramMap, pageIndex, pageSize);
		adjustParamMap(paramMap);
		return query(ql, paramMap, requiredType, null, pageIndex, pageSize);
	}
	private static void adjustParamMap(Map<String, ?> paramMap) {
		if (paramMap == null || paramMap.isEmpty())
			return;
		List<String> charKeyList = null;
		for (String key : paramMap.keySet()) {
			Object value = paramMap.get(key);
			if (value == null)
				continue;
			if (value instanceof List) {
				@SuppressWarnings("unchecked")
				List<Object> list = (List<Object>) value;
				int size = list.size();
				for (int i = 0; i < size; i++) {
					Object item = list.get(i);
					if (item == null || !(item instanceof Character))
						continue;
					list.remove(i);
					list.add(i, item.toString());
				}
				continue;
			}
			if (!(value instanceof Character))
				continue;
			if (charKeyList == null)
				charKeyList = new ArrayList<String>();
			charKeyList.add(key);
		}
		if (charKeyList == null)
			return;
		@SuppressWarnings("unchecked")
		Map<String, Object> map = (Map<String, Object>) paramMap;
		for (String key : charKeyList)
			map.put(key, paramMap.get(key).toString());
	}

	@Override
	public <T> Page<T> selectPageByQl(String ql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception {
		paramMap = paramMap == null ? new HashMap<String, Object>() : paramMap;
		Page<T> page = new Page<T>();
		page.setIndex(pageIndex);
		page.setSize(pageSize);
		page.setList(selectListByQl(ql, paramMap, requiredType, pageIndex, pageSize));
		int forUpdateIndex = ql.toLowerCase().lastIndexOf("for update");
		if (forUpdateIndex > -1)
			ql = ql.substring(0, forUpdateIndex - 1);
		ql = "select count(*) from (" + ql + ")";
		page.setTotalSize(selectByQl(ql, paramMap, Integer.class));
		if (page.getIndex() >= 0 && page.getSize() > 0 && page.getTotalSize() > 0)
			page.setLastIndex((page.getTotalSize() / page.getSize()) - (page.getTotalSize() % page.getSize() == 0 ? 1 : 0));
		return page;
	}

	@Override
	public <T> List<T> selectListByQlPath(String qlPath, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize)
			throws Exception {
		return selectListByQl(getSqlByPath(qlPath), paramMap, requiredType, pageIndex, pageSize);
	}

	@Override
	public <T> Page<T> selectPageByQlPath(String qlPath, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize)
			throws Exception {
		return selectPageByQl(getSqlByPath(qlPath), paramMap, requiredType, pageIndex, pageSize);
	}

	private Map<String, String> sqlByPathCache;
	private String getSqlByPath(final String path) throws IOException {
		ValueUtils.assertNotNull("path", path);
		if (sqlByPathCache == null)
			return _getSqlByPath(path);
		if (sqlByPathCache.containsKey(path))
			return sqlByPathCache.get(path);
		return SyncCtrlUtils.wrap("DmlJdbc.sqlByPathCache." + path, sqlByPathCache, path, new Closure<String, IOException>() {
			@Override
			public String execute() throws IOException {
				if (sqlByPathCache.containsKey(path))
					return sqlByPathCache.get(path);
				return _getSqlByPath(path);
			}
		});
	}
	private String _getSqlByPath(String path) throws IOException {
		String _path = path;
		if (_path.endsWith("/") || ResourceUtils.isDirectory(_path)) {
			if (!_path.endsWith("/"))
				_path += "/";
			if (ResourceUtils.exists(_path + getDbType() + ".sql"))
				path = _path + getDbType() + ".sql";
			else if (ResourceUtils.exists(_path + "ansi.sql"))
				path = _path + "ansi.sql";
		}
		return ResourceUtils.readText(path);
	}

	@Override
	public int deleteList(Class<?> clazz, Object condition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("condition", condition);

		final Table table = getTable(clazz);
		Query query = toQuery(table, condition);

		StringBuffer buf = new StringBuffer("delete");
		@SuppressWarnings("unchecked")
		Map<String, Object> paramMap = new ListOrderedMap();
		appendFromWhere(table, query, false, buf, paramMap);

		return this.namedParameterJdbcOperations.update(buf.toString(), paramMap);
	}

	@Override
	public int executeByQl(String ql, Map<String, ?> paramMap) throws Exception {
		ValueUtils.assertNotEmpty("ql", ql);
		paramMap = paramMap == null ? new HashMap<String, Object>() : paramMap;
		ql = ql.trim();
		if (getPreprocessor() != null)
			ql = getPreprocessor().process(ql, paramMap);
		adjustParamMap(paramMap);
		return this.namedParameterJdbcOperations.update(ql, paramMap);
	}

	@Override
	public int executeByQlPath(String qlPath, Map<String, ?> paramMap) throws Exception {
		return executeByQl(getSqlByPath(qlPath), paramMap);
	}

	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
		if (ValueUtils.isEmpty(domain)) {
			domainList.clear();
			return;
		}
		for (String d : StringUtils.tokenizeToStringArray(domain, ","))
			domainList.add(d);
	}
	public DataSource getDataSource() {
		return dataSource;
	}
	public void setDataSource(DataSource dataSource) {
		this.dataSource = dataSource;
	}
	public JdbcOperations getJdbcOperations() {
		return jdbcOperations;
	}
	public void setJdbcOperations(JdbcOperations jdbcOperations) {
		this.jdbcOperations = jdbcOperations;
	}
	public NamedParameterJdbcOperations getNamedParameterJdbcOperations() {
		return namedParameterJdbcOperations;
	}
	public void setNamedParameterJdbcOperations(NamedParameterJdbcOperations namedParameterJdbcOperations) {
		this.namedParameterJdbcOperations = namedParameterJdbcOperations;
	}
	public int getMaxSqlByPathCacheSize() {
		return maxSqlByPathCacheSize;
	}
	public void setMaxSqlByPathCacheSize(int maxSqlByPathCacheSize) {
		this.maxSqlByPathCacheSize = maxSqlByPathCacheSize;
	}

	private Map<String, Class<?>> classByTableNameCache = new ConcurrentHashMap<String, Class<?>>();
	@Override
	public Class<?> getClass(String tableName) {
		final String _name = tableName.toLowerCase();

		if (classByTableNameCache.containsKey(_name))
			return classByTableNameCache.get(_name);

		return SyncCtrlUtils.wrap("DmlJdbc.classByTableName." + tableName, classByTableNameCache, tableName,
				new Closure<Class<?>, RuntimeException>() {
					@Override
					public Class<?> execute() {
						Table table = new Table();

						checkAndPopulateDomainAndName(table, _name);

						ClassPool pool = ClassPool.getDefault();
						CtClass cc = pool.makeClass("org.dbist.virtual." + ValueUtils.toCamelCase(_name, '_', true));
						for (TableColumn tableColumn : getTableColumnList(table)) {
							try {
								cc.addField(new CtField(toCtClass(tableColumn.getDataType()), ValueUtils.toCamelCase(tableColumn.getName(), '_'), cc));
							} catch (CannotCompileException e) {
								throw new DbistRuntimeException(e);
							} catch (NotFoundException e) {
								throw new DbistRuntimeException(e);
							}
						}

						try {
							return cc.toClass();
						} catch (CannotCompileException e) {
							throw new DbistRuntimeException(e);
						}
					}
				});
	}
	private static final Map<String, CtClass> CTCLASS_BY_DBDATATYPE_MAP;
	static {
		CTCLASS_BY_DBDATATYPE_MAP = new HashMap<String, CtClass>();
		ClassPool pool = ClassPool.getDefault();
		try {
			CTCLASS_BY_DBDATATYPE_MAP.put("number", pool.get(BigDecimal.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("int", pool.get(BigDecimal.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("bigint", pool.get(BigDecimal.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("smallint", pool.get(BigDecimal.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("tinyint", pool.get(BigDecimal.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("float", pool.get(BigDecimal.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("money", pool.get(BigDecimal.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("smallmoney", pool.get(BigDecimal.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("numeric", pool.get(BigDecimal.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("decimal", pool.get(BigDecimal.class.getName()));

			CTCLASS_BY_DBDATATYPE_MAP.put("date", pool.get(Date.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("datetime", pool.get(Date.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("datetime2", pool.get(Date.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("smalldatetime", pool.get(Date.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("time", pool.get(Date.class.getName()));
			CTCLASS_BY_DBDATATYPE_MAP.put("timestamp", pool.get(Date.class.getName()));
		} catch (NotFoundException e) {
			logger.warn(e.getMessage(), e);
		}
	}
	private static CtClass toCtClass(String dbDataType) throws NotFoundException {
		if (CTCLASS_BY_DBDATATYPE_MAP.containsKey(dbDataType))
			return CTCLASS_BY_DBDATATYPE_MAP.get(dbDataType);
		return ClassPool.getDefault().getCtClass(String.class.getName());
	}

	private Map<Class<?>, Table> tableByClassCache = new ConcurrentHashMap<Class<?>, Table>();
	@Override
	public Table getTable(Object obj) {
		final Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();

		final boolean debug = logger.isDebugEnabled();

		if (tableByClassCache.containsKey(clazz)) {
			if (debug)
				logger.debug("get table metadata from map cache by class: " + clazz.getName());
			return tableByClassCache.get(clazz);
		}

		return SyncCtrlUtils.wrap("DmlJdbc.tableByClass." + clazz.getName(), tableByClassCache, clazz, new Closure<Table, RuntimeException>() {
			@Override
			public Table execute() {
				if (debug)
					logger.debug("make table metadata by class: " + clazz.getName());
				Table table = new Table();
				table.setClazz(clazz);

				// Domain and Name
				org.dbist.annotation.Table tableAnn = clazz.getAnnotation(org.dbist.annotation.Table.class);
				if (tableAnn != null) {
					if (!ValueUtils.isEmpty(tableAnn.domain()))
						table.setDomain(tableAnn.domain().toLowerCase());
					if (!ValueUtils.isEmpty(tableAnn.name()))
						table.setName(tableAnn.name().toLowerCase());
				}

				String simpleName = clazz.getSimpleName();
				String[] tableNameCandidates = ValueUtils.isEmpty(table.getName()) ? new String[] { ValueUtils.toDelimited(simpleName, '_', false),
						ValueUtils.toDelimited(simpleName, '_', true), simpleName.toLowerCase() } : new String[] { table.getName() };
				checkAndPopulateDomainAndName(table, tableNameCandidates);

				// Columns
				for (Field field : ReflectionUtils.getFieldList(clazz, false))
					addColumn(table, field);

				return table;
			}
		});
	}

	private static final String QUERY_NUMBEROFTABLE_MYSQL = "select count(*) from information_schema.tables where lcase(table_schema) = '${domain}' and lcase(table_name) = ?";
	private static final String QUERY_NUMBEROFTABLE_ORACLE = "select count(*) from all_tables where lower(owner) = '${domain}' and lower(table_name) = ?";
	private static final String QUERY_NUMBEROFTABLE_SQLSERVER = "select count(*) from ${domain}.sysobjects where xtype = 'U' and lower(name) = ?";
	private static final Map<String, String> QUERY_NUMBEROFTABLE_MAP;
	static {
		QUERY_NUMBEROFTABLE_MAP = new HashMap<String, String>();
		QUERY_NUMBEROFTABLE_MAP.put(DBTYPE_MYSQL, QUERY_NUMBEROFTABLE_MYSQL);
		QUERY_NUMBEROFTABLE_MAP.put(DBTYPE_ORACLE, QUERY_NUMBEROFTABLE_ORACLE);
		QUERY_NUMBEROFTABLE_MAP.put(DBTYPE_SQLSERVER, QUERY_NUMBEROFTABLE_SQLSERVER);
	}

	private static final String QUERY_PKCOLUMNS_MYSQL = "select lower(column_name) name from information_schema.key_column_usage"
			+ " where table_schema = '${domain}' and table_name = ? and constraint_name = 'PRIMARY' order by ordinal_position";
	private static final String QUERY_PKCOLUMNS_ORACLE = "select lower(conscol.column_name) name from all_constraints cons, all_cons_columns conscol"
			+ " where cons.constraint_name = conscol.constraint_name and cons.owner = conscol.owner and lower(conscol.owner) = '${domain}' and lower(conscol.table_name) = ? and cons.constraint_type = 'P' order by conscol.position";
	private static final String QUERY_PKCOLUMNS_SQLSERVER = "select lower(col.name) name from ${domain}.sysobjects tbl, ${domain}.syscolumns col"
			+ " where tbl.xtype = 'U' and lower(tbl.name) = ? and col.id = tbl.id and col.typestat = 3 order by colorder";
	private static final Map<String, String> QUERY_PKCOLUMNS_MAP;
	static {
		QUERY_PKCOLUMNS_MAP = new HashMap<String, String>();
		QUERY_PKCOLUMNS_MAP.put(DBTYPE_MYSQL, QUERY_PKCOLUMNS_MYSQL);
		QUERY_PKCOLUMNS_MAP.put(DBTYPE_ORACLE, QUERY_PKCOLUMNS_ORACLE);
		QUERY_PKCOLUMNS_MAP.put(DBTYPE_SQLSERVER, QUERY_PKCOLUMNS_SQLSERVER);
	}

	private static final String MSG_QUERYNOTFOUND = "Couldn't find ${queryName} query of dbType: ${dbType}. this type maybe unsupported yet.";

	//	private static final
	private <T> Table checkAndPopulateDomainAndName(Table table, String... tableNameCandidates) {
		// Check table existence and populate
		String sql = QUERY_NUMBEROFTABLE_MAP.get(getDbType());
		if (sql == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: number of table", "dbType:" + getDbType())));

		List<String> domainNameList = ValueUtils.isEmpty(table.getDomain()) ? this.domainList : ValueUtils.toList(table.getDomain());

		boolean populated = false;
		for (String domainName : domainNameList) {
			domainName = domainName.toLowerCase();
			String _sql = StringUtils.replace(sql, "${domain}", domainName);
			for (String tableName : tableNameCandidates) {
				if (jdbcOperations.queryForInt(_sql, tableName) > 0) {
					table.setDomain(domainName);
					table.setName(tableName);
					populated = true;
					break;
				}
			}
		}

		if (!populated) {
			String errMsg = "Couldn't find table[${table}] from this(these) domain(s)[${domain}]";
			throw new IllegalArgumentException(ValueUtils.populate(errMsg,
					ValueUtils.toMap("domain:" + mapOr(domainNameList), "table:" + mapOr(tableNameCandidates))));
		}

		// populate PK name
		sql = QUERY_PKCOLUMNS_MAP.get(getDbType());
		if (sql == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: primary key", "dbType:" + getDbType())));
		sql = StringUtils.replace(sql, "${domain}", table.getDomain());
		table.setPkColumnNameList(jdbcOperations.queryForList(sql, String.class, table.getName()));

		return table;
	}

	private static final RowMapper<TableColumn> TABLECOLUMN_ROWMAPPER = new TableColumnRowMapper();

	private static final String QUERY_COLUMNS_MYSQL = "select lower(column_name) name, data_type dataType from information_schema.columns where lower(table_schema) = '${domain}' and lower(table_name) = ?";
	private static final String QUERY_COLUMNS_ORACLE = "select lower(column_name) name, lower(data_type) dataType from all_tab_columns where lower(owner) = '${domain}' and lower(table_name) = ?";
	private static final String QUERY_COLUMNS_SQLSERVER = "select lower(col.name) name, lower(type.name) dataType from ${domain}.sysobjects tbl, ${domain}.syscolumns col, ${domain}.systypes type"
			+ " where tbl.xtype = 'U' and lower(tbl.name) = ? and col.id = tbl.id and col.xusertype = type.xusertype";
	private static final Map<String, String> QUERY_COLUMNS_MAP;
	static {
		QUERY_COLUMNS_MAP = new HashMap<String, String>();
		QUERY_COLUMNS_MAP.put(DBTYPE_MYSQL, QUERY_COLUMNS_MYSQL);
		QUERY_COLUMNS_MAP.put(DBTYPE_ORACLE, QUERY_COLUMNS_ORACLE);
		QUERY_COLUMNS_MAP.put(DBTYPE_SQLSERVER, QUERY_COLUMNS_SQLSERVER);
	}
	private List<TableColumn> getTableColumnList(Table table) {
		String sql = QUERY_COLUMNS_MAP.get(getDbType());
		if (sql == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: table columns", "dbType:" + getDbType())));
		sql = StringUtils.replace(sql, "${domain}", table.getDomain());

		String tableName = table.getName();

		return jdbcOperations.query(sql, new Object[] { tableName }, TABLECOLUMN_ROWMAPPER);
	}

	private static final String QUERY_COLUMN_MYSQL = "select lower(column_name) name, data_type dataType from information_schema.columns where lower(table_schema) = '${domain}' and lower(table_name) = ? and lower(column_name) = ?";
	private static final String QUERY_COLUMN_ORACLE = "select lower(column_name) name, lower(data_type) dataType from all_tab_columns where lower(owner) = '${domain}' and lower(table_name) = ? and lower(column_name) = ?";
	private static final String QUERY_COLUMN_SQLSERVER = "select lower(col.name) name, lower(type.name) dataType from ${domain}.sysobjects tbl, ${domain}.syscolumns col, ${domain}.systypes type"
			+ " where tbl.xtype = 'U' and lower(tbl.name) = ? and col.id = tbl.id and col.xusertype = type.xusertype and lower(col.name) = ?";
	private static final Map<String, String> QUERY_COLUMN_MAP;
	static {
		QUERY_COLUMN_MAP = new HashMap<String, String>();
		QUERY_COLUMN_MAP.put(DBTYPE_MYSQL, QUERY_COLUMN_MYSQL);
		QUERY_COLUMN_MAP.put(DBTYPE_ORACLE, QUERY_COLUMN_ORACLE);
		QUERY_COLUMN_MAP.put(DBTYPE_SQLSERVER, QUERY_COLUMN_SQLSERVER);
	}
	private static final String MSG_COLUMNNOTFOUND = "Couldn't find column[${column}] of table[${table}].";
	private void addColumn(Table table, Field field) {
		Ignore ignoreAnn = field.getAnnotation(Ignore.class);
		if (ignoreAnn != null)
			return;

		String sql = QUERY_COLUMN_MAP.get(getDbType());
		if (sql == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: table column", "dbType:" + getDbType())));
		sql = StringUtils.replace(sql, "${domain}", table.getDomain());

		String tableName = table.getName();

		Column column = table.addColumn(new Column());
		column.setField(field);
		column.setGetter(ReflectionUtils.getGetter(table.getClazz(), field.getName(), field.getType()));
		column.setSetter(ReflectionUtils.getSetter(table.getClazz(), field.getName(), field.getType()));
		org.dbist.annotation.Column columnAnn = field.getAnnotation(org.dbist.annotation.Column.class);
		TableColumn tabColumn = null;

		if (columnAnn != null) {
			if (!ValueUtils.isEmpty(columnAnn.name())) {
				try {
					tabColumn = jdbcOperations.queryForObject(sql, TABLECOLUMN_ROWMAPPER, tableName, columnAnn.name());
				} catch (EmptyResultDataAccessException e) {
					throw new DbistRuntimeException(ValueUtils.populate(MSG_COLUMNNOTFOUND,
							ValueUtils.toMap("column:" + columnAnn.name(), "table:" + table.getDomain() + "." + tableName)));
				}
			}
			column.setType(ValueUtils.toNull(columnAnn.type().value()));
		}
		if (tabColumn == null) {
			String[] columnNameCandidates = new String[] { ValueUtils.toDelimited(field.getName(), '_').toLowerCase(),
					ValueUtils.toDelimited(field.getName(), '_', true).toLowerCase(), field.getName().toLowerCase() };
			Set<String> checkedSet = new HashSet<String>();
			for (String columnName : columnNameCandidates) {
				if (checkedSet.contains(columnName))
					continue;
				try {
					tabColumn = jdbcOperations.queryForObject(sql, TABLECOLUMN_ROWMAPPER, tableName, columnName);
				} catch (EmptyResultDataAccessException e) {
					checkedSet.add(columnName);
					continue;
				}
				break;
			}
			if (tabColumn == null)
				throw new DbistRuntimeException(ValueUtils.populate(MSG_COLUMNNOTFOUND,
						ValueUtils.toMap("column:" + mapOr(columnNameCandidates), "table:" + table.getDomain() + "." + tableName)));
		}

		column.setName(tabColumn.getName());
		column.setPrimaryKey(table.getPkColumnNameList().contains(tabColumn.getName()));
		column.setDataType(tabColumn.getDataType().toLowerCase());
	}

	static class TableColumnRowMapper implements RowMapper<TableColumn> {
		@Override
		public TableColumn mapRow(ResultSet rs, int rowNum) throws SQLException {
			TableColumn tabColumn = new TableColumn();
			tabColumn.setName(rs.getString("name"));
			tabColumn.setDataType(rs.getString("dataType"));
			return tabColumn;
		}
	}

	static class TableColumn {
		private String name;
		private String dataType;
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getDataType() {
			return dataType;
		}
		public void setDataType(String type) {
			this.dataType = type;
		}
	}

	private static String mapOr(String... values) {
		StringBuffer buf = new StringBuffer();
		int i = 0;
		for (String value : values) {
			buf.append(i == 0 ? "" : i == values.length - 1 ? " or " : ", ");
			buf.append(value);
			i++;
		}
		return buf.toString();
	}
	private static String mapOr(List<String> valueList) {
		StringBuffer buf = new StringBuffer();
		int i = 0;
		for (String value : valueList) {
			buf.append(i == 0 ? "" : i == valueList.size() - 1 ? " or " : ", ");
			buf.append(value);
			i++;
		}
		return buf.toString();
	}

}
