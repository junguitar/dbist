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
package org.dbist.metadata;

import java.lang.reflect.Field;

/**
 * @author Steve M. Jung
 * @since 2011. 7. 10 (version 0.0.1)
 */
public class Column {
	public static final String TYPE_PRIMARYKEY = "pk";
	public static final String TYPE_TITLE = "title";
	public static final String TYPE_CREATED = "created";
	public static final String TYPE_MODIFIED = "modified";

	private String name;
	private Field field;
	String type;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Field getField() {
		return field;
	}
	public void setField(Field field) {
		this.field = field;
	}
	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public boolean isPrimaryKey() {
		return TYPE_PRIMARYKEY.equals(type);
	}
	public boolean isTitle() {
		return TYPE_TITLE.equals(type);
	}
	public boolean isCreated() {
		return TYPE_CREATED.equals(type);
	}
	public boolean isModified() {
		return TYPE_MODIFIED.equals(type);
	}
}
