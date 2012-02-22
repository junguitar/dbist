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

import java.lang.reflect.Field;
import java.math.BigDecimal;
import java.sql.DatabaseMetaData;
import java.sql.Date;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javax.sql.DataSource;

import net.sf.common.util.Closure;
import net.sf.common.util.ReflectionUtils;
import net.sf.common.util.SyncCtrlUtils;
import net.sf.common.util.ValueUtils;

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
	private static final List<String> DBTYPE_SUPPORTED_LIST = ValueUtils.toList(DBTYPE_MYSQL, DBTYPE_ORACLE);
	private static final List<String> DBTYPE_PAGINATIONQUERYSUPPORTED_LIST = ValueUtils.toList(DBTYPE_MYSQL, DBTYPE_ORACLE);
	//	private static final List<String> DBTYPE_SUPPORTED_LIST = ValueUtils.toList("hsqldb", "mysql", "postgresql", "oracle", "sqlserver", "db2");

	private String dbType;
	private String domain;
	private List<String> domainList = new ArrayList<String>(2);
	private DataSource dataSource;
	private JdbcOperations jdbcOperations;
	private NamedParameterJdbcOperations namedParameterJdbcOperations;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		ValueUtils.assertNotEmpty("domain", getDomain());
		ValueUtils.assertNotEmpty("dataSource", getDataSource());
		ValueUtils.assertNotEmpty("jdbcOperations", getJdbcOperations());
		ValueUtils.assertNotEmpty("namedParameterJdbcOperations", getNamedParameterJdbcOperations());
		DatabaseMetaData metadata = dataSource.getConnection().getMetaData();
		dbType = metadata.getDatabaseProductName().toLowerCase();
		if (!DBTYPE_SUPPORTED_LIST.contains(dbType))
			throw new IllegalArgumentException("Unsupported dbType: " + getDbType());
	}

	@Override
	public <T> void insert(T data) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		String sql = table.getInsertSql();
		Map<String, ?> paramMap = toParamMap(table, data);
		update(sql, paramMap);
	}

	@Override
	public <T> void insertBatch(List<T> list) throws Exception {
		if (ValueUtils.isEmpty(list))
			return;
		Table table = getTable(list.get(0));
		String sql = table.getInsertSql();
		List<Map<String, ?>> paramMapList = toParamMapList(table, list);
		updateBatch(sql, paramMapList);
	}

	@Override
	public <T> void update(T data) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		String sql = table.getUpdateSql();
		Map<String, ?> paramMap = toParamMap(table, data);
		// TODO DataNotFoundException message
		if (update(sql, paramMap) != 1)
			throw new DataNotFoundException("Couldn't find data for update " + data.getClass().getName());
	}

	@Override
	public <T> void updateBatch(List<T> list) throws Exception {
		if (ValueUtils.isEmpty(list))
			return;
		Table table = getTable(list.get(0));
		String sql = table.getUpdateSql();
		List<Map<String, ?>> paramMapList = toParamMapList(table, list);
		updateBatch(sql, paramMapList);
	}

	@Override
	public <T> void update(T data, String... fieldNames) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		String sql = table.getUpdateSql(fieldNames);
		Map<String, ?> paramMap = toParamMap(table, data, fieldNames);
		// TODO DataNotFoundException message
		if (update(sql, paramMap) != 1)
			throw new DataNotFoundException("Couldn't find data for update " + data.getClass().getName());
	}

	@Override
	public <T> void updateBatch(List<T> list, String... fieldNames) throws Exception {
		if (ValueUtils.isEmpty(list))
			return;
		Table table = getTable(list.get(0));
		String sql = table.getUpdateSql(fieldNames);
		List<Map<String, ?>> paramMapList = toParamMapList(table, list, fieldNames);
		updateBatch(sql, paramMapList);
	}

	@Override
	public <T> void delete(T data) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		String sql = table.getDeleteSql();
		Map<String, ?> paramMap = toParamMap(table, data, table.getPkFieldNames());
		// TODO DataNotFoundException message
		if (update(sql, paramMap) != 1)
			throw new DataNotFoundException("Couldn't find data for update " + data.getClass().getName());
	}

	@Override
	public <T> void deleteBatch(List<T> list) throws Exception {
		if (ValueUtils.isEmpty(list))
			return;
		Table table = getTable(list.get(0));
		String sql = table.getDeleteSql();
		List<Map<String, ?>> paramMapList = toParamMapList(table, list, table.getPkFieldNames());
		updateBatch(sql, paramMapList);
	}

	private int update(String sql, Map<String, ?> paramMap) {
		return this.namedParameterJdbcOperations.update(sql, paramMap);
	}

	@SuppressWarnings("unchecked")
	private int[] updateBatch(String sql, List<Map<String, ?>> paramMapList) {
		return this.namedParameterJdbcOperations.batchUpdate(sql, paramMapList.toArray(new Map[paramMapList.size()]));
	}

	private <T> List<Map<String, ?>> toParamMapList(Table table, List<T> list, String... fieldNames) throws Exception {
		List<Map<String, ?>> paramMapList = new ArrayList<Map<String, ?>>();
		for (T data : list)
			paramMapList.add(toParamMap(table, data, fieldNames));
		return paramMapList;
	}
	private <T> Map<String, ?> toParamMap(Table table, T data, String... fieldNames) throws Exception {
		@SuppressWarnings("unchecked")
		Map<String, Object> paramMap = new ListOrderedMap();

		// All fields
		if (ValueUtils.isEmpty(fieldNames)) {
			for (Column column : table.getColumnList()) {
				Field field = column.getField();
				paramMap.put(field.getName(), field.get(data));
			}
			return paramMap;
		}

		// Some fields
		for (String fieldName : fieldNames) {
			Field field = table.getField(fieldName);
			if (field == null)
				throw new DbistRuntimeException("Couldn't find column of table[" + table.getDomain() + "." + table.getName() + "] by fieldName["
						+ fieldName + "]");
			paramMap.put(fieldName, field.get(data));
		}
		return paramMap;
	}

	private void appendFromWhere(Table table, Query query, boolean lock, StringBuffer buf, Map<String, Object> paramMap) {
		// From
		buf.append(" from ").append(table.getDomain()).append(".").append(table.getName());

		// Where
		appendWhere(buf, table, query, 0, paramMap);
	}

	@Override
	public <T> int selectSize(Class<T> clazz, Object condition) throws Exception {
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
		if (ValueUtils.isEmpty(query.getField())) {
			int i = 0;
			for (Column column : table.getColumnList())
				buf.append(i++ == 0 ? " " : ", ").append(column.getName());
		} else {
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

		// Order by
		if (!ValueUtils.isEmpty(query.getOrder())) {
			buf.append(" order by");
			int i = 0;
			for (Order order : query.getOrder())
				buf.append(i++ == 0 ? " " : ", ").append(table.toColumnName(order.getField())).append(order.isAscending() ? " asc" : " desc");
		}

		appendLock(buf, lock);

		String sql = appyPagination(buf.toString(), paramMap, query.getPageIndex(), query.getPageSize());

		List<T> list = query(sql, paramMap, clazz, query.getPageIndex(), query.getPageSize());
		return list;
	}
	private void appendLock(StringBuffer buf, boolean lock) {
		buf.append(lock ? " for update" : "");
	}
	private String appyPagination(String sql, Map<String, ?> paramMap, int pageIndex, int pageSize) {
		if (pageIndex < 0 || pageSize <= 0)
			return sql;
		if (DBTYPE_PAGINATIONQUERYSUPPORTED_LIST.contains(dbType)) {
			int fromIndex = pageIndex * pageSize;
			int toIndex = fromIndex + pageSize;
			@SuppressWarnings("unchecked")
			Map<String, Object> _paramMap = (Map<String, Object>) paramMap;
			// Oracle
			if (DBTYPE_ORACLE.equals(dbType)) {
				String subsql = null;
				int forUpdateIndex = sql.toLowerCase().lastIndexOf("for update");
				if (forUpdateIndex > -1) {
					subsql = sql.substring(forUpdateIndex);
					sql = sql.substring(0, forUpdateIndex - 1);
				}

				StringBuffer buf = new StringBuffer(sql.length() + 100);
				if (pageIndex > 0) {
					_paramMap.put("__fromIndex", fromIndex);
					_paramMap.put("__toIndex", toIndex);
					buf.append("select * from (select data.*, rownum rownum_ from (").append(sql)
							.append(") data where rownum <= :__toIndex) where rownum_ > :__fromIndex");
				} else {
					_paramMap.put("__toIndex", toIndex);
					buf.append("select * from (").append(sql).append(") where rownum <= :__toIndex");
				}
				if (subsql != null)
					buf.append(subsql);
				return buf.toString();

			}
			// MySQL
			else if (DBTYPE_MYSQL.equals(dbType)) {
				if (pageIndex > 0) {
					_paramMap.put("__fromIndex", fromIndex);
					_paramMap.put("__toIndex", toIndex);
					return " limit :__fromIndex, :__toIndex";
				} else {
					_paramMap.put("__toIndex", toIndex);
					return " limit :__toIndex";
				}
			}
		}
		return sql;
	}
	private <T> List<T> query(String sql, Map<String, ?> paramMap, final Class<T> requiredType, final int pageIndex, final int pageSize)
			throws Exception {
		List<T> list = null;
		if (DBTYPE_PAGINATIONQUERYSUPPORTED_LIST.contains(dbType) || pageIndex < 0 || pageSize <= 0) {
			list = this.namedParameterJdbcOperations.query(sql, paramMap, new RowMapper<T>() {
				@Override
				public T mapRow(ResultSet rs, int rowNum) throws SQLException {
					return newInstance(rs, requiredType);
				}
			});
		} else {
			list = this.namedParameterJdbcOperations.query(sql, paramMap, new ResultSetExtractor<List<T>>() {
				@Override
				public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<T> list = new ArrayList<T>();
					int offset = pageIndex * pageSize;
					for (int i = 0; i < offset; i++)
						rs.next();
					while (rs.next())
						list.add(newInstance(rs, requiredType));
					return list;
				}
			});
		}
		return list;
	}
	private static Map<Class<?>, Map<String, Field>> classFieldCache = new ConcurrentHashMap<Class<?>, Map<String, Field>>();

	@SuppressWarnings("unchecked")
	private static <T> T newInstance(ResultSet rs, Class<T> clazz) throws SQLException {
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

		T data = newInstance(clazz);
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
				Field field;
				if (fieldCache.containsKey(name)) {
					field = fieldCache.get(name);
				} else {
					field = ReflectionUtils.getField(clazz, ValueUtils.toCamelCase(name, '_'));
					if (field == null) {
						for (Field f : ReflectionUtils.getFieldList(clazz, false)) {
							if (!f.getName().equalsIgnoreCase(name))
								continue;
							field = f;
							break;
						}
						if (field == null)
							field = ReflectionUtils.NULL_FIELD;
					}
					fieldCache.put(name, field);
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
			if (requiredType.equals(Character.class) || requiredType.equals(char.class))
				return rs.getDouble(index);
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
	private int appendWhere(StringBuffer buf, Table table, Filters filters, int i, Map<String, Object> paramMap) {
		String logicalOperator = " " + ValueUtils.toString(filters.getOperator(), "and").trim() + " ";

		int j = 0;
		if (!ValueUtils.isEmpty(filters.getFilter())) {
			for (Filter filter : filters.getFilter()) {
				String operator = ValueUtils.toString(filter.getOperator(), "=").trim();
				String lo = filter.getLeftOperand();
				buf.append(i++ == 0 ? " where " : j == 0 ? "" : logicalOperator).append(table.toColumnName(lo));
				j++;
				List<?> rightOperand = filter.getRightOperand();
				if (ValueUtils.isEmpty(rightOperand)) {
					if ("=".equals(operator))
						operator = "is";
					else if ("!=".equals(operator))
						operator = "is not";
					buf.append(" ").append(operator).append(" null");
				} else {
					String key = lo + i;
					if (rightOperand.size() == 1) {
						paramMap.put(key, rightOperand.get(0));
					} else {
						paramMap.put(key, rightOperand);
						if ("=".equals(operator))
							operator = "in";
						else if ("!=".equals(operator))
							operator = "not in";
					}
					buf.append(" ").append(operator).append(" :").append(key);
				}
			}
		}

		if (!ValueUtils.isEmpty(filters.getFilters())) {
			for (Filters subFilters : filters.getFilters()) {
				buf.append("(");
				i = appendWhere(buf, table, subFilters, i, paramMap);
				buf.append(")");
			}
		}

		return i;
	}

	@Override
	public <T> List<T> selectList(String sql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception {
		ValueUtils.assertNotEmpty("sql", sql);
		ValueUtils.assertNotEmpty("requiredType", requiredType);
		sql = sql.trim();
		sql = appyPagination(sql, paramMap, pageIndex, pageSize);
		return query(sql, paramMap, requiredType, pageIndex, pageSize);
	}

	@Override
	public <T> Page<T> selectPage(String sql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception {
		Page<T> page = new Page<T>();
		page.setIndex(pageIndex);
		page.setSize(pageSize);
		page.setList(selectList(sql, paramMap, requiredType, pageIndex, pageSize));
		int forUpdateIndex = sql.toLowerCase().lastIndexOf("for update");
		if (forUpdateIndex > -1)
			sql = sql.substring(0, forUpdateIndex - 1);
		sql = "select count(*) from (" + sql + ")";
		page.setTotalSize(select(sql, paramMap, Integer.class));
		if (page.getIndex() >= 0 && page.getSize() > 0 && page.getTotalSize() > 0)
			page.setLastIndex((page.getTotalSize() / page.getSize()) - (page.getTotalSize() % page.getSize() == 0 ? 1 : 0));
		return page;
	}

	@Override
	public <T> int deleteList(Class<T> clazz, Object condition) throws Exception {
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

	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType == null ? null : dbType.toLowerCase();
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
	public List<String> getDomainList() {
		return domainList;
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

	private static Map<Class<?>, Table> cache = new ConcurrentHashMap<Class<?>, Table>();
	@Override
	public Table getTable(Object obj) {
		final Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();

		final boolean debug = logger.isDebugEnabled();

		if (cache.containsKey(clazz)) {
			if (debug)
				logger.debug("get table metadata from map cache by class: " + clazz.getName());
			return cache.get(clazz);
		}

		return SyncCtrlUtils.wrap("Table." + clazz.getName(), cache, clazz, new Closure<Table, RuntimeException>() {
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
				checkAndPopulateDomainAndName(table, clazz);

				// Columns
				for (Field field : ReflectionUtils.getFieldList(clazz, false))
					addColumn(table, field);

				return table;
			}
		});
	}

	private static final String QUERY_NUMBEROFTABLE_MYSQL = "select count(*) from information_schema.tables where lcase(table_schema) = '${domain}' and lcase(table_name) = ?";
	private static final String QUERY_NUMBEROFTABLE_ORACLE = "select count(*) from all_tables where lower(owner) = '${domain}' and lower(table_name) = ?";
	private static final Map<String, String> QUERY_NUMBEROFTABLE_MAP;
	static {
		QUERY_NUMBEROFTABLE_MAP = new HashMap<String, String>();
		QUERY_NUMBEROFTABLE_MAP.put(DBTYPE_MYSQL, QUERY_NUMBEROFTABLE_MYSQL);
		QUERY_NUMBEROFTABLE_MAP.put(DBTYPE_ORACLE, QUERY_NUMBEROFTABLE_ORACLE);
	}

	// TODO QUERY_PKCOLUMNS_MYSQL
	private static final String QUERY_PKCOLUMNS_MYSQL = "";
	private static final String QUERY_PKCOLUMNS_ORACLE = "select lower(conscol.column_name) name from all_constraints cons, all_cons_columns conscol"
			+ " where cons.constraint_name = conscol.constraint_name and lower(conscol.owner) = '${domain}' and lower(conscol.table_name) = ? and cons.constraint_type = 'P'";
	private static final Map<String, String> QUERY_PKCOLUMNS_MAP;
	static {
		QUERY_PKCOLUMNS_MAP = new HashMap<String, String>();
		QUERY_PKCOLUMNS_MAP.put(DBTYPE_MYSQL, QUERY_PKCOLUMNS_MYSQL);
		QUERY_PKCOLUMNS_MAP.put(DBTYPE_ORACLE, QUERY_PKCOLUMNS_ORACLE);
	}

	private static final String MSG_QUERYNOTFOUND = "Couldn't find ${queryName} query of dbType: ${dbType}. this type maybe unsupported yet.";

	//	private static final
	private <T> Table checkAndPopulateDomainAndName(Table table, Class<T> clazz) {
		// Check table existence and populate
		String sql = QUERY_NUMBEROFTABLE_MAP.get(dbType);
		if (sql == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: number of table", "dbType:" + dbType)));

		String simpleName = clazz.getSimpleName();

		List<String> domainNameList = ValueUtils.isEmpty(table.getDomain()) ? this.domainList : ValueUtils.toList(table.getDomain());
		List<String> tableNameList = ValueUtils.isEmpty(table.getName()) ? ValueUtils.toList(ValueUtils.toDelimited(simpleName, '_', false),
				ValueUtils.toDelimited(simpleName, '_', true), simpleName.toLowerCase()) : ValueUtils.toList(table.getName());

		boolean populated = false;
		for (String domainName : domainNameList) {
			domainName = domainName.toLowerCase();
			String _sql = StringUtils.replace(sql, "${domain}", domainName);
			for (String tableName : tableNameList) {
				if (jdbcOperations.queryForInt(_sql, tableName) > 0) {
					table.setDomain(domainName);
					table.setName(tableName);
					populated = true;
					break;
				}
			}
		}

		if (!populated) {
			String errMsg = "Couldn't find table[${table}] of class[${class}] from this(these) domain(s)[${domain}]";
			throw new IllegalArgumentException(ValueUtils.populate(errMsg,
					ValueUtils.toMap("class:" + clazz.getSimpleName(), "domain:" + mapOr(domainNameList), "table:" + mapOr(tableNameList))));
		}

		// populate PK name
		sql = QUERY_PKCOLUMNS_MAP.get(dbType);
		if (sql == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND, ValueUtils.toMap("queryName: primary key", "dbType:" + dbType)));
		sql = StringUtils.replace(sql, "${domain}", table.getDomain());
		table.setPkColumnNameList(jdbcOperations.queryForList(sql, String.class, table.getName()));

		return table;
	}

	// TODO QUERY_COLUMNS_MYSQL
	private static final String QUERY_COLUMNS_MYSQL = "";
	private static final String QUERY_COLUMNS_ORACLE = "select lower(column_name) name, data_type dataType from all_tab_columns where lower(owner) = '${domain}' and lower(table_name) = ? and lower(column_name) = ?";
	private static final Map<String, String> QUERY_COLUMNS_MAP;
	static {
		QUERY_COLUMNS_MAP = new HashMap<String, String>();
		QUERY_COLUMNS_MAP.put(DBTYPE_MYSQL, QUERY_COLUMNS_MYSQL);
		QUERY_COLUMNS_MAP.put(DBTYPE_ORACLE, QUERY_COLUMNS_ORACLE);
	}
	private static final String MSG_COLUMNNOTFOUND = "Couldn't find column[${column}] of table[${table}].";
	private void addColumn(Table table, Field field) {
		Ignore ignoreAnn = field.getAnnotation(Ignore.class);
		if (ignoreAnn != null)
			return;

		String sql = QUERY_COLUMNS_MAP.get(dbType);
		if (sql == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: table columns", "dbType:" + dbType)));
		sql = StringUtils.replace(sql, "${domain}", table.getDomain());

		String tableNamae = table.getName();

		Column column = table.addColumn(new Column());
		column.setField(field);
		column.setGetter(ReflectionUtils.getGetter(table.getClazz(), field.getName(), field.getType()));
		column.setSetter(ReflectionUtils.getSetter(table.getClazz(), field.getName(), field.getType()));
		org.dbist.annotation.Column columnAnn = field.getAnnotation(org.dbist.annotation.Column.class);
		TabColumn tabColumn = null;
		RowMapper<TabColumn> rowMapper = new RowMapper<TabColumn>() {
			@Override
			public TabColumn mapRow(ResultSet rs, int rowNum) throws SQLException {
				TabColumn tabColumn = new TabColumn();
				tabColumn.setName(rs.getString("name"));
				tabColumn.setDataType(rs.getString("dataType"));
				return tabColumn;
			}
		};
		if (columnAnn != null) {
			if (!ValueUtils.isEmpty(columnAnn.name())) {
				tabColumn = jdbcOperations.queryForObject(sql, rowMapper, tableNamae, columnAnn.name());
				if (tabColumn == null)
					throw new DbistRuntimeException(ValueUtils.populate(MSG_COLUMNNOTFOUND,
							ValueUtils.toMap("column:" + columnAnn.name(), "table:" + table.getDomain() + "." + tableNamae)));
			}
			column.setType(ValueUtils.toNull(columnAnn.type().value()));
		}
		if (tabColumn == null) {
			String columnName = ValueUtils.toDelimited(field.getName(), '_').toLowerCase();
			String columnName1 = field.getName().toLowerCase();
			tabColumn = jdbcOperations.queryForObject(sql, rowMapper, tableNamae, columnName);
			if (tabColumn == null) {
				if (columnName.equals(columnName1))
					throw new DbistRuntimeException(ValueUtils.populate(MSG_COLUMNNOTFOUND,
							ValueUtils.toMap("column:" + columnName, "table:" + table.getDomain() + "." + tableNamae)));
				tabColumn = jdbcOperations.queryForObject(sql, rowMapper, tableNamae, columnName1);
				if (tabColumn == null)
					throw new DbistRuntimeException(ValueUtils.populate(MSG_COLUMNNOTFOUND,
							ValueUtils.toMap("column:" + columnName + " or " + columnName1, "table:" + table.getDomain() + "." + tableNamae)));
			}
		}

		column.setName(tabColumn.getName());
		column.setPrimaryKey(table.getPkColumnNameList().contains(tabColumn.getName()));
		column.setDataType(tabColumn.getDataType().toLowerCase());
	}

	class TabColumn {
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

	private static String mapOr(List<String> values) {
		StringBuffer buf = new StringBuffer();
		int i = 0;
		for (String value : values) {
			buf.append(i == 0 ? "" : i == values.size() - 1 ? " or " : ", ");
			buf.append(value);
			i++;
		}
		return buf.toString();
	}

}
