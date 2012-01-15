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
	private String domain;
	private List<String> domainList = new ArrayList<String>(2);

	@Override
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType == null ? null : dbType.toLowerCase();
	}
	@Override
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
		if (ValueUtils.isEmpty(domain)) {
			domainList.clear();
			return;
		}
		for (String d : StringUtils.tokenizeToStringArray(domain, ","))
			domainList.add(d);
	}
	public List<String> getDomainList() {
		return domainList;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ValueUtils.assertNotEmpty("dbType", getDbType());
		ValueUtils.assertNotEmpty("domains", getDomain());
	}
}
