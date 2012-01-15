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

import java.util.ArrayList;
import java.util.List;

import net.sf.common.util.ValueUtils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

/**
 * @author Steve M. Jung
 * @since 2012. 1. 5. (version 0.0.1)
 */
public abstract class AbstractDml implements Dml, InitializingBean {
	private String dbType;
	private List<String> domainList = new ArrayList<String>(2);

	@Override
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType == null ? null : dbType.toLowerCase();
	}
	@Override
	public List<String> getDomain() {
		return domainList;
	}
	public void setDomain(String domains) {
		if (ValueUtils.isEmpty(domains)) {
			domainList.clear();
			return;
		}
		for (String domain : StringUtils.tokenizeToStringArray(domains, ","))
			domainList.add(domain);
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ValueUtils.assertNotEmpty("dbType", getDbType());
		ValueUtils.assertNotEmpty("domains", getDomain());
	}
}
