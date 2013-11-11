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
package org.dbist.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Is used to specify a mapped table for a class.<br>
 * If no Table annotation is specified, the default values are applied.
 * 
 * <p>
 * Examples
 * 
 * <pre>
 * &#064;Table(name = &quot;comments&quot;)
 * public class Comment {
 * ...
 * }
 * 
 * &#064;Table(name = &quot;users&quot;)
 * public class User {
 * ...
 * }
 * </pre>
 * 
 * @author Steve M. Jung
 * @since 2012. 1. 5. (version 0.0.1)
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	/**
	 * 
	 */
	String domain() default "";
	/**
	 * (Optional) The name of the table.
	 * <p>
	 * The default value is applied to the following rules and order.<br>
	 * 1. the underscore case name of the class<br>
	 * 2. the name of the class
	 */
	String name() default "";
	boolean reservedWordTolerated() default false;
}
