package org.dbist.example.blog.jdo;

import java.util.Date;

import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(table = "POST")
public class Post {
	@PrimaryKey
	@Persistent(column = "ID")
	private String id;
	@Persistent(column = "BLOG_ID")
	private String blogId;
	@Persistent(column = "AUTHOR")
	private String author;
	@Persistent(column = "CREATED_AT")
	private Date createdAt;
	@Persistent(column = "UPDATED_AT")
	private Date updatedAt;
	@Persistent(column = "CONTENT")
	private String content;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getBlogId() {
		return blogId;
	}
	public void setBlogId(String blogId) {
		this.blogId = blogId;
	}
	public String getAuthor() {
		return author;
	}
	public void setAuthor(String author) {
		this.author = author;
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
