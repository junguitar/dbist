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
public class QueryMapperDb2 extends AbstractQueryMapper {

	public String getDbType() {
		return "db2";
	}

	public boolean isSupportedPaginationQuery() {
		return true;
	}

	public boolean isSupportedLockTimeout() {
		return true;
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
		if (offset > 0 && limit > 0) {
			buf.append("select * from (select pagetbl_.*, rownumber() over(order by order of pagetbl_) rownumber_ from (")
					.append(sql)
					.append(" fetch first " + (offset + limit) + " rows only) pagetbl_) pagetbl__ where rownumber_ > " + offset
							+ " order by rownumber_");
		} else if (limit > 0) {
			buf.append(sql);
			buf.append(" fetch first " + limit + " rows only");
		}

		if (subsql != null)
			buf.append(subsql);
		return buf.toString();
	}

	public String getFunctionLowerCase() {
		return "lcase";
	}

	public String getQueryCountTable() {
		return "select count(*) from sysibm.systables where lcase(creator) = '${domain}' and type = 'T' and lcase(name) = ?";
	}

	public String getQueryPkColumnNames() {
		return "select lcase(name) name from sysibm.syscolumns"
				+ " where lcase(tbcreator) = '${domain}' and lcase(tbname) = ? and keyseq is not null order by keyseq";
	}

	public String getQueryColumnNames() {
		return "select lcase(name) name, lcase(typename) dataType from sysibm.syscolumns where lcase(tbcreator) = '${domain}' and lcase(tbname) = ? order by colno";
	}

	public String getQueryColumnName() {
		return "select lcase(name) name, lcase(typename) dataType from sysibm.syscolumns where lcase(tbcreator) = '${domain}' and lcase(tbname) = ? and lcase(name) = ?";
	}

	public String getQueryCountIdentity() {
		return "select count(*) from sysibm.syscolumns where lcase(tbcreator) = '${domain}' and lcase(tbname) = ? and lcase(name) = ? and identity = 'Y'";
	}

	public String getQueryCountSequence() {
		return "select count(*) from sysibm.syssequences where lcase(seqschema) = '${domain}' and lcase(seqname) = ?";
	}

}
