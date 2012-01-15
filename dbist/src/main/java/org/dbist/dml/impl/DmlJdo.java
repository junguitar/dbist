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

import org.dbist.dml.AbstractDml;
import org.dbist.dml.Dml;

/**
 * @author Steve M. Jung
 * @since 2011. 6. 2. (version 0.0.1)
 */
public class DmlJdo extends AbstractDml implements Dml {

	@Override
	public <T> T select(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T selectForUpdate(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T select(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T selectForUpdate(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
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

	@Override
	public <T> List<T> selectList(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> selectListForUpdate(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void deleteList(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		
	}

}
