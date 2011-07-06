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
package org.dbist.dml.impl;

import java.util.List;

import org.dbist.dml.Dml;
import org.springframework.orm.jdo.support.JdoDaoSupport;

/**
 * @author Steve M. Jung
 * @since 2 June 2011 (version 0.0.1)
 */
public class DmlJdo extends JdoDaoSupport implements Dml {

	@Override
	public <T> T select(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T select(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T insert(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T update(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T update(T data, String... fieldName) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T upsert(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T delete(T data) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> T delete(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> List<T> selectList(Class<T> clazz, Object condition) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public <T> void deleteList(Class<T> clazz, Object condition)
			throws Exception {
		// TODO Auto-generated method stub
		
	}

}
