package org.dbist.dml;

import net.sf.common.util.ValueUtils;

import org.springframework.beans.factory.InitializingBean;

public abstract class AbstractDml implements Dml, InitializingBean {
	private String dbType;

	@Override
	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		ValueUtils.assertNotEmpty("dbType", getDbType());
	}
}
