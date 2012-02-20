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
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

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
import org.dbist.dml.Query;
import org.dbist.exception.DbistRuntimeException;
import org.dbist.metadata.Column;
import org.dbist.metadata.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
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
	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		ValueUtils.assertNotEmpty("domain", getDomain());
		ValueUtils.assertNotEmpty("jdbcTemplate", getJdbcTemplate());
		ValueUtils.assertNotEmpty("namedParameterJdbcTemplate", getNamedParameterJdbcTemplate());
		DatabaseMetaData metadata = jdbcTemplate.getDataSource().getConnection().getMetaData();
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
		update(sql, paramMap);
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
		update(sql, paramMap);
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
		update(sql, paramMap);
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

	private void update(String sql, Map<String, ?> paramMap) {
		this.namedParameterJdbcTemplate.update(sql, paramMap);
	}

	@SuppressWarnings("unchecked")
	private void updateBatch(String sql, List<Map<String, ?>> paramMapList) {
		this.namedParameterJdbcTemplate.batchUpdate(sql, paramMapList.toArray(new Map[paramMapList.size()]));
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

	private void populateFromWhere(Table table, Query query, StringBuffer buf, Map<String, Object> paramMap) {
		// From
		buf.append(" from ").append(table.getDomain()).append(".").append(table.getName());

		// Where
		populateWhere(buf, table, query, 0, paramMap);
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
		populateFromWhere(table, query, buf, paramMap);

		return this.namedParameterJdbcTemplate.queryForInt(buf.toString(), paramMap);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> selectList(final Class<T> clazz, Object condition) throws Exception {
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

		Map<String, Object> paramMap = new ListOrderedMap();
		populateFromWhere(table, query, buf, paramMap);

		// Order by
		if (!ValueUtils.isEmpty(query.getOrder())) {
			buf.append(" order by");
			int i = 0;
			for (Order order : query.getOrder())
				buf.append(i++ == 0 ? " " : ", ").append(table.toColumnName(order.getField())).append(order.isAscending() ? " asc" : " desc");
		}

		List<T> list = null;
		if (DBTYPE_PAGINATIONQUERYSUPPORTED_LIST.contains(dbType) || query.getPageIndex() < 0 || query.getPageSize() <= 0) {
			list = this.namedParameterJdbcTemplate.query(buf.toString(), paramMap, new RowMapper<T>() {
				@Override
				public T mapRow(ResultSet rs, int rowNum) throws SQLException {
					return newInstance(table, columnNameList, rs, clazz);
				}
			});
		} else {
			list = this.namedParameterJdbcTemplate.query(buf.toString(), paramMap, new ResultSetExtractor<List<T>>() {
				@Override
				public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<T> list = new ArrayList<T>();
					int firstRowIndex = query.getPageIndex() * query.getPageSize();
					for (int i = 0; i < firstRowIndex; i++)
						rs.next();
					while (rs.next()) {
						list.add(newInstance(table, columnNameList, rs, clazz));
					}
					return list;
				}
			});
		}
		return list;
	}
	private static <T> T newInstance(Table table, List<String> columnNameList, ResultSet rs, Class<T> clazz) throws SQLException {
		T data = newInstance(clazz);

		// Select all fields
		if (columnNameList.isEmpty()) {
			for (Column column : table.getColumnList())
				setFieldValue(data, rs, column);
			return data;
		}

		// Select some fields
		for (String columnName : columnNameList)
			setFieldValue(data, rs, table.getColumn(columnName));
		return data;
	}

	private static void setFieldValue(Object data, ResultSet rs, Column column) throws SQLException {
		Field field = column.getField();
		try {
			field.set(data, toRequiredType(rs, column.getName(), field.getType()));
		} catch (IllegalArgumentException e) {
			throw new DbistRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new DbistRuntimeException(e);
		}
	}
	private static Object toRequiredType(ResultSet rs, String name, Class<?> requiredType) throws SQLException {
		if (ValueUtils.isPrimitive(requiredType)) {
			if (requiredType.equals(String.class))
				return rs.getString(name);
			if (requiredType.equals(Character.class) || requiredType.equals(char.class))
				return rs.getDouble(name);
			if (requiredType.equals(BigDecimal.class))
				return rs.getBigDecimal(name);
			if (requiredType.equals(Date.class))
				return rs.getTimestamp(name);
			if (requiredType.equals(Double.class) || requiredType.equals(double.class))
				return rs.getDouble(name);
			if (requiredType.equals(Float.class) || requiredType.equals(float.class))
				return rs.getFloat(name);
			if (requiredType.equals(Long.class) || requiredType.equals(long.class))
				return rs.getLong(name);
			if (requiredType.equals(Integer.class) || requiredType.equals(int.class))
				return rs.getInt(name);
			if (requiredType.equals(Boolean.class) || requiredType.equals(boolean.class))
				return rs.getBoolean(name);
			if (requiredType.equals(Byte[].class) || requiredType.equals(byte[].class))
				return rs.getBytes(name);
			if (requiredType.equals(Byte.class) || requiredType.equals(byte.class))
				return rs.getByte(name);
		}
		return rs.getObject(name);
	}
	private int populateWhere(StringBuffer buf, Table table, Filters filters, int i, Map<String, Object> paramMap) {
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
				i = populateWhere(buf, table, subFilters, i, paramMap);
				buf.append(")");
			}
		}

		return i;
	}

	@Override
	public <T> List<T> selectListWithLock(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> selectList(String query, Map<String, Object> paramMap, T requiredType, int pageIndex, int pageSize) throws Exception {
		// TODO Auto-generated method stub
		return null;
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
		populateFromWhere(table, query, buf, paramMap);

		return this.namedParameterJdbcTemplate.update(buf.toString(), paramMap);
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
	public JdbcTemplate getJdbcTemplate() {
		return jdbcTemplate;
	}
	public void setJdbcTemplate(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}
	public NamedParameterJdbcTemplate getNamedParameterJdbcTemplate() {
		return namedParameterJdbcTemplate;
	}
	public void setNamedParameterJdbcTemplate(NamedParameterJdbcTemplate namedParameterJdbcTemplate) {
		this.namedParameterJdbcTemplate = namedParameterJdbcTemplate;
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
		String query = QUERY_NUMBEROFTABLE_MAP.get(dbType);
		if (query == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: number of table", "dbType:" + dbType)));

		String simpleName = clazz.getSimpleName();

		List<String> domainNameList = ValueUtils.isEmpty(table.getDomain()) ? this.domainList : ValueUtils.toList(table.getDomain());
		List<String> tableNameList = ValueUtils.isEmpty(table.getName()) ? ValueUtils.toList(ValueUtils.toDelimited(simpleName, '_', false),
				ValueUtils.toDelimited(simpleName, '_', true), simpleName.toLowerCase()) : ValueUtils.toList(table.getName());

		boolean populated = false;
		for (String domainName : domainNameList) {
			domainName = domainName.toLowerCase();
			String sql = StringUtils.replace(query, "${domain}", domainName);
			for (String tableName : tableNameList) {
				if (jdbcTemplate.queryForInt(sql, tableName) > 0) {
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
		query = QUERY_PKCOLUMNS_MAP.get(dbType);
		if (query == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND, ValueUtils.toMap("queryName: primary key", "dbType:" + dbType)));
		query = StringUtils.replace(query, "${domain}", table.getDomain());
		table.setPkColumnNameList(jdbcTemplate.queryForList(query, String.class, table.getName()));

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

		String query = QUERY_COLUMNS_MAP.get(dbType);
		if (query == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: table columns", "dbType:" + dbType)));
		query = StringUtils.replace(query, "${domain}", table.getDomain());

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
				tabColumn = jdbcTemplate.queryForObject(query, rowMapper, tableNamae, columnAnn.name());
				if (tabColumn == null)
					throw new DbistRuntimeException(ValueUtils.populate(MSG_COLUMNNOTFOUND,
							ValueUtils.toMap("column:" + columnAnn.name(), "table:" + table.getDomain() + "." + tableNamae)));
			}
			column.setType(ValueUtils.toNull(columnAnn.type().value()));
		}
		if (tabColumn == null) {
			String columnName = ValueUtils.toDelimited(field.getName(), '_').toLowerCase();
			String columnName1 = field.getName().toLowerCase();
			tabColumn = jdbcTemplate.queryForObject(query, rowMapper, tableNamae, columnName);
			if (tabColumn == null) {
				if (columnName.equals(columnName1))
					throw new DbistRuntimeException(ValueUtils.populate(MSG_COLUMNNOTFOUND,
							ValueUtils.toMap("column:" + columnName, "table:" + table.getDomain() + "." + tableNamae)));
				tabColumn = jdbcTemplate.queryForObject(query, rowMapper, tableNamae, columnName1);
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
