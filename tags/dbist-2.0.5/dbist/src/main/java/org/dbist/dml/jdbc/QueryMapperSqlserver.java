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

import org.dbist.dml.Lock;

/**
 * @author Steve M. Jung
 * @since 2013. 9. 7. (version 2.0.3)
 */
public class QueryMapperSqlserver extends AbstractQueryMapper {

	public String getDbType() {
		return "sqlserver";
	}

	public boolean isSupportedPaginationQuery() {
		return false;
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

		String lowerSql = sql.toLowerCase();
		int selectIndex = lowerSql.indexOf("select");
		int distinctIndex = lowerSql.indexOf("distinct");
		int topIndex = distinctIndex > 0 && distinctIndex < selectIndex + 13 ? distinctIndex + 8 : selectIndex + 6;
		int top = (pageIndex + 1) * pageSize + firstResultIndex;
		return new StringBuffer(sql).insert(topIndex, " top " + top).toString();
	}

	public String toWithLock(Lock lock) {
		return "with (updlock, rowlock)";
	}

	@Override
	public String toForUpdate(Lock lock) {
		return "";
	}

	public String getFunctionLowerCase() {
		return "lower";
	}

	public String getQueryCountTable() {
		return "select count(*) from information_schema.tables where lower(table_catalog) = '${domain}' and lower(table_name) = ?";
	}

	public String getQueryPkColumnNames() {
		return "select lower(col.column_name) name from information_schema.table_constraints tbl, information_schema.constraint_column_usage col, information_schema.columns cols"
				+ " where lower(tbl.table_catalog) = '${domain}' and lower(tbl.table_name) = ? and tbl.constraint_type = 'PRIMARY KEY'"
				+ " and col.constraint_name = tbl.constraint_name and col.table_name = tbl.table_name and col.table_catalog = tbl.table_catalog and col.table_schema = tbl.table_schema"
				+ " and col.table_name = cols.table_name and col.table_catalog = cols.table_catalog and col.table_schema = cols.table_schema and col.column_name = cols.column_name"
				+ " order by cols.ordinal_position";
	}

	public String getQueryColumnNames() {
		return "select lower(column_name) name, data_type dataType from information_schema.columns where lower(table_catalog) = '${domain}' and lower(table_name) = ? order by ordinal_position";
	}

	public String getQueryColumnName() {
		return "select lower(column_name) name, data_type dataType from information_schema.columns where lower(table_catalog) = '${domain}' and lower(table_name) = ? and lower(column_name) = ?";
	}

	public String getQueryCountIdentity() {
		return "";
	}

	public String getQueryCountSequence() {
		return "";
	}

	public char getReservedWordEscapingBraceOpen() {
		return '[';
	}

	public char getReservedWordEscapingBraceClose() {
		return ']';
	}

}
