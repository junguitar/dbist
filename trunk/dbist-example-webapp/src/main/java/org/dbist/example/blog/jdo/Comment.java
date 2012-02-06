package org.dbist.example.blog.jdo;

import java.util.Date;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(table = "comments")
public class Comment {
	@PrimaryKey
	@Persistent
	@Column(length = 100)
	private String id;
	@Persistent(column = "post_id")
	@Column(length = 100)
	private String postId;
	@Persistent
	@Column(length = 100)
	private String author;
	@Persistent(column = "created_at")
	private Date createdAt;
	@Persistent
	@Column(length = 1000)
	private String content;
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getPostId() {
		return postId;
	}
	public void setPostId(String postId) {
		this.postId = postId;
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
	public String getContent() {
		return content;
	}
	public void setContent(String content) {
		this.content = content;
	}
}
