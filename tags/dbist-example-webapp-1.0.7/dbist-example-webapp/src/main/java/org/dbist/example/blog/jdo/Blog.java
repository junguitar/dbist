package org.dbist.example.blog.jdo;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable
public class Blog {
	@PrimaryKey
	@Persistent
	@Column(length = 100)
	private String id;
	@Persistent
	@Column(length = 100)
	private String name;
	@Persistent
	@Column(length = 200)
	private String description;
	@Persistent
	@Column(length = 100)
	private String owner;
	@Persistent(column = "created_at")
	private Date createdAt;
	@Persistent(column = "updated_at")
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
