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

import net.sf.common.util.ValueUtils;

import org.dbist.exception.DbistRuntimeException;
import org.dbist.processor.Preprocessor;
import org.springframework.beans.factory.InitializingBean;

/**
 * @author Steve M. Jung
 * @since 2012. 1. 5. (version 0.0.1)
 */
public abstract class AbstractDml implements Dml, InitializingBean {
	private Preprocessor preprocessor;

	@Override
	public Preprocessor getPreprocessor() {
		return preprocessor;
	}
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

	@Override
	public <T> T select(Class<T> clazz, Object condition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		return select(selectList(clazz, condition));
	}

	@Override
	public <T> T selectForUpdate(Class<T> clazz, Object condition) throws Exception {
		ValueUtils.assertNotNull("clazz", clazz);
		return select(selectListForUpdate(clazz, condition));
	}

	@Override
	public <T> T select(String query, Map<String, Object> paramMap, T requiredType) throws Exception {
		ValueUtils.assertNotNull("query", query);
		return select(selectList(query, paramMap, requiredType));
	}

	@Override
	public <T> T selectForUpdate(String query, Map<String, Object> paramMap, T requiredType) throws Exception {
		ValueUtils.assertNotNull("query", query);
		return select(selectListForUpdate(query, paramMap, requiredType));
	}

	@Override
	public <T> T selectByNativeQuery(String query, Map<String, Object> paramMap, T requiredType) throws Exception {
		return select(query, paramMap, requiredType);
	}

	@Override
	public <T> T selectForUpdateByNativeQuery(String query, Map<String, Object> paramMap, T requiredType) throws Exception {
		return selectForUpdate(query, paramMap, requiredType);
	}

	@Override
	public <T> List<T> selectListByNativeQuery(String query, Map<String, Object> paramMap, T requiredType) throws Exception {
		return selectList(query, paramMap, requiredType);
	}

	@Override
	public <T> List<T> selectListForUpdateNativeQuery(String query, Map<String, Object> paramMap, T requiredType) throws Exception {
		return selectListForUpdate(query, paramMap, requiredType);
	}
}
