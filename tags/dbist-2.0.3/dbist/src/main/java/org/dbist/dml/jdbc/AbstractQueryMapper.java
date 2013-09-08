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

import org.dbist.dml.Lock;
import org.dbist.metadata.Sequence;

/**
 * @author Steve M. Jung
 * @since 2013. 9. 7. (version 2.0.3)
 */
public abstract class AbstractQueryMapper implements QueryMapper {
	public String toNextval(Sequence sequence) {
		if (sequence.getName() == null || sequence.isAutoIncrement())
			return null;
		return sequence.getDomain() + "." + sequence.getName() + ".nextval";
	}

	public String toEscapement(char escape) {
		return "escape '" + escape + "'";
	}

	public String toWithLock(Lock lock) {
		return null;
	}

	public String toForUpdate(Lock lock) {
		StringBuffer buf = new StringBuffer();
		buf.append("for update");

		if (!isSupportedLockTimeout())
			return buf.toString();

		Integer timeout = lock.getTimeout();
		if (timeout == null || timeout < 0)
			return buf.toString();

		timeout /= 1000;
		if (timeout == 0)
			buf.append(" nowait");
		else
			buf.append(" wait " + timeout);
		return buf.toString();
	}

}
