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
package org.dbist.dml;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import net.sf.common.util.ValueUtils;

import org.apache.commons.collections.map.ListOrderedMap;
import org.dbist.exception.DataNotFoundException;
import org.dbist.exception.DbistRuntimeException;
import org.dbist.metadata.Table;
import org.dbist.processor.Preprocessor;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Steve M. Jung
 * @since 2012. 1. 5. (version 0.0.1)
 */
public abstract class AbstractDml implements Dml, InitializingBean {
	private Preprocessor preprocessor;

	@Override
	public Table getTable(String name) {
		Class<?> clazz = getClass(name);
		return clazz == null ? null : getTable(clazz);
	}

	public Preprocessor getPreprocessor() {
		return preprocessor;
	}
	@Override
	public void setPreprocessor(Preprocessor preprocessor) {
		this.preprocessor = preprocessor;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
	}

	protected <T> T select(List<T> list) {
		if (ValueUtils.isEmpty(list))
			return null;
		else if (list.size() > 1)
			throw new DbistRuntimeException("Selected data size is not 1: " + list.size());
		return list.get(0);
	}

	protected Query toQuery(Table table, Object condition, String... fieldNames) throws Exception {
		if (condition instanceof Query)
			return (Query) condition;
		Query query = new Query();
		if (condition instanceof HttpServletRequest) {
			boolean byFieldName = fieldNames != null && fieldNames.length != 0;
			Set<String> fieldNameSet = byFieldName ? ValueUtils.toSet(fieldNames) : null;
			HttpServletRequest request = (HttpServletRequest) condition;
			Map<String, String[]> paramMap = request.getParameterMap();
			for (String key : paramMap.keySet()) {
				if (byFieldName) {
					if (!fieldNameSet.contains(key))
						continue;
					fieldNameSet.remove(key);
				}
				Field field = table.getField(key);
				if (field == null)
					continue;
				String[] values = paramMap.get(key);
				if (ValueUtils.isEmpty(values))
					continue;
				for (String value : values)
					query.addFilter(key, value);
			}
			if (paramMap.containsKey("pageIndex"))
				query.setPageIndex(ValueUtils.toInteger(request.getParameter("pageIndex"), 0));
			if (paramMap.containsKey("pageSize"))
				query.setPageSize(ValueUtils.toInteger(request.getParameter("pageSize"), 0));
			if (paramMap.containsKey("operator") && table.getField("operator") == null)
				query.setOperator(request.getParameter("operator"));
		} else if (condition instanceof Filters) {
			ValueUtils.populate(condition, query);
		} else if (condition instanceof Filter) {
			query.addFilter((Filter) condition);
		} else {
			query.addFilterAll(condition, fieldNames);
		}
		return query;
	}
	protected Query toPkQuery(Object obj, Object condition) throws Exception {
		Class<?> clazz;
		if (obj instanceof Class)
			clazz = (Class<?>) obj;
		else if (obj instanceof String)
			clazz = getClass((String) obj);
		else
			clazz = obj.getClass();
		if (condition instanceof Object[] && ((Object[]) condition).length == 1)
			condition = ((Object[]) condition)[0];
		Query query = new Query();
		try {
			if (condition == null || condition instanceof Query)
				return (Query) condition;
			Table table = getTable(clazz);
			if (ValueUtils.isPrimitive(condition)) {
				query.addFilter(table.getPkFieldNames()[0], condition);
				return query;
			} else if (condition instanceof Object[]) {
				if (ValueUtils.isEmpty(condition))
					throw new IllegalAccessException("Requested pk condition is empty.");
				Object[] array = (Object[]) condition;
				if (ValueUtils.isPrimitive(array[0])) {
					int i = 0;
					String[] pkFieldNames = table.getPkFieldNames();
					int pkFieldSize = pkFieldNames.length;
					for (Object item : array) {
						query.addFilter(pkFieldNames[i++], item);
						if (i == pkFieldSize)
							break;
					}
					return query;
				}
			} else if (condition instanceof List) {
				if (ValueUtils.isEmpty(condition))
					throw new IllegalAccessException("Requested pk condition is empty.");
				@SuppressWarnings("unchecked")
				List<?> list = (List<Object>) condition;
				if (ValueUtils.isPrimitive(list.get(0))) {
					int i = 0;
					String[] pkFieldNames = table.getPkFieldNames();
					int pkFieldSize = pkFieldNames.length;
					for (Object item : list) {
						query.addFilter(pkFieldNames[i++], item);
						if (i == pkFieldSize)
							break;
					}
					return query;
				}
			}
			query = toQuery(table, condition, table.getPkFieldNames());
			return query;
		} finally {
			//			query.setPageIndex(0);
			//			query.setPageSize(2);
		}
	}

