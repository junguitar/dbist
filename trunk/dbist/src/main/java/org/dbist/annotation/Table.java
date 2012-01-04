package org.dbist.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Table {
	/**
	 * (Optional) The name of the table.<br />
	 * Defaults to the following cases.<br />
	 * 1. the underscore case name of the class<br />
	 * 2. the name of the class
	 */
	String name() default "";
}
