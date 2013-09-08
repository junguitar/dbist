/**
 * Copyright 2011-2013 the original author or authors.
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
import org.dbist.annotation.Relation;
import org.dbist.dml.AbstractDml;
import org.dbist.dml.Dml;
import org.dbist.dml.Filter;
import org.dbist.dml.Filters;
import org.dbist.dml.Lock;
import org.dbist.dml.Order;
import org.dbist.dml.Page;
import org.dbist.dml.Query;
import org.dbist.dml.jdbc.QueryMapper;
import org.dbist.dml.jdbc.QueryMapperDb2;
import org.dbist.dml.jdbc.QueryMapperMysql;
import org.dbist.dml.jdbc.QueryMapperOracle;
import org.dbist.dml.jdbc.QueryMapperPostgresql;
import org.dbist.dml.jdbc.QueryMapperSqlserver;
import org.dbist.exception.DataNotFoundException;
import org.dbist.exception.DbistRuntimeException;
import org.dbist.metadata.Column;
import org.dbist.metadata.Sequence;
import org.dbist.metadata.Table;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcOperations;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcOperations;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

/**
 * @author Steve M. Jung
 * @since 2011. 6. 2. (version 0.0.1)
 */
public class DmlJdbc extends AbstractDml implements Dml {
	private static final Logger logger = LoggerFactory.getLogger(DmlJdbc.class);

	public static final String COLUMNALIASRULE_DEFAULT = "default";
	public static final String COLUMNALIASRULE_UPPERCASE = "uppercase";
	public static final String COLUMNALIASRULE_LOWERCASE = "lowercase";
	public static final String COLUMNALIASRULE_CAMELCASE = "camelcase";
	private static final List<String> COLUMNALIASRULE_LIST = ValueUtils.toList(COLUMNALIASRULE_DEFAULT, COLUMNALIASRULE_UPPERCASE,
			COLUMNALIASRULE_LOWERCASE, COLUMNALIASRULE_CAMELCASE);

	private boolean isSupported() {
		return queryMapper != null;
	}
	private boolean isSupportedPaginationQuery() {
		return queryMapper.isSupportedPaginationQuery();
	}

	private String domain;
	private List<String> domainList = new ArrayList<String>(2);
	private String columnAliasRuleForMapKey;
	private int columnAliasRule;
	private DataSource dataSource;
	private JdbcOperations jdbcOperations;
	private NamedParameterJdbcOperations namedParameterJdbcOperations;
	private int maxSqlByPathCacheSize = 1000;
	private int defaultLockTimeout = -1;
	private QueryMapper queryMapper;

	@SuppressWarnings("unchecked")
	@Override
	public void afterPropertiesSet() throws Exception {
		boolean debug = logger.isDebugEnabled();
		super.afterPropertiesSet();
		ValueUtils.assertNotEmpty("domain", getDomain());
		ValueUtils.assertNotEmpty("dataSource", getDataSource());
		ValueUtils.assertNotEmpty("jdbcOperations", getJdbcOperations());
		ValueUtils.assertNotEmpty("namedParameterJdbcOperations", getNamedParameterJdbcOperations());

		if (ValueUtils.isEmpty(getDbType())) {
			DatabaseMetaData metadata = dataSource.getConnection().getMetaData();
			setDbType(metadata.getDatabaseProductName().toLowerCase());
			if (getDbType().startsWith("microsoft sql server")) {
				setDbType("sqlserver");
			} else if (getDbType().startsWith("db2/")) {
				setDbType("db2");
			}
		}
		if (getQueryMapper() == null) {
			if (getDbType().equals("mysql"))
				setQueryMapper(new QueryMapperMysql());
			else if (getDbType().equals("postgresql"))
				setQueryMapper(new QueryMapperPostgresql());
			else if (getDbType().equals("oracle"))
				setQueryMapper(new QueryMapperOracle());
			else if (getDbType().equals("db2"))
				setQueryMapper(new QueryMapperDb2());
			else if (getDbType().equals("sqlserver"))
				setQueryMapper(new QueryMapperSqlserver());
		}
		if (!isSupported())
			throw new IllegalArgumentException("Unsupported dbType: " + getDbType());

		if ("sqlserver".equals(getDbType())) {
			List<String> domainList = new ArrayList<String>(this.domainList.size());
			for (String domain : this.domainList) {
				if (domain.endsWith("."))
					continue;
				domainList.add(domain + ".");
			}
			this.domainList = domainList;
		}

		if (ValueUtils.isEmpty(columnAliasRuleForMapKey))
			columnAliasRuleForMapKey = COLUMNALIASRULE_DEFAULT;
		else if (!COLUMNALIASRULE_LIST.contains(columnAliasRuleForMapKey))
			throw new IllegalArgumentException("Unsupported columnAliasRule: ");
		columnAliasRule = COLUMNALIASRULE_LIST.indexOf(columnAliasRuleForMapKey);

		if (maxSqlByPathCacheSize > 0)
			sqlByPathCache = Collections.synchronizedMap(new LRUMap(maxSqlByPathCacheSize));
		if (debug)
			logger.debug("dml loaded (dbType: " + getDbType() + ")");
	}
	public void clear() {
		logger.info("Clearing DmlJdbc bean: " + getBeanName() + "...");
		classFieldCache.clear();
		classByTableNameCache.clear();
		tableByClassCache.clear();
	}

