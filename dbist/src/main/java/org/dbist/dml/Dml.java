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

import java.util.List;
import java.util.Map;

import org.dbist.metadata.Table;
import org.dbist.processor.Preprocessor;

/**
 * DML (Data Manipulation Languge) operator or DAO (Data Access Object) executing queries.
 * 
 * @author Steve M. Jung
 * @since 2011. 6. 2. (version 0.0.1)
 */
public interface Dml {
	String getDbType();
	Class<?> getClass(String tableName);
	Table getTable(Object obj);
	Table getTable(String obj);
	void setPreprocessor(Preprocessor preprocessor);

	/**
	 * Select a data from the database table mapped to T class by primary key fields' value of data parameter.<br>
	 * The data parameter must be set primary key fields' value.
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param data
	 *            The data wanted to select
	 * @return The data selected
	 * @throws Exception
	 */
	<T> T select(T data) throws Exception;
	<T> T selectWithLock(T data) throws Exception;

	/**
	 * Select a data from the database table mapped to T class by condition parameter.<br>
	 * The data type of condition parameter can be primary key value (a value, array, List, or HttpServletRequest), Map, Query, Filters, Filter
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param clazz
	 *            The object class mapped to a database table
	 * @param pkCondition
	 *            The primary key condition wanted to select
	 * @return The data selected
	 * @throws Exception
	 */
	<T> T select(Class<T> clazz, Object... pkCondition) throws Exception;
	<T> T selectWithLock(Class<T> clazz, Object... pkCondition) throws Exception;

	/**
	 * Select a data from the database table mapped to T class by condition parameter.<br>
	 * The data type of condition parameter can be primary key value (a value, array, List, or HttpServletRequest), Map, Query, Filters, Filter
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param clazz
	 *            The object class mapped to a database table
	 * @param condition
	 *            The condition wanted to select
	 * @return The data selected
	 * @throws Exception
	 */
	<T> T selectByCondition(Class<T> clazz, Object condition) throws Exception;
	<T> T selectByConditionWithLock(Class<T> clazz, Object condition) throws Exception;

	<T> T select(String tableName, Object pkCondition, Class<T> requiredType) throws Exception;
	<T> T selectWithLock(String tableName, Object pkCondition, Class<T> requiredType) throws Exception;
	<T> T selectByCondition(String tableName, Object condition, Class<T> requiredType) throws Exception;
	<T> T selectByConditionWithLock(String tableName, Object condition, Class<T> requiredType) throws Exception;

