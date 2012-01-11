package org.dbist.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Column {
	/**
	 * (Optional) The name of the column.<br />
	 * Defaults to the following cases.<br />
	 * 1. the underscore case name of the field<br />
	 * 2. the name of the field
	 */
	String name() default "";
	boolean skip() default false;
}
