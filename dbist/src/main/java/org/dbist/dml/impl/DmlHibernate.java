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

import java.util.List;
import java.util.Map;

import org.dbist.dml.AbstractDml;
import org.dbist.dml.Dml;
import org.dbist.metadata.Table;

/**
 * @author Steve M. Jung
 * @since 2011. 6. 2. (version 0.0.1)
 */
public class DmlHibernate extends AbstractDml implements Dml {

	@Override
	public Table getTable(Object obj) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void insert(T data) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void insertBatch(List<T> list) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void update(T data) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void updateBatch(List<T> list) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void update(T data, String... fieldNames) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void updateBatch(List<T> list, String... fieldNames) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void upsert(T data) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void upsertBatch(List<T> list) throws Exception {
		// TODO Auto-generated method stub

	}

	@Override
	public <T> void delete(T data) throws Exception {
		// TODO Auto-generated method stub

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
	public <T> int selectSize(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public <T> List<T> selectList(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> selectListWithLock(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> selectList(String sql, Map<String, ?> paramMap, Class<T> requiredType, int pageIndex, int pageSize) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> int deleteList(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

}