	/**
	 * Select a data as the requiredType by the query and the paramMap<br>
	 * In case of DmlJdbc query means SQL query. In case of DmlHibernate query means HQL query. ...
	 * 
	 * @param query
	 * @param paramMap
	 * @param requiredType
	 * @return
	 * @throws Exception
	 */
	<T> T selectByQl(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws Exception;
	<T> T selectByQlPath(String qlPath, Map<String, ?> paramMap, Class<T> requiredType) throws Exception;

	/**
	 * Select a data as the requiredType by the query (SQL query) and the paramMap
	 * 
	 * @param query
	 * @param paramMap
	 * @param requiredType
	 * @return
	 * @throws Exception
	 */
	<T> T selectBySql(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws Exception;
	<T> T selectBySqlPath(String sqlPath, Map<String, ?> paramMap, Class<T> requiredType) throws Exception;

	/**
	 * Insert a data to the database table mapped to T class.
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param data
	 *            The data to insert
	 * @return The data inserted
	 * @throws Exception
	 */
	<T> T insert(T data) throws Exception;
	void insertBatch(List<?> list) throws Exception;
	void insert(Object data, String... fieldNames) throws Exception;
	void insertBatch(List<?> list, String... fieldNames) throws Exception;

	/**
	 * Insert a data to the database table mapped to T class.
	 * 
	 * @param clazz
	 *            The class mapped to a database table
	 * @param data
	 *            The data to insert
	 * @return The data inserted
	 * @throws Exception
	 */
	<T> T insert(Class<T> clazz, Object data) throws Exception;
	void insertBatch(Class<?> clazz, List<Object> list) throws Exception;
	void insert(Class<?> clazz, Object data, String... fieldNames) throws Exception;
	void insertBatch(Class<?> clazz, List<Object> list, String... fieldNames) throws Exception;

	void insert(String tableName, Object data) throws Exception;
	void insertBatch(String tableName, List<Object> list) throws Exception;
	void insert(String tableName, Object data, String... fieldNames) throws Exception;
	void insertBatch(String tableName, List<Object> list, String... fieldNames) throws Exception;

	/**
	 * Update a data to the database table mapped to T class.
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param data
	 *            The data to update
	 * @return The data updated
	 * @throws Exception
	 */
	void update(Object data) throws Exception;
	void updateBatch(List<?> list) throws Exception;
	void update(Object data, String... fieldNames) throws Exception;
	void updateBatch(List<?> list, String... fieldNames) throws Exception;

	/**
	 * Update a data to the database table mapped to T class.
	 * 
	 * @param clazz
	 *            The class mapped to a database table
	 * @param data
	 *            The data to update
	 * @return The data updated
	 * @throws Exception
	 */
	<T> T update(Class<T> clazz, Object data) throws Exception;
	void updateBatch(Class<?> clazz, List<Object> list) throws Exception;
	<T> T update(Class<T> clazz, Object data, String... fieldNames) throws Exception;
	void updateBatch(Class<?> clazz, List<?> list, String... fieldNames) throws Exception;

	void update(String tableName, Object data) throws Exception;
	void updateBatch(String tableName, List<Object> list) throws Exception;
	void update(String tableName, Object data, String... fieldNames) throws Exception;
	void updateBatch(String tableName, List<Object> list, String... fieldNames) throws Exception;

	/**
	 * Upsert (Insert or update) a data to the database table mapped to T class.
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param data
	 *            The data to upsert
	 * @return The data upserted
	 * @throws Exception
	 */
	void upsert(Object data) throws Exception;
	void upsertBatch(List<?> list) throws Exception;
	void upsert(Object data, String... fieldNames) throws Exception;
	void upsertBatch(List<?> list, String... fieldNames) throws Exception;

	/**
	 * Upsert (Insert or update) a data to the database table mapped to T class.
	 * 
	 * @param clazz
	 *            The class mapped to a database table
	 * @param data
	 *            The data to upsert
	 * @return The data upserted
	 * @throws Exception
	 */
	<T> T upsert(Class<T> clazz, Object data) throws Exception;
	<T> List<T> upsertBatch(Class<T> clazz, List<Object> list) throws Exception;
	void upsert(Class<?> clazz, Object data, String... fieldNames) throws Exception;
	void upsertBatch(Class<?> clazz, List<Object> list, String... fieldNames) throws Exception;

	void upsert(String tableName, Object data) throws Exception;
	void upsertBatch(String tableName, List<Object> list) throws Exception;
	void upsert(String tableName, Object data, String... fieldNames) throws Exception;
	void upsertBatch(String tableName, List<Object> list, String... fieldNames) throws Exception;

	/**
	 * Delete a data to the database table mapped to T class.
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param data
	 *            The data to delete
	 * @return The data deleted
	 * @throws Exception
	 */
	void delete(Object data) throws Exception;
	void deleteBatch(List<?> list) throws Exception;

	/**
	 * Delete a data to the database table mapped to T class. by condition parameter.<br>
	 * The data type of condition parameter can be primary key value (a value, array, List, or HttpServletRequest), Map, Query, Filters, Filter
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param clazz
	 *            The object class mapped to a database table
	 * @param condition
	 *            The condition wanted to delete
	 * @return The data deleted
	 * @throws Exception
	 */
	<T> T delete(Class<T> clazz, Object... pkCondition) throws Exception;
	void deleteBatch(Class<?> clazz, List<Object> list) throws Exception;
	<T> T deleteByCondition(Class<T> clazz, Object condition) throws Exception;

	void delete(String tableName, Object... pkCondition) throws Exception;
	void deleteBatch(String tableName, List<Object> list) throws Exception;
	void deleteByCondition(String tableName, Object condition) throws Exception;

	/**
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param clazz
	 *            The object class mapped to a database table
	 * @param condition
	 *            The condition wanted to delete
	 * @return The size of data counted
	 * @throws Exception
	 */
	int selectSize(Class<?> clazz, Object condition) throws Exception;

	/**
	 * Select some data from the database table mapped to T class<br>
	 * by condition parameter<br>
	 * The data type of condition parameter can be Map, Query, Filters, Filter
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param clazz
	 *            The object class mapped to a database table
	 * @param condition
	 *            The condition wanted to select
	 * @return The data list selected
	 * @throws Exception
	 */
	<T> List<T> selectList(Class<T> clazz, Object condition) throws Exception;
	<T> List<T> selectListWithLock(Class<T> clazz, Object condition) throws Exception;
	<T> Page<T> selectPage(Class<T> clazz, Query query) throws Exception;

	<T> int selectSize(String tableName, Object condition) throws Exception;
	<T> List<T> selectList(String tableName, Object condition, Class<T> requiredType) throws Exception;
	<T> List<T> selectListWithLock(String tableName, Object condition, Class<T> requiredType) throws Exception;
	<T> Page<T> selectPage(String tableName, Query query, Class<T> requiredType) throws Exception;

	/**
	 * Select some data as the requiredType by the query statement and the paramMap<br>
	 * In case of DmlJdbc query means SQL query. In case of DmlHibernate query means HQL query. ...<br>
	 * If you don't want pagination, you would input pageIndex: 0 and pageSize: 0
	 * 
	 * @param sql
	 * @param paramMap
	 * @param requiredType
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	<T> List<T> selectListByQl(String ql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;
	<T> Page<T> selectPageByQl(String ql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;
	/**
	 * Select some data as the requiredType by the query statement (which is in the path) and the paramMap<br>
	 * The path means classpath or filepath<br>
	 * In case of DmlJdbc query means SQL query. In case of DmlHibernate query means HQL query. ...<br>
	 * If you don't want pagination, you would input pageIndex: 0 and pageSize: 0
	 * 
	 * @param qlPath
	 * @param paramMap
	 * @param requiredType
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	<T> List<T> selectListByQlPath(String qlPath, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;
	<T> Page<T> selectPageByQlPath(String qlPath, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;

	/**
	 * Select some data as the requiredType by the query statement (SQL query) and the paramMap
	 * 
	 * @param sql
	 * @param paramMap
	 * @param requiredType
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	<T> List<T> selectListBySql(String sql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;
	<T> Page<T> selectPageBySql(String sql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;
	/**
	 * Select some data as the requiredType by the query statement (SQL query) and the paramMap<br>
	 * The path means classpath or filepath
	 * 
	 * @param sqlPath
	 * @param paramMap
	 * @param requiredType
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	<T> List<T> selectListBySqlPath(String sqlPath, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;
	<T> Page<T> selectPageBySqlPath(String sqlPath, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;

	/**
	 * Delete some data from the database table mappedt to T class<br>
	 * by condition parameter<br>
	 * The data type of condition parameter can be Map, Query, Filters, Filter
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param clazz
	 *            The object class mapped to a database table
	 * @param condition
	 *            The condition wanted to delete
	 * @throws Exception
	 */
	int deleteList(Class<?> clazz, Object condition) throws Exception;

	/**
	 * Execute CUD (insert, update, or delete) by query statement<br>
	 * In case of DmlJdbc query means SQL query. In case of DmlHibernate query means HQL query. ...
	 * 
	 * @param ql
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	int executeByQl(String ql, Map<String, ?> paramMap) throws Exception;
	/**
	 * Execute CUD (insert, update, or delete) by query statement (which is in the path) and the paramMap<br>
	 * In case of DmlJdbc query means SQL query. In case of DmlHibernate query means HQL query. ...
	 * 
	 * @param qlPath
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	int executeByQlPath(String qlPath, Map<String, ?> paramMap) throws Exception;

	/**
	 * Execute CUD (insert, update, or delete) by query statement (SQL) and the paramMap<br>
	 * The path means classpath or filepath<br>
	 * In case of DmlJdbc query means SQL query. In case of DmlHibernate query means HQL query. ...<br>
	 * 
	 * @param sql
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	int executeBySql(String sql, Map<String, ?> paramMap) throws Exception;
	int executeBySqlPath(String sqlPath, Map<String, ?> paramMap) throws Exception;
}