	public void insert(Object data) throws Exception {
		_insert(data);
	}

	public void insertBatch(List<?> list) throws Exception {
		_insertBatch(list);
	}

	public void insert(Object data, String... fieldNames) throws Exception {
		_insert(data, fieldNames);
	}

	public void insertBatch(List<?> list, String... fieldNames) throws Exception {
		_insertBatch(list, fieldNames);
	}

	private void _insert(Object data, String... fieldNames) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		doBeforeInsert(data, table);
		String sql = table.getInsertSql(fieldNames);
		Map<String, Object> paramMap = toParamMap(table, data, fieldNames);
		updateBySql(sql, paramMap);
	}

	private <T> void _insertBatch(List<T> list, String... fieldNames) throws Exception {
		if (ValueUtils.isEmpty(list))
			return;
		Table table = getTable(list.get(0));
		doBeforeInsertBatch(list, table);
		String sql = table.getInsertSql(fieldNames);
		fieldNames = toFieldNamesForUpdate(table, fieldNames);
		List<Map<String, ?>> paramMapList = toParamMapList(table, list, fieldNames);
		updateBatchBySql(sql, paramMapList);
	}

	public void update(Object data) throws Exception {
		_update(data);
	}

	public void updateBatch(List<?> list) throws Exception {
		_updateBatch(list);
	}

	public void update(Object data, String... fieldNames) throws Exception {
		_update(data, fieldNames);
	}

	public void updateBatch(List<?> list, String... fieldNames) throws Exception {
		_updateBatch(list, fieldNames);
	}

	private <T> void _update(T data, String... fieldNames) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		if (ValueUtils.isEmpty(table.getPkFieldNames()))
			throw new DbistRuntimeException("More than 1 primary key field is required in the class " + table.getClazz().getName() + " to update");
		String sql = table.getUpdateSql(fieldNames);
		fieldNames = toFieldNamesForUpdate(table, fieldNames);
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
		if (ValueUtils.isEmpty(table.getPkFieldNames()))
			throw new DbistRuntimeException("More than 1 primary key field is required in the class " + table.getClazz().getName()
					+ " to update batch");
		String sql = table.getUpdateSql(fieldNames);
		fieldNames = toFieldNamesForUpdate(table, fieldNames);
		List<Map<String, ?>> paramMapList = toParamMapList(table, list, fieldNames);
		updateBatchBySql(sql, paramMapList);
	}
	private static String[] toFieldNamesForUpdate(Table table, String... fieldNames) {
		if (ValueUtils.isEmpty(fieldNames))
			return fieldNames;
		List<String> fieldNameList = ValueUtils.toList(fieldNames);
		for (String fieldName : table.getPkFieldNames()) {
			if (fieldNameList.contains(fieldName))
				continue;
			fieldNameList.add(fieldName);
		}
		fieldNames = fieldNameList.toArray(new String[fieldNameList.size()]);
		return fieldNames;
	}

	public void delete(Object data) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Table table = getTable(data);
		if (ValueUtils.isEmpty(table.getPkFieldNames()))
			throw new DbistRuntimeException("More than 1 primary key field is required in the class " + table.getClazz().getName() + " to delete");
		String sql = table.getDeleteSql();
		Map<String, Object> paramMap = toParamMap(table, data, table.getPkFieldNames());
		if (updateBySql(sql, paramMap) != 1)
			throw new DataNotFoundException(toNotFoundErrorMessage(table, data, paramMap));
	}

	public void deleteBatch(List<?> list) throws Exception {
		if (ValueUtils.isEmpty(list))
			return;
		Table table = getTable(list.get(0));
		if (ValueUtils.isEmpty(table.getPkFieldNames()))
			throw new DbistRuntimeException("More than 1 primary key field is required in the class " + table.getClazz().getName()
					+ " to delete batch");
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

	private void appendFromWhere(Table table, Query query, StringBuffer buf, Map<String, Object> paramMap, Map<String, Column> relColMap) {
		if (relColMap != null && table.containsLinkedTable() && (!ValueUtils.isEmpty(query.getSelect()) || !ValueUtils.isEmpty(query.getUnselect())))
			populateRelColMap(table, query, relColMap);

		// From
		buf.append(" from ").append(table.getDomain()).append(".").append(table.getName());
		if (query.getLock() != null && queryMapper != null) {
			String str = queryMapper.toWithLock(query.getLock());
			if (!ValueUtils.isEmpty(str))
				buf.append(" ").append(str);
		}
		if (!ValueUtils.isEmpty(relColMap)) {
			for (Column col : relColMap.values()) {
				Table subTab = col.getTable();
				buf.append(" left outer join ").append(subTab.getDomain()).append(".").append(subTab.getName());
				if (!subTab.getName().equals(col.getName()))
					buf.append(" " + col.getName());
				buf.append(" on ");
				Relation relation = col.getRelation();
				int i = 0;
				for (String key : subTab.getPkColumnNameList())
					buf.append(table.getName()).append(".").append(toColumnName(table, relation.field()[i++])).append(" = ").append(col.getName())
							.append(".").append(key);
			}
		}

		// Where
		appendWhere(buf, table, query, 0, paramMap);
	}
	private void populateRelColMap(Table table, Filters filters, Map<String, Column> relColMap) {
		if (relColMap == null)
			return;
		if (!ValueUtils.isEmpty(filters.getFilter())) {
			for (Filter filter : filters.getFilter()) {
				if (!filter.getLeftOperand().contains("."))
					continue;
				String lo = filter.getLeftOperand();
				String fieldName = lo.substring(0, lo.indexOf('.'));
				Column column = toColumn(table, fieldName);
				if (column.getRelation() == null)
					throw new DbistRuntimeException("filter: " + lo + " is not a joined condition.");
				if (relColMap.containsKey(column.getName()))
					continue;
				relColMap.put(column.getName(), column);
			}
		}

		if (!ValueUtils.isEmpty(filters.getFilters())) {
			for (Filters fs : filters.getFilters())
				populateRelColMap(table, fs, relColMap);
		}
	}
	public int selectSize(Class<?> clazz, Object condition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("condition", condition);

		final Table table = getTable(clazz);
		Query query = toQuery(table, condition);

		StringBuffer buf = new StringBuffer("select count(*)");
		@SuppressWarnings("unchecked")
		Map<String, Object> paramMap = new ListOrderedMap();
		Lock lock = query.getLock();
		try {
			query.setLock(null);
			if (ValueUtils.isEmpty(query.getGroup())) {
				appendFromWhere(table, query, buf, paramMap, table.containsLinkedTable() ? new HashMap<String, Column>() : null);
			} else {
				buf.append(" from (");
				appendSelectSql(buf, paramMap, table, query, true, true);
				buf.append(") grptbl_");
			}
		} finally {
			query.setLock(lock);
		}

		return this.namedParameterJdbcOperations.queryForInt(buf.toString(), paramMap);
	}

	public <T> List<T> selectList(final Class<T> clazz, Object condition) throws Exception {
		return _selectList(clazz, condition, false);
	}

	public <T> List<T> selectListWithLock(Class<T> clazz, Object condition) throws Exception {
		return _selectList(clazz, condition, true);
	}

	public <T> List<T> _selectList(final Class<T> clazz, Object condition, boolean lock) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("condition", condition);

		final Table table = getTable(clazz);
		final Query query = toQuery(table, condition);
		Lock lockObj = query.getLock();
		if ((lock || lockObj != null) && query.getPageIndex() >= 0 && query.getPageSize() > 0)
			throw new DbistRuntimeException("Cannot select with lock and pagination at the same time. (class: " + clazz + ")");

		StringBuffer buf = new StringBuffer();
		@SuppressWarnings("unchecked")
		Map<String, Object> paramMap = new ListOrderedMap();

		try {
			if (lock && lockObj == null)
				query.setLock(new Lock());
			boolean groupBy = !ValueUtils.isEmpty(query.getGroup());
			if (groupBy && query.getLock() != null)
				throw new DbistRuntimeException("Grouping query cannot be executed with lock.");

			appendSelectSql(buf, paramMap, table, query, groupBy, false);
		} finally {
			query.setLock(lockObj);
		}

		String sql = applyPagination(buf.toString(), paramMap, query.getPageIndex(), query.getPageSize(), query.getFirstResultIndex(),
				query.getMaxResultSize());

		List<T> list = query(sql, paramMap, clazz, table, query.getPageIndex(), query.getPageSize(), query.getFirstResultIndex(),
				query.getMaxResultSize());
		return list;
	}
	private void appendSelectSql(StringBuffer buf, Map<String, Object> paramMap, Table table, Query query, boolean groupBy, boolean ignoreOrderBy) {
		boolean joined = table.containsLinkedTable();
		@SuppressWarnings("unchecked")
		Map<String, Column> relColMap = joined ? new ListOrderedMap() : null;

		// Select
		buf.append("select");
		// Grouping fields
		if (groupBy) {
			if (!ValueUtils.isEmpty(query.getSelect())) {
				List<String> group = query.getGroup();
				for (String fieldName : query.getSelect()) {
					if (group.contains(fieldName))
						continue;
					throw new DbistRuntimeException("Grouping query cannot be executed with some other fields: " + table.getClazz().getName() + "."
							+ fieldName);
				}
			}
			int i = 0;
			for (String group : query.getGroup()) {
				buf.append(i++ == 0 ? " " : ", ");
				if (joined)
					buf.append(table.getName()).append(".");
				buf.append(toColumnName(table, group));
			}
		}
		// All fields
		else if (ValueUtils.isEmpty(query.getSelect())) {
			int i = 0;
			if (ValueUtils.isEmpty(query.getUnselect())) {
				for (Column column : table.getColumnList())
					i = appendColumn(buf, table, column, relColMap, i);
			}
			// Except some fields
			else {
				List<String> unselects = new ArrayList<String>(query.getUnselect().size());
				for (String unselect : query.getUnselect())
					unselects.add(toColumnName(table, unselect));
				for (Column column : table.getColumnList()) {
					if (unselects.contains(column.getName()))
						continue;
					i = appendColumn(buf, table, column, relColMap, i);
				}
			}
		}
		// Some fields
		else {
			int i = 0;
			for (String fieldName : query.getSelect()) {
				Column column = toColumn(table, fieldName);
				i = appendColumn(buf, table, column, relColMap, i);
			}
		}

		appendFromWhere(table, query, buf, paramMap, relColMap);

		// Group by
		if (groupBy) {
			buf.append(" group by");
			int i = 0;
			for (String group : query.getGroup()) {
				buf.append(i++ == 0 ? " " : ", ");
				if (joined)
					buf.append(table.getName()).append(".");
				buf.append(toColumnName(table, group));
			}
		}

		// Order by
		if (!ignoreOrderBy && !ValueUtils.isEmpty(query.getOrder())) {
			buf.append(" order by");
			int i = 0;
			for (Order order : query.getOrder()) {
				for (String fieldName : StringUtils.tokenizeToStringArray(order.getField(), ",")) {
					buf.append(i++ == 0 ? " " : ", ");
					if (joined)
						buf.append(table.getName()).append(".");
					buf.append(toColumnName(table, fieldName)).append(order.isAscending() ? " asc" : " desc");
				}
			}
		}

		appendLock(buf, query.getLock());
	}
	private int appendColumn(StringBuffer buf, Table table, Column column, Map<String, Column> relColMap, int i) {
		if (column.getRelation() == null) {
			buf.append(i++ == 0 ? " " : ", ");
			if (table.containsLinkedTable())
				buf.append(table.getName()).append(".");
			buf.append(column.getName());
			return i;
		} else if (ValueUtils.isEmpty(column.getColumnList())) {
			return i;
		}
		if (relColMap != null && !relColMap.containsKey(column.getName()))
			relColMap.put(column.getName(), column);
		for (Column subCol : column.getColumnList())
			buf.append(i++ == 0 ? " " : ", ").append(column.getName()).append(".").append(subCol.getName()).append(" ").append(column.getName())
					.append("__").append(subCol.getName());
		return i;
	}

	private void appendLock(StringBuffer buf, Lock lock) {
		if (lock == null)
			return;
		//		if (DBTYPE_DB2.equals(getDbType())) {
		//			buf.append(" for read only with rs");
		//			return;
		//		}
		if (lock.getTimeout() == null) {
			lock = new Lock();
			lock.setTimeout(defaultLockTimeout);
		}
		String str = queryMapper.toForUpdate(lock);
		if (ValueUtils.isEmpty(str))
			return;
		buf.append(" ").append(str);
	}

	public String applyPagination(String sql, Map<String, ?> paramMap, int pageIndex, int pageSize, int firstResultIndex, int maxResultSize) {
		return queryMapper.applyPagination(sql, paramMap, pageIndex, pageSize, firstResultIndex, maxResultSize);
	}

	private <T> List<T> query(String sql, Map<String, ?> paramMap, final Class<T> requiredType, final Table table, int pageIndex, int pageSize,
			int firstResultIndex, int maxResultSize) throws Exception {
		boolean pagination = pageIndex >= 0 && pageSize > 0;
		boolean fragment = firstResultIndex > 0 || maxResultSize > 0;

		List<T> list = null;
		if (isSupportedPaginationQuery() || (!pagination && !fragment)) {
			list = this.namedParameterJdbcOperations.query(sql, paramMap, new RowMapper<T>() {
				public T mapRow(ResultSet rs, int rowNum) throws SQLException {
					return newInstance(rs, requiredType, table);
				}
			});
		} else {
			if (!pagination) {
				pageIndex = 0;
				pageSize = 0;
			}
			if (firstResultIndex < 0)
				firstResultIndex = 0;
			if (maxResultSize < 0)
				maxResultSize = 0;
			int pageFromIndex = pagination ? pageIndex * pageSize : 0;
			int offset = pageFromIndex + firstResultIndex;
			long limit = 0;
			if (pageSize > 0) {
				limit = pageSize - firstResultIndex;
				if (maxResultSize > 0)
					limit = Math.min(limit, maxResultSize);
			} else if (maxResultSize > 0) {
				limit = maxResultSize;
			} else if (limit == 0) {
				limit = Long.MAX_VALUE;
			}
			final int _offset = offset;
			final long _limit = limit;
			list = this.namedParameterJdbcOperations.query(sql, paramMap, new ResultSetExtractor<List<T>>() {
				public List<T> extractData(ResultSet rs) throws SQLException, DataAccessException {
					List<T> list = new ArrayList<T>();
					for (int i = 0; i < _offset; i++) {
						if (rs.next())
							continue;
						return list;
					}
					int i = 0;
					while (rs.next()) {
						if (i++ == _limit)
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
	private static Map<Class<?>, Map<String, Field>> classSubFieldCache = new ConcurrentHashMap<Class<?>, Map<String, Field>>();

	@SuppressWarnings("unchecked")
	private <T> T newInstance(ResultSet rs, Class<T> clazz, Table table) throws SQLException {
		if (ValueUtils.isPrimitive(clazz))
			return (T) toRequiredType(rs, 1, clazz);

		ResultSetMetaData metadata = rs.getMetaData();
		Map<String, Field> fieldCache;
		Map<String, Field> subFieldCache;
		if (classFieldCache.containsKey(clazz)) {
			fieldCache = classFieldCache.get(clazz);
			subFieldCache = classSubFieldCache.get(clazz);
		} else {
			fieldCache = new ConcurrentHashMap<String, Field>();
			classFieldCache.put(clazz, fieldCache);
			subFieldCache = null;
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
				String name = metadata.getColumnLabel(i);
				switch (columnAliasRule) {
				case 0: {
					break;
				}
				case 1: {
					name = name.toUpperCase();
					break;
				}
				case 2: {
					name = name.toLowerCase();
					break;
				}
				case 3: {
					name = ValueUtils.toCamelCase(name, '_');
					break;
				}
				}
				map.put(name, toRequiredType(rs, i, null));
			}
		} else {
			for (int i = 0; i < metadata.getColumnCount();) {
				i++;
				String name = metadata.getColumnLabel(i);
				Field field = null;
				Field subField = null;
				if (fieldCache.containsKey(name)) {
					field = fieldCache.get(name);
					subField = subFieldCache == null ? null : subFieldCache.get(name);
				} else {
					field = getField(clazz, table, name);

					if (field == null && name.contains("__")) {
						int index = name.indexOf("__");
						String fieldName = name.substring(0, index);
						field = getField(clazz, table, fieldName);
						if (field != null) {
							String subFieldName = name.substring(index + 2);
							Class<?> subClass = field.getType();
							Table subTable = getTable(subClass);
							subField = getField(subClass, subTable, subFieldName);
							if (subField == null) {
								field = null;
							} else {
								if (subFieldCache == null) {
									subFieldCache = new ConcurrentHashMap<String, Field>();
									classSubFieldCache.put(clazz, subFieldCache);
								}
								subFieldCache.put(name, subField);
							}
						}
					}

					fieldCache.put(name, field == null ? ReflectionUtils.NULL_FIELD : field);
				}
				if (field == null || ReflectionUtils.NULL_FIELD.equals(field))
					continue;
				setFieldValue(rs, i, data, field, subField);
			}
		}
		return data;
	}
	private static Field getField(Class<?> clazz, Table table, String name) {
		Field field = null;

		if (table != null) {
			field = table.getFieldByColumnName(name);
			if (field != null)
				return field;
			field = table.getField(name);
			if (field != null)
				return field;
		}

		field = ReflectionUtils.getField(clazz, ValueUtils.toCamelCase(name, '_'));
		if (field != null)
			return field;

		for (Field f : ReflectionUtils.getFieldList(clazz, false)) {
			if (!f.getName().equalsIgnoreCase(name))
				continue;
			field = f;
			break;
		}

		return field;
	}
	private static void setFieldValue(ResultSet rs, int index, Object data, Field field, Field subField) throws SQLException {
		if (subField == null) {
			try {
				field.set(data, toRequiredType(rs, index, field.getType()));
			} catch (IllegalArgumentException e) {
				throw new DbistRuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new DbistRuntimeException(e);
			}
			return;
		}

		Object subData;
		try {
			subData = field.get(data);
		} catch (IllegalArgumentException e) {
			throw new DbistRuntimeException(e);
		} catch (IllegalAccessException e) {
			throw new DbistRuntimeException(e);
		}

		if (subData == null) {
			try {
				subData = field.getType().newInstance();
			} catch (InstantiationException e) {
				throw new DbistRuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new DbistRuntimeException(e);
			}

			try {
				field.set(data, subData);
			} catch (IllegalArgumentException e) {
				throw new DbistRuntimeException(e);
			} catch (IllegalAccessException e) {
				throw new DbistRuntimeException(e);
			}
		}

		try {
			subField.set(subData, toRequiredType(rs, index, subField.getType()));
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
				if (str == null || str.length() == 0)
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

	@SuppressWarnings("unchecked")
	private static final List<?> CASECHECK_TYPELIST = ValueUtils.toList(String.class, Character.class, char.class);
	private int appendWhere(StringBuffer buf, Table table, Filters filters, int i, Map<String, Object> paramMap) {
		String logicalOperator = " " + ValueUtils.toString(filters.getOperator(), "and").trim().toLowerCase() + " ";

		int j = 0;
		if (!ValueUtils.isEmpty(filters.getFilter())) {
			String defaultAlias = table.containsLinkedTable() ? table.getName() : null;
			for (Filter filter : filters.getFilter()) {
				String operator = ValueUtils.toString(filter.getOperator(), "=").trim().toLowerCase();
				if ("!=".equals(operator))
					operator = "<>";
				String lo = filter.getLeftOperand();
				String alias;
				Column column;
				if (lo.contains(".")) {
					int index = lo.indexOf('.');
					String fieldName = lo.substring(0, index);
					column = toColumn(table, fieldName);
					if (column.getRelation() == null)
						throw new DbistRuntimeException("filter: " + lo + " is not a joined condition.");
					alias = column.getName();
					String subFieldName = lo.substring(index + 1);
					column = toColumn(column.getTable(), subFieldName);
				} else {
					alias = defaultAlias;
					column = toColumn(table, lo);
				}
				buf.append(i++ == 0 ? " where " : j == 0 ? "" : logicalOperator);
				j++;

				String columnName = alias == null ? column.getName() : alias + "." + column.getName();
				List<?> rightOperand = filter.getRightOperand();

				// case: 'is null' or 'is not null'
				if (ValueUtils.isEmpty(rightOperand)) {
					appendNullCondition(buf, columnName, operator);
					continue;
				}

				Class<?> type = column.getField().getType();

				// check and process case sensitive
				List<Object> newRightOperand = new ArrayList<Object>(rightOperand.size());
				if (!filter.isCaseSensitive() && CASECHECK_TYPELIST.contains(type)) {
					columnName = getFunctionLowerCase() + "(" + columnName + ")";
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
						appendNullCondition(buf, columnName, operator);
						continue;
					}

					// case x = 'l' or x != 'l'
					String key = lo + i;
					paramMap.put(key, value);
					buf.append(columnName).append(" ").append(operator).append(" :").append(key);
					if ("like".equals(operator) && !ValueUtils.isEmpty(filter.getEscape()))
						buf.append(applyEscapement(filter.getEscape()));
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
							appendNullCondition(buf, columnName, operator);
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
			buf.append(i++ == 0 ? " where " : j++ == 0 ? " " : logicalOperator);
			int k = 0;
			for (Filters subFilters : filters.getFilters()) {
				buf.append(k++ == 0 ? "" : logicalOperator).append("(");
				i = appendWhere(buf, table, subFilters, i, paramMap);
				buf.append(")");
			}
		}

		return i;
	}

	private static Column toColumn(Table table, String name) {
		return table.getColumn(toColumnName(table, name));
	}
	private static String toColumnName(Table table, String name) {
		String columnName = table.toColumnName(name);
		if (columnName != null)
			return columnName;
		columnName = name.toLowerCase();
		if (table.getColumn(columnName) != null)
			return columnName;

		StringBuffer buf = new StringBuffer("Undeclared column/field: ").append(name);
		buf.append(" of table").append(table.getClazz() == null ? "" : "(class)").append(": ");
		buf.append(table.getDomain()).append(".").append(table.getName());
		if (table.getClazz() != null)
			buf.append("(").append(table.getClazz().getName()).append(")");
		throw new DbistRuntimeException(buf.toString());
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

	private void appendNullCondition(StringBuffer buf, String columnName, String operator) {
		buf.append(columnName);
		if ("=".equals(operator) || "in".equals(operator))
			operator = "is";
		else if ("<>".equals(operator) || "not in".equals(operator))
			operator = "is not";
		buf.append(" ").append(operator).append(" null");
	}

	public <T> List<T> selectListByQl(String ql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize, int firstResultIndex,
			int maxResultSize) throws Exception {
		ValueUtils.assertNotEmpty("ql", ql);
		ValueUtils.assertNotEmpty("requiredType", requiredType);
		paramMap = paramMap == null ? new HashMap<String, Object>() : paramMap;
		ql = ql.trim();
		if (getPreprocessor() != null)
			ql = getPreprocessor().process(ql, paramMap);
		ql = applyPagination(ql, paramMap, pageIndex, pageSize, firstResultIndex, maxResultSize);
		adjustParamMap(paramMap);
		return query(ql, paramMap, requiredType, null, pageIndex, pageSize, firstResultIndex, maxResultSize);
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

	public int selectSizeByQl(String ql, Map<String, ?> paramMap) throws Exception {
		paramMap = paramMap == null ? new HashMap<String, Object>() : paramMap;
		int forUpdateIndex = ql.toLowerCase().lastIndexOf("for update");
		if (forUpdateIndex > -1)
			ql = ql.substring(0, forUpdateIndex - 1);
		ql = "select count(*) from (" + ql + ") cnttbl_";
		return selectByQl(ql, paramMap, Integer.class);
	}

	public <T> List<T> selectListByQlPath(String qlPath, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize,
			int firstResultIndex, int maxResultSize) throws Exception {
		return selectListByQl(getSqlByPath(qlPath), paramMap, requiredType, pageIndex, pageSize, firstResultIndex, maxResultSize);
	}

	public <T> Page<T> selectPageByQlPath(String qlPath, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize,
			int firstResultIndex, int maxResultSize) throws Exception {
		return selectPageByQl(getSqlByPath(qlPath), paramMap, requiredType, pageIndex, pageSize, firstResultIndex, maxResultSize);
	}

	public int selectSizeByQlPath(String qlPath, Map<String, ?> paramMap) throws Exception {
		return selectSizeByQl(getSqlByPath(qlPath), paramMap);
	}

	private Map<String, String> sqlByPathCache;
	private String getSqlByPath(final String path) throws IOException {
		ValueUtils.assertNotNull("path", path);
		if (sqlByPathCache == null)
			return _getSqlByPath(path);
		if (sqlByPathCache.containsKey(path))
			return sqlByPathCache.get(path);
		return SyncCtrlUtils.wrap("DmlJdbc.sqlByPathCache." + path, sqlByPathCache, path, new Closure<String, IOException>() {
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

	public int deleteList(Class<?> clazz, Object condition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("condition", condition);

		final Table table = getTable(clazz);
		Query query = toQuery(table, condition);

		StringBuffer buf = new StringBuffer("delete");
		@SuppressWarnings("unchecked")
		Map<String, Object> paramMap = new ListOrderedMap();
		Lock lock = query.getLock();
		try {
			query.setLock(null);
			appendFromWhere(table, query, buf, paramMap, table.containsLinkedTable() ? new HashMap<String, Column>() : null);
		} finally {
			query.setLock(lock);

		}

		return this.namedParameterJdbcOperations.update(buf.toString(), paramMap);
	}

	public int executeByQl(String ql, Map<String, ?> paramMap) throws Exception {
		ValueUtils.assertNotEmpty("ql", ql);
		paramMap = paramMap == null ? new HashMap<String, Object>() : paramMap;
		ql = ql.trim();
		if (getPreprocessor() != null)
			ql = getPreprocessor().process(ql, paramMap);
		adjustParamMap(paramMap);
		return this.namedParameterJdbcOperations.update(ql, paramMap);
	}

	public int executeByQlPath(String qlPath, Map<String, ?> paramMap) throws Exception {
		return executeByQl(getSqlByPath(qlPath), paramMap);
	}

	@Override
	public String getDbType() {
		return queryMapper == null ? super.getDbType() : queryMapper.getDbType();
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
	public String getColumnAliasRuleForMapKey() {
		return columnAliasRuleForMapKey;
	}
	public void setColumnAliasRuleForMapKey(String columnAliasRuleForMapKey) {
		this.columnAliasRuleForMapKey = columnAliasRuleForMapKey;
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
	public int getDefaultLockTimeout() {
		return defaultLockTimeout;
	}
	public void setDefaultLockTimeout(int defaultLockTimeout) {
		this.defaultLockTimeout = defaultLockTimeout;
	}
	public QueryMapper getQueryMapper() {
		return queryMapper;
	}
	public void setQueryMapper(QueryMapper queryMapper) {
		this.queryMapper = queryMapper;
	}

	private Map<String, Class<?>> classByTableNameCache = new ConcurrentHashMap<String, Class<?>>();

	public Class<?> getClass(String tableName) {
		final String _name = tableName.toLowerCase();

		if (classByTableNameCache.containsKey(_name))
			return classByTableNameCache.get(_name);

		return SyncCtrlUtils.wrap("DmlJdbc.classByTableName." + _name, classByTableNameCache, _name, new Closure<Class<?>, RuntimeException>() {
			public Class<?> execute() {
				String className = "org.dbist.virtual." + ValueUtils.toCamelCase(_name, '_', true);
				try {
					return ClassUtils.forName(className, null);
				} catch (ClassNotFoundException e) {
					try {
						return ClassPool.getDefault().getCtClass(className).toClass();
					} catch (CannotCompileException e1) {
					} catch (NotFoundException e1) {
					}
				} catch (LinkageError e) {
				}

				Table table = new Table();

				checkAndPopulateDomainAndName(table, _name);

				CtClass cc = ClassPool.getDefault().makeClass(className);
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

	public Table getTable(Object obj) {
		final Class<?> clazz = obj instanceof Class ? (Class<?>) obj : obj.getClass();

		final boolean debug = logger.isDebugEnabled();

		if (tableByClassCache.containsKey(clazz)) {
			if (debug)
				logger.debug("get table metadata from map cache by class: " + clazz.getName());
			return tableByClassCache.get(clazz);
		}

		return SyncCtrlUtils.wrap("DmlJdbc.tableByClass." + clazz.getName(), tableByClassCache, clazz, new Closure<Table, RuntimeException>() {
			public Table execute() {
				if (debug)
					logger.debug("make table metadata by class: " + clazz.getName());
				Table table = new Table();
				table.setClazz(clazz);
				table.setDbType(getDbType());

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

				table.setQueryMapper(queryMapper);

				return table;
			}
		});
	}

	private static final String MSG_QUERYNOTFOUND = "Couldn't find ${queryName} query of dbType: ${dbType}. this type maybe unsupported yet.";

	//	private static final
	private <T> Table checkAndPopulateDomainAndName(Table table, String... tableNameCandidates) {
		// Check table existence and populate
		String sql = getQueryCountTable();
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
			if (populated)
				break;
		}

		if (!populated) {
			String errMsg = "Couldn't find table[${table}] from this(these) domain(s)[${domain}]";
			throw new IllegalArgumentException(ValueUtils.populate(errMsg,
					ValueUtils.toMap("domain:" + mapOr(domainNameList), "table:" + mapOr(tableNameCandidates))));
		}

		// populate PK name
		sql = getQueryPkColumnNames();
		if (sql == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: primary key", "dbType:" + getDbType())));
		sql = StringUtils.replace(sql, "${domain}", table.getDomain());
		table.setPkColumnNameList(jdbcOperations.queryForList(sql, String.class, table.getName()));

		return table;
	}

	private static final RowMapper<TableColumn> TABLECOLUMN_ROWMAPPER = new TableColumnRowMapper();
	private List<TableColumn> getTableColumnList(Table table) {
		String sql = getQueryColumnNames();
		if (sql == null)
			throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
					ValueUtils.toMap("queryName: table columns", "dbType:" + getDbType())));
		sql = StringUtils.replace(sql, "${domain}", table.getDomain());

		String tableName = table.getName();

		return jdbcOperations.query(sql, new Object[] { tableName }, TABLECOLUMN_ROWMAPPER);
	}

	private static final String MSG_COLUMNNOTFOUND = "Couldn't find column[${column}] of table[${table}].";
	private void addColumn(Table table, Field field) {
		Ignore ignoreAnn = field.getAnnotation(Ignore.class);
		if (ignoreAnn != null)
			return;

		Column column = table.addColumn(new Column());
		column.setField(field);
		column.setGetter(ReflectionUtils.getGetter(table.getClazz(), field.getName(), field.getType()));
		column.setSetter(ReflectionUtils.getSetter(table.getClazz(), field.getName(), field.getType()));

		Relation relAnn = field.getAnnotation(Relation.class);
		if (relAnn != null) {
			if (relAnn.field().length == 0)
				throw new DbistRuntimeException("@Relation of " + table.getClazz().getName() + "." + field.getName()
						+ " requires linked field value.");
			column.setRelation(relAnn);

			Class<?> linkedClass = field.getType();
			Table linkedTable = getTable(linkedClass);

			if (relAnn.field().length != linkedTable.getPkColumnNameList().size())
				throw new DbistRuntimeException("@Relation.field.length of " + table.getClazz().getName() + "." + field.getName()
						+ " must same with the primary key size of " + table.getName());

			column.setTable(linkedTable);
			table.setContainsLinkedTable(true);

			column.setName(ValueUtils.toDelimited(field.getName(), '_'));
			for (Column linkedColumn : linkedTable.getColumnList()) {
				if (!ValueUtils.isEmpty(linkedColumn.getColumnList()))
					continue;
				column.addColumn(linkedColumn);
			}
			return;
		}

		String tableName = table.getName();

		// Column
		{
			String sql = getQueryColumnName();
			if (sql == null)
				throw new IllegalArgumentException(ValueUtils.populate(MSG_QUERYNOTFOUND,
						ValueUtils.toMap("queryName: table column", "dbType:" + getDbType())));
			sql = StringUtils.replace(sql, "${domain}", table.getDomain());
			org.dbist.annotation.Column columnAnn = field.getAnnotation(org.dbist.annotation.Column.class);
			TableColumn tabColumn = null;

			if (columnAnn != null) {
				if (!ValueUtils.isEmpty(columnAnn.name())) {
					try {
						tabColumn = jdbcOperations.queryForObject(sql, TABLECOLUMN_ROWMAPPER, tableName, columnAnn.name().toLowerCase());
					} catch (EmptyResultDataAccessException e) {
						throw new DbistRuntimeException(ValueUtils.populate(MSG_COLUMNNOTFOUND,
								ValueUtils.toMap("column:" + columnAnn.name(), "table:" + table.getDomain() + "." + tableName)));
					}
				}
				column.setType(ValueUtils.toNull(columnAnn.type().value()));
				if (!ValueUtils.isEmpty(columnAnn.generator()))
					table.getValueGeneratorByFieldMap().put(field, getValueGenerator(columnAnn.generator()));
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

		// Identity / Sequence
		org.dbist.annotation.Sequence seqAnn = field.getAnnotation(org.dbist.annotation.Sequence.class);
		if (seqAnn != null) {
			Sequence seq = new Sequence();
			column.setSequence(seq);

			{
				String sql = getQueryCountIdentity();
				if (!ValueUtils.isEmpty(sql)) {
					sql = StringUtils.replace(sql, "${domain}", table.getDomain());
					if (jdbcOperations.queryForInt(sql, table.getName(), column.getName()) > 0)
						seq.setAutoIncrement(true);
				}
			}

			if (!seq.isAutoIncrement() && !ValueUtils.isEmpty(seqAnn.name())) {
				String sql = getQueryCountSequence();
				if (ValueUtils.isEmpty(sql)) {
					seq.setAutoIncrement(true);
				} else {
					List<String> domainNameList = ValueUtils.isEmpty(seqAnn.domain()) ? this.domainList : ValueUtils.toList(seqAnn.domain());
					String name = seqAnn.name().toLowerCase();

					boolean populated = false;
					for (String domainName : domainNameList) {
						domainName = domainName.toLowerCase();
						String _sql = StringUtils.replace(sql, "${domain}", domainName);
						if (jdbcOperations.queryForInt(_sql, name) > 0) {
							seq.setDomain(domainName);
							seq.setName(name);
							populated = true;
							break;
						}
					}

					if (!populated) {
						String errMsg = "Couldn't find sequence[${sequence}] from this(these) domain(s)[${domain}]";
						throw new IllegalArgumentException(ValueUtils.populate(errMsg,
								ValueUtils.toMap("domain:" + mapOr(domainNameList), "sequence:" + name)));
					}
				}
			}
		}
	}

	private String getFunctionLowerCase() {
		return queryMapper.getFunctionLowerCase();
	}
	private String applyEscapement(char escape) {
		String str = queryMapper.toEscapement(escape);
		if (ValueUtils.isEmpty(str))
			return "";
		return " " + str;
	}
	private String getQueryCountTable() {
		return queryMapper.getQueryCountTable();
	}
	private String getQueryPkColumnNames() {
		return queryMapper.getQueryPkColumnNames();
	}
	private String getQueryColumnNames() {
		return queryMapper.getQueryColumnNames();
	}
	private String getQueryColumnName() {
		return queryMapper.getQueryColumnName();
	}
	private String getQueryCountIdentity() {
		return queryMapper.getQueryCountIdentity();
	}
	private String getQueryCountSequence() {
		return queryMapper.getQueryCountSequence();
	}

	static class TableColumnRowMapper implements RowMapper<TableColumn> {
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
