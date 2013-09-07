package org.dbist.dml.querymapper;

import java.util.Map;

public interface QueryMapper {
	public String getDbType();
	public boolean isPaginationQuerySupported();
	public String applyPagination(String sql, Map<String, ?> paramMap, int pageIndex, int pageSize, int firstResultIndex, int maxResultSize);
	public String getFunctionNameLowerCase();
	public String getQueryCountTable();
	public String getQueryPkColumnNames();
	public String getQueryColumns();
	public String getQueryCountIdentity();
	public String getQueryCountSequence();
}
