package org.dbist.dml;

import java.util.ArrayList;
import java.util.List;

import net.sf.common.util.ValueUtils;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.StringUtils;

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
