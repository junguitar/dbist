/**
 * Copyright 2011-2013 the original author or authors.
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
package org.dbist.dml.jdbc;

import java.util.Map;


/**
 * @author Steve M. Jung
 * @since 2013. 9. 7. (version 2.0.3)
 */
public class QueryMapperPostgresql extends AbstractQueryMapper {

	public String getDbType() {
		return "postgresql";
	}

	public boolean isSupportedPaginationQuery() {
		return true;
	}

	public boolean isSupportedLockTimeout() {
		return false;
	}

	public String applyPagination(String sql, Map<String, ?> paramMap, int pageIndex, int pageSize, int firstResultIndex, int maxResultSize) {
		// TODO Auto-generated method stub
		return null;
	}

	public String getFunctionLowerCase() {
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

	public String getQueryColumnNames() {
		// TODO Auto-generated method stub
		return null;
	}

	public String getQueryColumnName() {
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
