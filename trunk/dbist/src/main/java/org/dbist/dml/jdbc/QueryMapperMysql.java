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
public class QueryMapperMysql extends AbstractQueryMapper {

	public String getDbType() {
		return "mysql";
	}

	public boolean isSupportedPaginationQuery() {
		return true;
	}

	public boolean isSupportedLockTimeout() {
		return false;
	}

	public String applyPagination(String sql, Map<String, ?> paramMap, int pageIndex, int pageSize, int firstResultIndex, int maxResultSize) {
		boolean pagination = pageIndex >= 0 && pageSize > 0;
		boolean fragment = firstResultIndex > 0 || maxResultSize > 0;
		if (!pagination && !fragment)
			return sql;
		if (!pagination) {
			pageIndex = 0;
			pageSize = 0;
		}
		if (firstResultIndex < 0)
			firstResultIndex = 0;
		if (maxResultSize < 0)
			maxResultSize = 0;

		@SuppressWarnings("unchecked")
		Map<String, Object> _paramMap = (Map<String, Object>) paramMap;
		String subsql = null;
		int forUpdateIndex = sql.toLowerCase().lastIndexOf("for update");
		if (forUpdateIndex > -1) {
			subsql = sql.substring(forUpdateIndex - 1);
			sql = sql.substring(0, forUpdateIndex - 1);
		}

		StringBuffer buf = new StringBuffer();
		int pageFromIndex = pagination ? pageIndex * pageSize : 0;
		int offset = pageFromIndex + firstResultIndex;
		long limit = 0;
		if (pageSize > 0) {
			limit = pageSize - firstResultIndex;
			if (maxResultSize > 0)
				limit = Math.min(limit, maxResultSize);
		} else if (maxResultSize > 0) {
			limit = maxResultSize;
		} else if (limit == 0) {
			limit = Long.MAX_VALUE;
		}
		buf.append(sql);
		if (offset > 0 && limit > 0) {
			_paramMap.put("__offset", offset);
			_paramMap.put("__limit", limit);
			buf.append(" limit :__offset, :__limit");
		} else if (limit > 0) {
			_paramMap.put("__limit", limit);
			buf.append(" limit :__limit");
		}

		if (subsql != null)
			buf.append(subsql);
		return buf.toString();
	}

	public String toEscapementForFilter(char escape) {
		return null;
	}

	public String getFunctionLowerCase() {
		return "lower";
	}

	public String getQueryCountTable() {
		return "select count(*) from information_schema.tables where lower(table_schema) = '${domain}' and lower(table_name) = ?";
	}

	public String getQueryPkColumnNames() {
		return "select lower(column_name) name from information_schema.key_column_usage where lower(table_schema) = '${domain}' and lower(table_name) = ? and constraint_name = 'PRIMARY' order by ordinal_position";
	}

	public String getQueryColumnNames() {
		return "select lower(column_name) name, data_type dataType from information_schema.columns where lower(table_schema) = '${domain}' and lower(table_name) = ? order by ordinal_position";
	}

	public String getQueryColumnName() {
		return "select lower(column_name) name, data_type dataType from information_schema.columns where lower(table_schema) = '${domain}' and lower(table_name) = ? and lower(column_name) = ?";
	}

	public String getQueryCountIdentity() {
		return "";
	}

	public String getQueryCountSequence() {
		return "";
	}

}
