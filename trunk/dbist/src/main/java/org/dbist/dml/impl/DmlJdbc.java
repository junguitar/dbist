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
import java.sql.DatabaseMetaData;
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
import org.springframework.jdbc.core.JdbcTemplate;
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

	@SuppressWarnings("unchecked")
	@Override
	public <T> T select(T data) throws Exception {
		ValueUtils.assertNotNull("data", data);
		return select(selectList((Class<T>) data.getClass(), data));
	}
	@SuppressWarnings("unchecked")
	@Override
	public <T> T selectForUpdate(T data) throws Exception {
		ValueUtils.assertNotNull("data", data);
		return select(selectListForUpdate((Class<T>) data.getClass(), data));
	}

	@Override
	public <T> T insert(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void insertBatch(List<T> list) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T update(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void updateBatch(List<T> list) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T update(T data, String... fieldName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void updateBatch(List<T> list, String... filedName) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T upsert(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void upsertBatch(List<T> list) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T delete(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void deleteBatch(List<T> list) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> T delete(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int count(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> List<T> selectList(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		Table table = getTable(clazz);
		StringBuffer buf = new StringBuffer();
		Query query = condition instanceof Query ? (Query) condition : null;

		// Select
		buf.append("select");
		if (query == null || ValueUtils.isEmpty(query.getField())) {
			int i = 0;
			for (Column column : table.getColumn())
				buf.append(i++ == 0 ? " " : ", ").append(column.getName());
		} else {
			int i = 0;
			for (String fieldName : query.getField())
				buf.append(i++ == 0 ? " " : ", ").append(table.toColumnName(fieldName));
		}

		// From
		buf.append(" from ").append(table.getName());

		// Where
		if (query == null) {
			query = new Query();
			if (condition instanceof Map) {
				Map<String, Object> map = (Map<String, Object>) condition;
				for (String lo : map.keySet())
					query.addFilter(lo, map.get(lo));
			} else if (condition instanceof Filters) {
				ValueUtils.populate(condition, query);
			} else if (condition instanceof List) {
				query.setFilter((List<Filter>) condition);
			} else if (condition instanceof Filter) {
				query.addFilter((Filter) condition);
			}
		}
		{
			Map<String, Object> paramMap = new HashMap<String, Object>();
			int i = 0;
			if (!ValueUtils.isEmpty(query.getFilter())) {
				for (Filter filter : query.getFilter()) {
					String lo = filter.getLeftOperand();
					// buf.append(i++ == 0 ? " where " : " and ")
					// .append(table.toColumnName(lo)).append(" = :").append(lo);
					// paramMap.put(lo, );
				}
			}
		}

		// Order by
		if (!ValueUtils.isEmpty(query.getOrder())) {
			buf.append(" order by");
			int i = 0;
			for (Order order : query.getOrder()) {
				buf.append(i++ == 0 ? " " : ", ").append(table.toColumnName(order.getField())).append(order.isAscending() ? " asc" : " desc");
			}
		}

		return null;
	}

	@Override
	public <T> List<T> selectListForUpdate(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> selectList(String query, Map<String, Object> paramMap, T requiredType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> selectListForUpdate(String query, Map<String, Object> paramMap, T requiredType) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void deleteList(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub

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

				// Domain and Name
				org.dbist.annotation.Table tableAnn = clazz.getAnnotation(org.dbist.annotation.Table.class);
				if (tableAnn != null) {
					if (!ValueUtils.isEmpty(tableAnn.domain()))
						table.setDomain(tableAnn.domain().toLowerCase());
					if (!ValueUtils.isEmpty(tableAnn.name()))
						table.setName(tableAnn.name().toLowerCase());
				}
				checkAndPopulateDomainAndName(table, clazz);

				// PkColumns

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
		table.setPkColumnName(jdbcTemplate.queryForList(query, String.class, table.getName()));

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
		column.setPrimaryKey(table.getPkColumnName().contains(tabColumn.getName()));
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
