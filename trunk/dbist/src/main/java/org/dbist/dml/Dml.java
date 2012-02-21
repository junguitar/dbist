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
	Table getTable(Object obj);
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
	<T> T select(Class<T> clazz, Object pkCondition) throws Exception;
	<T> T selectWithLock(Class<T> clazz, Object pkCondition) throws Exception;

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
	<T> T select(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws Exception;

	/**
	 * Select a data as the requiredType by the query (SQL query) and the paramMap
	 * 
	 * @param query
	 * @param paramMap
	 * @param requiredType
	 * @return
	 * @throws Exception
	 */
	<T> T selectByNativeQuery(String sql, Map<String, ?> paramMap, Class<T> requiredType) throws Exception;

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
	<T> void insert(T data) throws Exception;
	<T> void insertBatch(List<T> list) throws Exception;

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
	<T> void update(T data) throws Exception;
	<T> void updateBatch(List<T> list) throws Exception;

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

	/**
	 * Update some fields of data to the database table mapped to T class.
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param data
	 *            The data to update
	 * @param fieldNames
	 *            The fieldName array of the data to update
	 * @return The data updated
	 * @throws Exception
	 */
	<T> void update(T data, String... fieldNames) throws Exception;
	<T> void updateBatch(List<T> list, String... fieldNames) throws Exception;

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
	<T> void upsert(T data) throws Exception;
	<T> void upsertBatch(List<T> list) throws Exception;

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
	<T> void delete(T data) throws Exception;
	<T> void deleteBatch(List<T> list) throws Exception;

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
	<T> T delete(Class<T> clazz, Object condition) throws Exception;

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
	<T> int selectSize(Class<T> clazz, Object condition) throws Exception;

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

	/**
	 * Select some data as the requiredType by the query and the paramMap<br>
	 * In case of DmlJdbc query means SQL query. In case of DmlHibernate query means HQL query. ...
	 * 
	 * @param sql
	 * @param paramMap
	 * @param requiredType
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	<T> List<T> selectList(String sql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;

	/**
	 * Select some data as the requiredType by the query (SQL query) and the paramMap
	 * 
	 * @param sql
	 * @param paramMap
	 * @param requiredType
	 * @param pageIndex
	 * @param pageSize
	 * @return
	 * @throws Exception
	 */
	<T> List<T> selectListByNativeQuery(String sql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception;

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
	<T> int deleteList(Class<T> clazz, Object condition) throws Exception;
}
