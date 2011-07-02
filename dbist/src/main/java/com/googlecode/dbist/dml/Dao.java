/**
 * Copyright 2011 the original author or authors.
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
package com.googlecode.dbist.dml;

import java.util.List;

public interface Dao {
	<T> T select(T data) throws Exception;
	<T> T select(Class<T> clazz, Object id) throws Exception;
	<T> T insert(T data) throws Exception;
	<T> T update(T data) throws Exception;
	<T> T upsert(T data) throws Exception;
	<T> T delete(T data) throws Exception;
	<T> T delete(Class<T> clazz, Object id) throws Exception;
	<T> List<T> list(Class<T> clazz, Object condition) throws Exception;
	<T> List<T> deleteList(Class<T> clazz, Object condition) throws Exception;
}
