package org.dbist.dml;

import java.util.Date;

import org.dbist.annotation.Column;
import org.dbist.annotation.ColumnType;

public class Blog {
	private String id;
	private String name;
	@Column(type = ColumnType.TEXT)
	private String description;
	@Column(type = ColumnType.LISTED)
	private String owner;
	@Column(type = ColumnType.LISTED)
	private Date createdAt;
	@Column(type = ColumnType.LISTED)
	private Date updatedAt;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public String getDescription() {
		return description;
	}
	public void setDescription(String description) {
		this.description = description;
	}
	public String getOwner() {
		return owner;
	}
	public void setOwner(String owner) {
		this.owner = owner;
	}
	public Date getCreatedAt() {
		return createdAt;
	}
	public void setCreatedAt(Date createdAt) {
		this.createdAt = createdAt;
	}
	public Date getUpdatedAt() {
		return updatedAt;
	}
	public void setUpdatedAt(Date updatedAt) {
		this.updatedAt = updatedAt;
	}
}
