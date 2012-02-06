package org.dbist.example.blog.jdo;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(table = "POST")
public class Post {
	@PrimaryKey
	@Persistent
	@Column(length = 100)
	private String id;
	@Persistent(column = "blog_id")
	@Column(length = 100)
	private String blogId;
	@Persistent
	@Column(length = 200)
	private String title;
	@Persistent
	@Column(length = 100)
	private String author;
	@Persistent(column = "created_at")
	private Date createdAt;
	@Persistent(column = "created_at")
	private Date updatedAt;
	@Persistent
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
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
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
