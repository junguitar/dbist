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

import org.dbist.exception.DbistRuntimeException;

/**
 * @author Steve M. Jung
 * @since 2011. 7. 10 (version 0.0.1)
 */
public class Table {
	public static final String DBTYPE_MYSQL = "mysql";
	public static final String DBTYPE_ORACLE = "oracle";
	public static final String DBTYPE_SQLSERVER = "sqlserver";
	public static final String DBTYPE_DB2 = "db2";

	private String dbType;
	private String domain;
	private String name;
	private Class<?> clazz;
	private boolean containsLinkedTable;
	private List<String> pkColumnNameList;
	private String[] pkFieldNames;
	private List<String> titleColumnNameList;
	private List<String> listedColumnNameList;
	private List<Column> columnList = new ArrayList<Column>();
	private String insertSql;
	private String updateSql;
	private String deleteSql;

	public String getDbType() {
		return dbType;
	}
	public void setDbType(String dbType) {
		this.dbType = dbType;
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
	public Class<?> getClazz() {
		return clazz;
	}
	public void setClazz(Class<?> clazz) {
		this.clazz = clazz;
	}
	public boolean containsLinkedTable() {
		return containsLinkedTable;
	}
	public void setContainsLinkedTable(boolean containsLinkedTable) {
		this.containsLinkedTable = containsLinkedTable;
	}
	public List<String> getPkColumnNameList() {
		return pkColumnNameList;
	}
	public void setPkColumnNameList(List<String> pkColumnName) {
		this.pkColumnNameList = pkColumnName;
	}
	public boolean isPkColmnName(String name) {
		return getPkColumnNameList().contains(name);
	}
	public String[] getPkFieldNames() {
		populate();
		return pkFieldNames;
	}
	public boolean isPkFieldName(String name) {
		String columnName = toColumnName(name);
		return columnName != null && isPkColmnName(columnName);
	}
	public List<String> getTitleColumnNameList() {
		populate();
		return titleColumnNameList;
	}
	public List<String> getListedColumnNameList() {
		populate();
		return listedColumnNameList;
	}
	private void populate() {
		if (this.titleColumnNameList != null)
			return;
		synchronized (this) {
			if (this.titleColumnNameList != null)
				return;
			List<String> titleColumnNameList = new ArrayList<String>(0);
			listedColumnNameList = new ArrayList<String>(0);
			String titleCandidate = null;
			for (Column column : this.columnList) {
				if (column.isTitle()) {
					titleColumnNameList.add(column.getName());
				} else if (column.isListed()) {
					listedColumnNameList.add(column.getName());
				} else if (!column.isPrimaryKey() && titleCandidate == null) {
					titleCandidate = column.getName();
				}
			}
			if (titleColumnNameList.isEmpty() && titleCandidate != null)
				titleColumnNameList.add(titleCandidate);
			List<String> pkFieldNameList = new ArrayList<String>();
			for (String columnName : pkColumnNameList)
				pkFieldNameList.add(toFieldName(columnName));
			pkFieldNames = pkFieldNameList.toArray(new String[pkFieldNameList.size()]);
			this.titleColumnNameList = titleColumnNameList;
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

	public String getInsertSql(String... fieldNames) {
		// Insert all fields
		if (ValueUtils.isEmpty(fieldNames)) {
			if (insertSql != null)
				return insertSql;
			synchronized (this) {
				if (insertSql != null)
					return insertSql;
				StringBuffer buf = new StringBuffer("insert into ").append(getDomain()).append(".").append(getName()).append("(");
				StringBuffer valuesBuf = new StringBuffer(" values(");
				int i = 0;
				for (Column column : getColumnList()) {
					if (column.getRelation() != null || (column.getSequence() != null && column.getSequence().isAutoIncrement()))
						continue;
					buf.append(i == 0 ? "" : ", ").append(column.getName());
					if (column.getSequence() == null || ValueUtils.isEmpty(column.getSequence().getName()))
						valuesBuf.append(i == 0 ? ":" : ", :").append(column.getField().getName());
					else
						valuesBuf.append(i == 0 ? "" : ", ").append(ValueUtils.toString(column.getSequence().getDomain(), getDomain())).append(".")
								.append(column.getSequence().getName()).append(".nextval");
					i++;
				}
				buf.append(")");
				valuesBuf.append(")");
				buf.append(valuesBuf);

				insertSql = buf.toString();
			}
			return insertSql;
		}

		// Insert some fields
		StringBuffer buf = new StringBuffer("insert into ").append(getDomain()).append(".").append(getName()).append("(");
		StringBuffer valuesBuf = new StringBuffer(" values(");
		int i = 0;
		List<String> pkFieldNameList = ValueUtils.toList(getPkFieldNames());
		for (String fieldName : pkFieldNameList) {
			Column column = getColumnByFieldName(fieldName);
			if (column.getRelation() != null || (column.getSequence() != null && column.getSequence().isAutoIncrement()))
				continue;
			buf.append(i == 0 ? "" : ", ").append(getColumnByFieldName(fieldName).getName());
			if (column.getSequence() == null || ValueUtils.isEmpty(column.getSequence().getName()))
				valuesBuf.append(i == 0 ? ":" : ", :").append(fieldName);
			else
				valuesBuf.append(i == 0 ? "" : ", ").append(ValueUtils.toString(column.getSequence().getDomain(), getDomain())).append(".")
						.append(column.getSequence().getName()).append(".nextval");
			i++;
		}
		for (String fieldName : fieldNames) {
			if (pkFieldNameList.contains(fieldName))
				continue;
			Column column = getColumnByFieldName(fieldName);
			if (column == null)
				throw new DbistRuntimeException("Invalid fieldName: " + getClazz().getName() + "." + fieldName);
			if (column.getRelation() != null || (column.getSequence() != null && column.getSequence().isAutoIncrement()))
				continue;
			buf.append(i == 0 ? "" : ", ").append(column.getName());
			if (column.getSequence() == null || ValueUtils.isEmpty(column.getSequence().getName()))
				valuesBuf.append(i == 0 ? ":" : ", :").append(fieldName);
			else
				valuesBuf.append(i == 0 ? "" : ", ").append(ValueUtils.toString(column.getSequence().getDomain(), getDomain())).append(".")
						.append(column.getSequence().getName()).append(".nextval");
			i++;
		}
		buf.append(")");
		valuesBuf.append(")");
		buf.append(valuesBuf);

		return buf.toString();
	}

	public String getUpdateSql(String... fieldNames) {
		// Update all fields
		if (ValueUtils.isEmpty(fieldNames)) {
			if (updateSql != null)
				return updateSql;
			synchronized (this) {
				if (updateSql != null)
					return updateSql;
				StringBuffer buf = new StringBuffer("update ").append(getDomain()).append(".").append(getName()).append(" set ");
				StringBuffer whereBuf = new StringBuffer();
				int i = 0;
				int j = 0;
				for (Column column : getColumnList()) {
					if (column.getRelation() != null)
						continue;
					if (column.isPrimaryKey()) {
						whereBuf.append(j++ == 0 ? " where " : " and ").append(column.getName()).append(" = ").append(":")
								.append(column.getField().getName());
						continue;
					}
					buf.append(i++ == 0 ? "" : ", ").append(column.getName()).append(" = :").append(column.getField().getName());
				}
				updateSql = buf.append(whereBuf).toString();
			}
			return updateSql;
		}

		// Update some fields
		StringBuffer buf = new StringBuffer("update ").append(getDomain()).append(".").append(getName()).append(" set ");
		int i = 0;
		int j = 0;
		for (String fieldName : fieldNames) {
			Column column = getColumnByFieldName(fieldName);
			if (column == null)
				throw new DbistRuntimeException("Invalid fieldName: " + getClazz().getName() + "." + fieldName);
			if (column.isPrimaryKey())
				throw new DbistRuntimeException("Updating primary key is not supported. " + getDomain() + "." + getName() + getPkColumnNameList());
			buf.append(i++ == 0 ? "" : ", ").append(toColumnName(fieldName)).append(" = :").append(fieldName);
		}
		StringBuffer whereBuf = new StringBuffer();
		for (String columnName : getPkColumnNameList())
			whereBuf.append(j++ == 0 ? " where " : " and ").append(columnName).append(" = ").append(":").append(toFieldName(columnName));
		return buf.append(whereBuf).toString();
	}

	public String getDeleteSql() {
		if (deleteSql != null)
			return deleteSql;
		synchronized (this) {
			if (deleteSql != null)
				return deleteSql;
			StringBuffer buf = new StringBuffer("delete from ").append(getDomain()).append(".").append(getName());
			int i = 0;
			for (String columnName : getPkColumnNameList())
				buf.append(i++ == 0 ? " where " : " and ").append(columnName).append(" = :").append(getColumn(columnName).getField().getName());
			deleteSql = buf.toString();
		}
		return deleteSql;
	}
}
