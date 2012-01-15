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
package org.dbist.metadata;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.common.util.Closure;
import net.sf.common.util.ReflectionUtils;
import net.sf.common.util.SyncCtrlUtils;
import net.sf.common.util.ValueUtils;

import org.apache.tools.ant.util.StringUtils;
import org.dbist.dml.Dml;
import org.dbist.dml.impl.DmlJdbc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.jdbc.core.JdbcTemplate;

/**
 * @author Steve M. Jung
 * @since 2011. 7. 10 (version 0.0.1)
 */
public class Table {
	private static final Logger logger = LoggerFactory.getLogger(Table.class);

	private String domain;
	private String name;
	private List<Field> pkField = new ArrayList<Field>(1);
	private List<Field> field = new ArrayList<Field>();
	private List<Column> pkColumn = new ArrayList<Column>(1);
	private List<Column> column = new ArrayList<Column>();
	private Map<String, String> fieldNameColumNameMap = new HashMap<String, String>();
	private Map<String, String> columnNameFieldNameMap = new HashMap<String, String>();
	private static Map<Class<?>, Table> cache = new ConcurrentHashMap<Class<?>, Table>();

	public static Table get(Object obj, final Dml dml) {
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

				// Domain and Name
				org.dbist.annotation.Table tableAnn = clazz.getAnnotation(org.dbist.annotation.Table.class);
				if (tableAnn != null) {
					if (!ValueUtils.isEmpty(tableAnn.domain()))
						table.setDomain(tableAnn.domain().toLowerCase());
					if (!ValueUtils.isEmpty(tableAnn.name()))
						table.setName(tableAnn.name().toLowerCase());
				}
				populateDomainAndName(table, clazz, dml);

				// Columns
				for (Field field : ReflectionUtils.getFieldList(clazz, false)) {
					String cname = getColumnName(field, dml);
					if (cname == null)
						continue;
					Column column = table.addColumn(new Column());
					column.setName(cname);
					table.field.add(field);
				}
				return table;
			}
		});
	}
	private static final String QUERY_NUMBEROFTABLE_MYSQL = "select count(*) from information_schema.tables where lcase(table_name) = ? and lcase(table_schema) = '${domain}'";
	private static final String QUERY_NUMBEROFTABLE_ORACLE = "select count(*) from all_tables where lower(table_name) = ? and lower(owner) = '${domain}'";
	private static final Map<String, String> QUERY_NUMBEROFTABLE_MAP;
	static {
		QUERY_NUMBEROFTABLE_MAP = new HashMap<String, String>();
		QUERY_NUMBEROFTABLE_MAP.put(Dml.DBTYPE_MYSQL, QUERY_NUMBEROFTABLE_MYSQL);
		QUERY_NUMBEROFTABLE_MAP.put(Dml.DBTYPE_ORACLE, QUERY_NUMBEROFTABLE_ORACLE);
	}

	private static final String MSG_TABLENOTFOUND = "Couldn't find table of class: ${class} from these dml.domain: ${domain}. The tableNameCandidate(s) was(were) ${tableNameCandidate}.";
	private static <T> void populateDomainAndName(Table table, Class<T> clazz, Dml dml) {
		if (dml instanceof DmlJdbc) {
			DmlJdbc dmlJdbc = (DmlJdbc) dml;
			JdbcTemplate jdbcTemplate = dmlJdbc.getJdbcTemplate();

			String query = QUERY_NUMBEROFTABLE_MAP.get(dml.getDbType());
			if (query == null)
				throw new IllegalArgumentException("Couldn't find table query of dml.dbType: " + dml.getDbType()
						+ ". this type maybe unsupported yet.");

			boolean domainEmpty = ValueUtils.isEmpty(table.getDomain());
			boolean nameEmpty = ValueUtils.isEmpty(table.getName());

			if (nameEmpty) {
				String tableNameCandidate = ValueUtils.toDelimiterCase(clazz.getSimpleName(), '_').toLowerCase();
				String tableNameCandidate1 = clazz.getSimpleName().toLowerCase();
				if (domainEmpty) {
					for (String domain : dmlJdbc.getDomainList()) {
						String sql = StringUtils.replace(query, "${domain}", domain.toLowerCase());
						if (jdbcTemplate.queryForInt(sql, tableNameCandidate) > 0) {
							table.setDomain(domain);
							table.setName(tableNameCandidate);
							return;
						} else if (jdbcTemplate.queryForInt(sql, tableNameCandidate1) > 0) {
							table.setDomain(domain);
							table.setName(tableNameCandidate1);
							return;
						}
					}
					String errMsg = ValueUtils.populate(
							MSG_TABLENOTFOUND,
							ValueUtils.toMap("class:" + clazz.getSimpleName(), "domain:" + dml.getDomain(), "tableNameCandidate:"
									+ tableNameCandidate + ", " + tableNameCandidate1));
					throw new IllegalArgumentException(errMsg);
				} else {
					String sql = StringUtils.replace(query, "${domain}", table.getDomain());
					if (jdbcTemplate.queryForInt(sql, tableNameCandidate1) > 0) {
						table.setDomain(table.getDomain());
						table.setName(tableNameCandidate1);
						return;
					}
				}
			} else if (domainEmpty) {

			} else {
				String sql = StringUtils.replace(query, "${domain}", table.getDomain());
				if (jdbcTemplate.queryForInt(sql, table.getName()) > 0)
					return;
			}

		} else {
			throw new IllegalArgumentException("Unsupported yet dml: " + dml.getClass());
		}
	}
	private static String getColumnName(Field field, Dml dml) {
		// TODO String toColumnName(Field field)
		org.dbist.annotation.Column columnAnn = field.getAnnotation(org.dbist.annotation.Column.class);
		if (columnAnn != null) {
			if (columnAnn.skip())
				return null;
			if (!ValueUtils.isEmpty(columnAnn.name()))
				return columnAnn.name();
		}

		return field.getName();
	}

	public String toColumnName(String name) {
		// TODO String toColumnName(String name)
		return name;
	}

	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
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
	private Column addColumn(Column column) {
		this.column.add(column);
		return column;
	}
}
