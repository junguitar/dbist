/**
 * Copyright 2011-2012 the original author or authors.
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
package org.dbist.aspect;

import java.util.Map;

import net.sf.common.util.ValueUtils;

import org.aspectj.lang.ProceedingJoinPoint;
import org.hibernate.jdbc.util.BasicFormatterImpl;
import org.hibernate.jdbc.util.Formatter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Steve M. Jung
 * @since 2012. 2. 23. (version 1.0.0)
 */
public class SqlAspect {
	private static final Logger logger = LoggerFactory.getLogger(SqlAspect.class);

	private boolean enabled = true;
	private boolean prettyPrint;
	public boolean isEnabled() {
		return enabled;
	}
	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}
	public boolean isPrettyPrint() {
		return prettyPrint;
	}
	public void setPrettyPrint(boolean prettyPrint) {
		this.prettyPrint = prettyPrint;
	}

	private Formatter formatter = new BasicFormatterImpl();
	public Object print(final ProceedingJoinPoint point) throws Throwable {
		print(point.getArgs());

		return point.proceed();
	}
	private void print(Object[] args) {
		if (!enabled || !logger.isInfoEnabled() || ValueUtils.isEmpty(args) || !(args[0] instanceof String))
			return;
		StringBuffer buf = new StringBuffer("\r\nSQL: ");
		String sql = (String) args[0];
		buf.append(prettyPrint ? formatter.format(sql) : sql);
		if (args.length > 1 && !ValueUtils.isEmpty(args[1])) {
			if (args[1] instanceof Map) {
				@SuppressWarnings("unchecked")
				Map<String, ?> map = (Map<String, ?>) args[1];
				buf.append("\r\nParameters:");
				for (String key : map.keySet())
					buf.append("\r\n\t").append(key).append(": ").append(ValueUtils.toString(map.get(key)));
			} else if (args[1] instanceof Object[]) {
				buf.append("\r\nParameters:");
				int i = 0;
				for (Object param : (Object[]) args[1])
					buf.append(i++ == 0 ? "" : ",").append("\r\n\t").append(ValueUtils.toString(param));
			}
		}
		buf.append("\r\n");
		logger.info(buf.toString());
	}
}
