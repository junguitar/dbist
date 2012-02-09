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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Steve M. Jung
 * @since 2011. 7. 10 (version 0.0.1)
 */
public class Table {
	private String domain;
	private String name;
	private List<Field> pkField = new ArrayList<Field>(1);
	private List<Field> field = new ArrayList<Field>();
	private List<Column> pkColumn = new ArrayList<Column>(1);
	private List<Column> column = new ArrayList<Column>();
	private Map<String, String> fieldNameColumNameMap = new HashMap<String, String>();
	private Map<String, String> columnNameFieldNameMap = new HashMap<String, String>();

	public String toColumnName(String name) {
		// TODO String toColumnName(String name)
		return name;
	}

	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public List<Field> getPkField() {
		return pkField;
	}
	public List<Field> getField() {
		return field;
	}
	public Field addField(Field field) {
		if (this.field == null)
			this.field = new ArrayList<Field>();
		this.field.add(field);
		return field;
	}
	public List<Column> getPkColumn() {
		return pkColumn;
	}
	public List<Column> getColumn() {
		return column;
	}
	public Column addColumn(Column column) {
		this.column.add(column);
		return column;
	}
}
