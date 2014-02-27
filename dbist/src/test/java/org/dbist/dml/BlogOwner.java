package org.dbist.dml;

import org.dbist.annotation.PrimaryKey;

public class BlogOwner {
	@PrimaryKey
	private String id;
	private String owner;
	private String name;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
}
