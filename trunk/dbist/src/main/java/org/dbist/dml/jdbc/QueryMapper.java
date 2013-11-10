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
import java.util.Set;

import org.dbist.dml.Lock;
import org.dbist.metadata.Sequence;

/**
 * @author Steve M. Jung
 * @since 2013. 9. 7. (version 2.0.3)
 */
public interface QueryMapper {
	String getDbType();
	boolean isSupportedPaginationQuery();
	boolean isSupportedLockTimeout();
	String applyPagination(String sql, Map<String, ?> paramMap, int pageIndex, int pageSize, int firstResultIndex, int maxResultSize);
	String toNextval(Sequence sequence);
	String toEscapement(char escape);
	String toWithLock(Lock lock);
	String toForUpdate(Lock lock);
	String getFunctionLowerCase();
	String getQueryCountTable();
	String getQueryPkColumnNames();
	String getQueryColumnNames();
	String getQueryColumnName();
	String getQueryCountIdentity();
	String getQueryCountSequence();
	Set<String> getReservedWords();
	char getReservedWordEscapingBraceOpen();
	char getReservedWordEscapingBraceClose();
}
