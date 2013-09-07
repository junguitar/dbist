package org.dbist.dml.querymapper;

import java.util.Map;

public class PostgreSqlQueryMapper extends AbstractQueryMapper {

	public String getDbType() {
		return "postgresql";
	}

	public boolean isPaginationQuerySupported() {
		return true;
	}

	public String applyPagination(String sql, Map<String, ?> paramMap, int pageIndex, int pageSize, int firstResultIndex, int maxResultSize) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFunctionNameLowerCase() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueryCountTable() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueryPkColumnNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueryColumns() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueryCountIdentity() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueryCountSequence() {
		// TODO Auto-generated method stub
		return null;
	}

}
