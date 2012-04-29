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

import java.lang.reflect.Method;
import java.util.Collection;
import java.util.Comparator;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import net.sf.common.util.ValueUtils;

import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.ClassUtils;
import org.springframework.util.StringUtils;

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
 * 		&lt;property name=&quot;combinedPrint&quot; value=&quot;false&quot; /&gt;
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
	private boolean combinedPrint;
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
	public boolean isCombinedPrint() {
		return combinedPrint;
	}
	public void setCombinedPrint(boolean combinedPrint) {
		this.combinedPrint = combinedPrint;
	}

	public Object print(final ProceedingJoinPoint point) throws Throwable {
		print(point.getArgs());

		return point.proceed();
	}

	//	private Formatter formatter = new BasicFormatterImpl();
	private static Object formatterInstance;
	private static Method formatMethod;
	static {
		Class<?> formatterClass = null;
		try {
			formatterClass = ClassUtils.forName("org.hibernate.engine.jdbc.internal.BasicFormatterImpl", null);
		} catch (ClassNotFoundException e) {
			try {
				formatterClass = ClassUtils.forName("org.hibernate.jdbc.util.BasicFormatterImpl", null);
			} catch (ClassNotFoundException e1) {
				logger.warn(e.getMessage(), e);
			} catch (LinkageError e1) {
				logger.warn(e.getMessage(), e);
			}
		} catch (LinkageError e) {
			logger.warn(e.getMessage(), e);
		}

		if (formatterClass != null) {
			try {
				formatterInstance = formatterClass.newInstance();
				try {
					formatMethod = formatterClass.getMethod("format", String.class);
				} catch (SecurityException e) {
					logger.warn(e.getMessage(), e);
				} catch (NoSuchMethodException e) {
					logger.warn(e.getMessage(), e);
				}
			} catch (InstantiationException e) {
				logger.warn(e.getMessage(), e);
			} catch (IllegalAccessException e) {
				logger.warn(e.getMessage(), e);
			}
		}

	}

	@SuppressWarnings("unchecked")
	private static final Comparator<String> COMPARATOR_REVERSED = ComparatorUtils.reversedComparator(ComparatorUtils.naturalComparator());
	private void print(Object[] args) {
		if (!enabled || formatMethod == null || !logger.isInfoEnabled() || ValueUtils.isEmpty(args) || !(args[0] instanceof String))
			return;
		String sql = (String) args[0];
		Object params = args.length > 1 && !ValueUtils.isEmpty(args[1]) ? args[1] : null;

		// SQL
		StringBuffer buf = new StringBuffer("\r\nSQL: ");
		boolean combined = false;
		if (prettyPrint) {
			try {
				sql = (String) formatMethod.invoke(formatterInstance, sql);
			} catch (Exception e) {
			}
		}
		if (combinedPrint) {
			try {
				sql = combine(sql, params);
				combined = true;
			} catch (Exception e) {
			}
		}
		buf.append(sql);

		// Parameters
		if (!combined && params != null) {
			try {
				if (params instanceof Map) {
					@SuppressWarnings("unchecked")
					Map<String, ?> map = (Map<String, ?>) params;
					buf.append("\r\nParameters:");
					for (String key : map.keySet())
						buf.append("\r\n\t").append(key).append(": ").append(ValueUtils.toString(map.get(key)));
				} else if (params instanceof Object[]) {
					buf.append("\r\nParameters:");
					int i = 0;
					for (Object param : (Object[]) params)
						buf.append(i++ == 0 ? "" : ",").append("\r\n\t").append(ValueUtils.toString(param));
				}
			} catch (Exception e) {
				logger.warn(e.getMessage(), e);
			}
		}

		buf.append("\r\n");
		logger.info(buf.toString());
	}

	private static String combine(String sql, Object params) {
		if (sql == null || params == null)
			return sql;
		if (params instanceof Map) {
			@SuppressWarnings("unchecked")
			Map<String, ?> map = (Map<String, ?>) params;
			Set<String> keySet = new TreeSet<String>(COMPARATOR_REVERSED);
			keySet.addAll(map.keySet());
			for (String key : keySet)
				sql = StringUtils.replace(sql, ":" + key, toParamValue(map.get(key)));
		} else if (params instanceof Object[]) {
			for (Object param : (Object[]) params)
				sql = sql.replaceFirst("?", toParamValue(param));
		}
		return sql;
	}

	private static String toParamValue(Object value) {
		if (value == null)
			return "null";
		if (value instanceof String)
			return "'" + StringEscapeUtils.escapeSql((String) value) + "'";
		if (value instanceof Date)
			return "'" + ValueUtils.toDateString((Date) value, ValueUtils.DATEPATTERN_DATETIME) + "'";
		if (value instanceof Collection) {
			StringBuffer buf = new StringBuffer();
			int i = 0;
			for (Object item : (Collection<?>) value)
				buf.append(i++ == 0 ? "" : ",").append(toParamValue(item));
			return buf.toString();
		}
		return ValueUtils.toString(value);
	}
}
