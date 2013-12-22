package org.dbist.dml;

import org.dbist.annotation.Table;

@Table(reservedWordTolerated = true)
public class Asc {
	private String id;
	private String accessible;
	private String add;
	private String all;
	private String alter;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getAccessible() {
		return accessible;
	}
	public void setAccessible(String accessible) {
		this.accessible = accessible;
	}
	public String getAdd() {
		return add;
	}
	public void setAdd(String add) {
		this.add = add;
	}
	public String getAll() {
		return all;
	}
	public void setAll(String all) {
		this.all = all;
	}
	public String getAlter() {
		return alter;
	}
	public void setAlter(String alter) {
		this.alter = alter;
	}
}
