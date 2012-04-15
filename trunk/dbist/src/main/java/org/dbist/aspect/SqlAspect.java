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
 * Functions as an aspect(AOP) related to SQL.<br>
 * Currently, it works as a printer of SQL logs.
 * 
 * <p>
 * Examples
 * 
 * <pre>
 * 1. Springframework Configuration
 * 	&lt;aop:config&gt;
 * 		&lt;aop:aspect order=&quot;2&quot; ref=&quot;sqlAspect&quot;&gt;
 * 			&lt;aop:around method=&quot;print&quot; pointcut=&quot;execution(* org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate.*(..))&quot; /&gt;
 * 		&lt;/aop:aspect&gt;
 * 	&lt;/aop:config&gt;
 * 	&lt;bean id=&quot;sqlAspect&quot; class=&quot;org.dbist.aspect.SqlAspect&quot;&gt;
 * 		&lt;property name=&quot;enabled&quot; value=&quot;true&quot; /&gt;
 * 		&lt;property name=&quot;prettyPrint&quot; value=&quot;true&quot; /&gt;
 * 	&lt;/bean&gt;
 * 
 * 2. Logs
 * SQL: 
 *     select
 *         owner,
 *         name 
 *     from
 *         dbist.blog 
 *     where
 *         name <> :name1 
 *         and lower(description) like :description2 
 *         and created_at is not null 
 *         and owner in (
 *             :owner4
 *         ) 
 *     group by
 *         owner,
 *         name 
 *     order by
 *         name asc
 * Parameters:
 * 	name1: test
 * 	description2: %the%
 * 	owner4: [steve, myjung, junguitar]
 * </pre>
 * 
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
		try {
			buf.append(prettyPrint ? formatter.format(sql) : sql);
		} catch (Exception e) {
			buf.append(sql);
		}
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