	@SuppressWarnings("unchecked")
	protected static <T> T newInstance(Class<T> clazz) throws InstantiationException, IllegalAccessException {
		if (clazz.equals(Map.class))
			return (T) ListOrderedMap.class.newInstance();
		return clazz.newInstance();
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T select(T data) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Class<T> clazz = (Class<T>) data.getClass();
		Query query = toPkQuery(clazz, data);
		return select(selectList(clazz, query));
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T selectWithLock(T data) throws Exception {
		ValueUtils.assertNotNull("data", data);
		Class<T> clazz = (Class<T>) data.getClass();
		Query query = toPkQuery(clazz, data);
		return select(selectListWithLock(clazz, query));
	}

	@Override
	public <T> T select(Class<T> clazz, Object... pkCondition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotEmpty("pkCondition", pkCondition);
		Query query = toPkQuery(clazz, pkCondition);
		return select(selectList(clazz, query));
	}

	@Override
	public <T> T selectWithLock(Class<T> clazz, Object... pkCondition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotEmpty("pkCondition", pkCondition);
		Query query = toPkQuery(clazz, pkCondition);
		return select(selectListWithLock(clazz, query));
	}

	@Override
	public <T> T selectByCondition(Class<T> clazz, Object condition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("condition", condition);
		return select(selectList(clazz, condition));
	}

	@Override
	public <T> T select(String tableName, Object pkCondition, Class<T> requiredType) throws Exception {
		ValueUtils.assertNotNull("tableName", tableName);
		ValueUtils.assertNotNull("pkCondition", pkCondition);
		ValueUtils.assertNotNull("requiredType", requiredType);
		Class<?> clazz = getClass(tableName);
		Query query = toPkQuery(clazz, pkCondition);
		Object obj = select(selectList(clazz, query));
		if (obj == null)
			return null;
		return ValueUtils.populate(obj, newInstance(requiredType));
	}

	@Override
	public <T> T selectWithLock(String tableName, Object pkCondition, Class<T> requiredType) throws Exception {
		ValueUtils.assertNotNull("tableName", tableName);
		ValueUtils.assertNotNull("pkCondition", pkCondition);
		ValueUtils.assertNotNull("requiredType", requiredType);
		Class<?> clazz = getClass(tableName);
		Query query = toPkQuery(clazz, pkCondition);
		Object obj = selectWithLock(selectList(clazz, query));
		if (obj == null)
			return null;
		return ValueUtils.populate(obj, newInstance(requiredType));
	}

	@Override
	public <T> T selectByCondition(String tableName, Object condition, Class<T> requiredType) throws Exception {
		ValueUtils.assertNotNull("tableName", tableName);
		ValueUtils.assertNotNull("condition", condition);
		ValueUtils.assertNotNull("requiredType", requiredType);
		Class<?> clazz = getClass(tableName);
		Query query = toPkQuery(clazz, condition);
		Object obj = select(selectList(clazz, query));
		if (obj == null)
			return null;
		return ValueUtils.populate(obj, newInstance(requiredType));
	}

	@Override
	public <T> T selectByConditionWithLock(String tableName, Object condition, Class<T> requiredType) throws Exception {
		ValueUtils.assertNotNull("tableName", tableName);
		ValueUtils.assertNotNull("condition", condition);
		ValueUtils.assertNotNull("requiredType", requiredType);
		Class<?> clazz = getClass(tableName);
		Query query = toPkQuery(clazz, condition);
		Object obj = selectWithLock(selectList(clazz, query));
		if (obj == null)
			return null;
		return ValueUtils.populate(obj, newInstance(requiredType));
	}

	@Override
	public <T> T selectByConditionWithLock(Class<T> clazz, Object condition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("condition", condition);
		return select(selectListWithLock(clazz, condition));
	}

	@Override
	public <T> T selectByQl(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws Exception {
		ValueUtils.assertNotNull("query", sql);
		ValueUtils.assertNotNull("paramMap", paramMap);
		ValueUtils.assertNotNull("requiredType", requiredType);
		return select(selectListByQl(sql, paramMap, requiredType, 0, 2));
	}

	@Override
	public <T> Page<T> selectPage(Class<T> clazz, Query query) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		if (query == null)
			query = new Query();
		Page<T> page = new Page<T>();
		page.setIndex(query.getPageIndex());
		page.setSize(query.getPageSize());
		page.setTotalSize(selectSize(clazz, query));
		if (page.getIndex() >= 0 && page.getSize() > 0 && page.getTotalSize() > 0)
			page.setLastIndex((page.getTotalSize() / page.getSize()) - (page.getTotalSize() % page.getSize() == 0 ? 1 : 0));
		page.setList(selectList(clazz, query));
		return page;
	}

	@Override
	public <T> int selectSize(String tableName, Object condition) throws Exception {
		ValueUtils.assertNotNull("tableName", tableName);
		return selectSize(getClass(tableName), condition);
	}

	@Override
	public <T> List<T> selectList(String tableName, Object condition, Class<T> requiredType) throws Exception {
		ValueUtils.assertNotNull("tableName", tableName);
		ValueUtils.assertNotNull("requiredType", requiredType);
		List<?> objList = selectList(getClass(tableName), condition);
		List<T> list = new ArrayList<T>();
		for (Object obj : objList)
			list.add(ValueUtils.populate(obj, newInstance(requiredType)));
		return list;
	}

	@Override
	public <T> List<T> selectListWithLock(String tableName, Object condition, Class<T> requiredType) throws Exception {
		ValueUtils.assertNotNull("tableName", tableName);
		ValueUtils.assertNotNull("requiredType", requiredType);
		List<?> objList = selectListWithLock(getClass(tableName), condition);
		List<T> list = new ArrayList<T>();
		for (Object obj : objList)
			list.add(ValueUtils.populate(obj, newInstance(requiredType)));
		return list;
	}

	@Override
	public <T> Page<T> selectPage(String tableName, Query query, Class<T> requiredType) throws Exception {
		ValueUtils.assertNotNull("tableName", tableName);
		ValueUtils.assertNotNull("requiredType", requiredType);
		Page<T> page = new Page<T>();
		page.setIndex(query.getPageIndex());
		page.setSize(query.getPageSize());
		page.setTotalSize(selectSize(tableName, query));
		if (page.getIndex() >= 0 && page.getSize() > 0 && page.getTotalSize() > 0)
			page.setLastIndex((page.getTotalSize() / page.getSize()) - (page.getTotalSize() % page.getSize() == 0 ? 1 : 0));
		page.setList(selectList(tableName, query, requiredType));
		return page;
	}

	@Override
	public <T> T selectBySql(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws Exception {
		return selectByQl(sql, paramMap, requiredType);
	}

	@Override
	public <T> List<T> selectListBySql(String sql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception {
		return selectListByQl(sql, paramMap, requiredType, pageIndex, pageSize);
	}

	@Override
	public <T> Page<T> selectPageBySql(String sql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception {
		return selectPageByQl(sql, paramMap, requiredType, pageIndex, pageSize);
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T insert(Class<T> clazz, Object data) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("data", data);
		if (data.getClass().isAssignableFrom(clazz)) {
			T obj = (T) data;
			insert(obj);
			return obj;
		}
		T obj = ValueUtils.populate(data, clazz.newInstance());
		insert(obj);
		return obj;
	}

	@Override
	public void insertBatch(Class<?> clazz, List<Object> list) throws Exception {
		insertBatch(toRequiredType(list, clazz));
	}

	@Override
	public void insert(String tableName, Object data) throws Exception {
		insert(getClass(tableName), data);
	}

	@Override
	public void insertBatch(String tableName, List<Object> list) throws Exception {
		insertBatch(toRequiredType(list, getClass(tableName)));
	}

	@Override
	public <T> T update(Class<T> clazz, Object data) throws Exception {
		return _update(clazz, data);
	}

	@Override
	public void updateBatch(Class<?> clazz, List<Object> list) throws Exception {
		_updateBatch(clazz, list);
	}

	@Override
	public <T> T update(Class<T> clazz, Object data, String... fieldNames) throws Exception {
		return _update(clazz, data, fieldNames);
	}

	@Override
	public void updateBatch(Class<?> clazz, List<?> list, String... fieldNames) throws Exception {
		_updateBatch(clazz, list, fieldNames);
	}

	public <T> T _update(Class<T> clazz, Object data, String... fieldNames) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		ValueUtils.assertNotNull("data", data);
		if (data.getClass().equals(clazz)) {
			@SuppressWarnings("unchecked")
			T obj = (T) data;
			update(obj, fieldNames);
			return obj;
		}
		T obj = select(clazz, data);
		if (obj == null) {
			Table table = getTable(data);
			throw new DataNotFoundException("Couldn't find data from table[" + table.getDomain() + "." + table.getName() + "]");
		}
		obj = ValueUtils.populate(data, obj, fieldNames);
		update(obj, fieldNames);
		return obj;
	}

	@SuppressWarnings("unchecked")
	private static <T> List<T> toRequiredType(List<?> list, Class<T> requiredType, String... fieldNames) throws InstantiationException,
			IllegalAccessException {
		if (ValueUtils.isEmpty(list))
			return (List<T>) list;
		if (list.get(0).getClass().equals(requiredType))
			return (List<T>) list;
		List<T> dataList = new ArrayList<T>(list.size());
		for (Object obj : list)
			dataList.add(ValueUtils.populate(obj, newInstance(requiredType), fieldNames));
		return dataList;
	}

	public void _updateBatch(Class<?> clazz, List<?> list, String... fieldNames) throws Exception {
		updateBatch(toRequiredType(list, clazz, fieldNames), fieldNames);
	}

	@Override
	public void update(String tableName, Object data) throws Exception {
		update(getClass(tableName), data);
	}

	@Override
	public void updateBatch(String tableName, List<Object> list) throws Exception {
		updateBatch(getClass(tableName), list);
	}

	@Override
	public void update(String tableName, Object data, String... fieldNames) throws Exception {
		update(getClass(tableName), data, fieldNames);
	}

	@Override
	public void updateBatch(String tableName, List<Object> list, String... fieldNames) throws Exception {
		updateBatch(getClass(tableName), list, fieldNames);
	}

	@Override
	public void upsert(Object data) throws Exception {
		if (select(data) == null)
			insert(data);
		else
			update(data);
	}

	@Override
	public void upsertBatch(List<?> list) throws Exception {
		List<Object> insertList = new ArrayList<Object>();
		List<Object> updateList = new ArrayList<Object>();
		for (Object data : list) {
			if (select(data) == null)
				insertList.add(data);
			else
				updateList.add(data);
		}
		insertBatch(insertList);
		updateBatch(updateList);
	}

	@Override
	public <T> T upsert(Class<T> clazz, Object data) throws Exception {
		return select(clazz, data) == null ? insert(clazz, data) : update(clazz, data);
	}

	@Override
	public <T> List<T> upsertBatch(Class<T> clazz, List<Object> list) throws Exception {
		List<T> newList = toRequiredType(list, clazz);
		upsertBatch(toRequiredType(list, clazz));
		return newList;
	}

	@Override
	public void upsert(String tableName, Object data) throws Exception {
		upsert(getClass(tableName), data);
	}

	@Override
	public void upsertBatch(String tableName, List<Object> list) throws Exception {
		upsertBatch(getClass(tableName), list);
	}

	@Override
	public <T> T delete(Class<T> clazz, Object... pkCondition) throws Exception {
		T data = select(clazz, pkCondition);
		if (data == null)
			return null;
		delete(data);
		return data;
	}

	@Override
	public void deleteBatch(Class<?> clazz, List<Object> list) throws Exception {
		deleteBatch(toRequiredType(list, clazz));
	}

	@Override
	public <T> T deleteByCondition(Class<T> clazz, Object condition) throws Exception {
		T data = selectByCondition(clazz, condition);
		if (data == null)
			return null;
		delete(data);
		return data;
	}

	@Override
	public void delete(String tableName, Object... pkCondition) throws Exception {
		delete(getClass(tableName), pkCondition);
	}

	@Override
	public void deleteBatch(String tableName, List<Object> list) throws Exception {
		deleteBatch(getClass(tableName), list);
	}

	@Override
	public <T> void deleteByCondition(String tableName, Object condition) throws Exception {
		Object data = selectByCondition(tableName, condition, getClass(tableName));
		if (data == null)
			return;
		delete(data);
	}
}
