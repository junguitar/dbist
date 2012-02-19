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

import net.sf.common.util.ValueUtils;

/**
 * @author Steve M. Jung
 * @since 2011. 7. 10 (version 0.0.1)
 */
public class Table {
	private String domain;
	private String name;
	private Class<?> clazz;
	private List<String> pkColumnNameList;
	private String[] pkFieldNames;
	private List<String> titleColumnNameList;
	private List<String> listedColumnNameList;
	private List<Column> columnList = new ArrayList<Column>();

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
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public List<String> getPkColumnNameList() {
		return pkColumnNameList;
	}
	public void setPkColumnNameList(List<String> pkColumnName) {
		this.pkColumnNameList = pkColumnName;
	}
	public String[] getPkFieldNames() {
		populateColumnName();
		return pkFieldNames;
	}
	public boolean isPkColmnName(String name) {
		return getPkColumnNameList().contains(name);
	}
	public List<String> getTitleColumnNameList() {
		populateColumnName();
		return titleColumnNameList;
	}
	public List<String> getListedColumnNameList() {
		populateColumnName();
		return listedColumnNameList;
	}
	private void populateColumnName() {
		if (this.titleColumnNameList != null)
			return;
		synchronized (this) {
			if (this.titleColumnNameList != null)
				return;
			List<String> titleColumnName = new ArrayList<String>(1);
			listedColumnNameList = new ArrayList<String>(1);
			String titleCandidate = null;
			for (Column column : this.columnList) {
				if (column.isTitle()) {
					titleColumnName.add(column.getName());
				} else if (column.isListed()) {
					listedColumnNameList.add(column.getName());
				} else if (!column.isPrimaryKey() && titleCandidate == null) {
					titleCandidate = column.getName();
				}
			}
			if (titleColumnName.isEmpty() && titleCandidate != null)
				titleColumnName.add(titleCandidate);
			List<String> pkFieldNameList = new ArrayList<String>();
			for (String columnName : pkColumnNameList)
				pkFieldNameList.add(toFieldName(columnName));
			pkFieldNames = pkFieldNameList.toArray(new String[pkFieldNameList.size()]);
			this.titleColumnNameList = titleColumnName;
		}
	}
	public List<Column> getColumnList() {
		return columnList;
	}
	public Column addColumn(Column column) {
		this.columnList.add(column);
		return column;
	}
	private Map<String, Column> columnMap;
	public Column getColumn(String name) {
		ValueUtils.assertNotNull("name", name);
		if (this.columnMap == null) {
			synchronized (this) {
				if (this.columnMap == null) {
					Map<String, Column> columnMap = new HashMap<String, Column>(this.columnList.size());
					for (Column column : this.columnList)
						columnMap.put(column.getName(), column);
					this.columnMap = columnMap;
				}
			}
		}
		return columnMap.get(name.toLowerCase());
	}
	public Column getColumnByFieldName(String fieldName) {
		String columnName = toColumnName(fieldName);
		return columnName == null ? null : getColumn(columnName);
	}
	public Field getField(String name) {
		Column column = getColumnByFieldName(name);
		return column == null ? null : column.getField();
	}
	public Field getFieldByColumnName(String columnName) {
		Column column = getColumn(columnName);
		return column == null ? null : column.getField();
	}
	private Map<String, String> fieldNameColumNameMap;
	public String toColumnName(String fieldName) {
		if (this.fieldNameColumNameMap == null) {
			synchronized (this) {
				if (this.fieldNameColumNameMap == null) {
					Map<String, String> fieldNameColumnNameMap = new HashMap<String, String>(this.columnList.size());
					for (Column column : this.columnList)
						fieldNameColumnNameMap.put(column.getField().getName(), column.getName());
					this.fieldNameColumNameMap = fieldNameColumnNameMap;
				}
			}
		}
		return fieldNameColumNameMap.get(fieldName);
	}
	public String toFieldName(String columnName) {
		Column column = getColumn(columnName);
		return column == null ? null : column.getField().getName();
	}
}
