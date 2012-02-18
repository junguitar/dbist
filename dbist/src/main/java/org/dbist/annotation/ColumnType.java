package org.dbist.annotation;

import org.dbist.metadata.Column;

public enum ColumnType {
	/**
	 * 
	 */
	EMPTY(""),
	/**
	 * 
	 */
	TITLE(Column.TYPE_TITLE),
	/**
	 * 
	 */
	LISTED(Column.TYPE_LISTED),
	/**
	 * 
	 */
	TEXT(Column.TYPE_TEXT);

	private final String value;
	ColumnType(String value) {
		this.value = value;
	}
	public String value() {
		return this.value;
	}
}
