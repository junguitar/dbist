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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.common.util.ValueUtils;

import org.dbist.dml.AbstractDml;
import org.dbist.dml.Dml;
import org.dbist.dml.Filter;
import org.dbist.dml.Filters;
import org.dbist.dml.Order;
import org.dbist.dml.Query;
import org.dbist.metadata.Column;
import org.dbist.metadata.Table;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

/**
 * @author Steve M. Jung
 * @since 2011. 6. 2. (version 0.0.1)
 */
public class DmlJdbc extends AbstractDml implements Dml {
	private static final List<String> DBTYPE_SUPPORTED_LIST = ValueUtils.toList("hsqldb", "mysql", "postgresql", "oracle", "sqlserver", "db2");

	private JdbcTemplate jdbcTemplate;
	private NamedParameterJdbcTemplate namedParameterJdbcTemplate;

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
		Table table = Table.get(clazz, this);
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

	@Override
	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();
		ValueUtils.assertNotEmpty("jdbcTemplate", getJdbcTemplate());
		ValueUtils.assertNotEmpty("namedParameterJdbcTemplate", getNamedParameterJdbcTemplate());
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

}
