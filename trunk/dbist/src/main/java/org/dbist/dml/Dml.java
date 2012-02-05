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

import org.dbist.processor.Processor;

/**
 * DML (Data Manipulation Languge) operator or DAO (Data Access Object)
 * executing queries.
 * 
 * @author Steve M. Jung
 * @since 2011. 6. 2. (version 0.0.1)
 */
public interface Dml {
	public static final String DBTYPE_MYSQL = "mysql";
	public static final String DBTYPE_ORACLE = "oracle";

	String getDbType();
	String getDomain();
	Processor getPreprocessor();

	/**
	 * Select a data from the database table mapped to T class by primary key
	 * fields' value of data parameter.<br>
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
	<T> T selectForUpdate(T data) throws Exception;

	/**
	 * Select a data from the database table mapped to T class by condition
	 * parameter.<br>
	 * The data type of condition parameter can be primary key value (a value,
	 * array, List, or HttpServletRequest), Map, Query, Filters, Filter
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
	<T> T select(Class<T> clazz, Object condition) throws Exception;
	<T> T selectForUpdate(Class<T> clazz, Object condition) throws Exception;

	<T> T select(String query, Map<String, Object> paramMap, T requiredType) throws Exception;
	<T> T selectForUpdate(String query, Map<String, Object> paramMap, T requiredType) throws Exception;

	<T> T selectByNativeQuery(String query, Map<String, Object> paramMap, T requiredType) throws Exception;
	<T> T selectForUpdateByNativeQuery(String query, Map<String, Object> paramMap, T requiredType) throws Exception;

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
	<T> void insertBatch(List<T> list) throws Exception;

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
	<T> T update(T data) throws Exception;
	<T> void updateBatch(List<T> list) throws Exception;

	/**
	 * Update some fields of data to the database table mapped to T class.
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param data
	 *            The data to update
	 * @param fieldName
	 *            The fieldName array of the data to update
	 * @return The data updated
	 * @throws Exception
	 */
	<T> T update(T data, String... fieldName) throws Exception;
	<T> void updateBatch(List<T> list, String... filedName) throws Exception;

	/**
	 * Upsert (Insert or update) a data to the database table mapped to T class.
	 * 
	 * @param <T>
	 *            The object class mapped to a database table
	 * @param data
	 *            data The data to upsert
	 * @return The data upserted
	 * @throws Exception
	 */
	<T> T upsert(T data) throws Exception;
	<T> void upsertBatch(List<T> list) throws Exception;

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
	<T> T delete(T data) throws Exception;
	<T> void deleteBatch(List<T> list) throws Exception;

	/**
	 * Delete a data to the database table mapped to T class. by condition
	 * parameter.<br>
	 * The data type of condition parameter can be primary key value (a value,
	 * array, List, or HttpServletRequest), Map, Query, Filters, Filter
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
	<T> int count(Class<T> clazz, Object condition) throws Exception;

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
	<T> List<T> selectListForUpdate(Class<T> clazz, Object condition) throws Exception;

	<T> List<T> selectList(String query, Map<String, Object> paramMap, T requiredType) throws Exception;
	<T> List<T> selectListForUpdate(String query, Map<String, Object> paramMap, T requiredType) throws Exception;

	<T> List<T> selectListByNativeQuery(String query, Map<String, Object> paramMap, T requiredType) throws Exception;
	<T> List<T> selectListForUpdateNativeQuery(String query, Map<String, Object> paramMap, T requiredType) throws Exception;

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
	<T> void deleteList(Class<T> clazz, Object condition) throws Exception;
}
