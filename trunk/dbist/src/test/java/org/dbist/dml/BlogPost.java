package org.dbist.dml;

import java.util.Date;

import org.dbist.annotation.Column;
import org.dbist.annotation.ColumnType;
import org.dbist.annotation.PrimaryKey;
import org.dbist.annotation.Relation;
import org.dbist.annotation.Table;

public class BlogPost {
	@PrimaryKey
	private String id;
	private String blogId;
	private String blogName;
	@Column(type = ColumnType.TITLE)
	private String title;
	@Column(type = ColumnType.LISTED)
	private String author;
	@Relation(field = "author")
	private User authorName;
	@Column(type = ColumnType.LISTED)
	private Date createdAt;
	@Column(type = ColumnType.LISTED)
	private Date updatedAt;
	@Column(type = ColumnType.TEXT)
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
	public String getBlogName() {
		return blogName;
	}
	public void setBlogName(String blogName) {
		this.blogName = blogName;
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
	public User getAuthorName() {
		return authorName;
	}
	public void setAuthorName(User authorData) {
		this.authorName = authorData;
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

	public static class Blog {
		private String id;
		@Column(name = "name")
		private String blogName;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
		public String getBlogName() {
			return blogName;
		}
		public void setBlogName(String name) {
			this.blogName = name;
		}
	}

	@Table(name = "users")
	public static class User {
		private String firstName;
		private String lastName;
		public String getFirstName() {
			return firstName;
		}
		public void setFirstName(String firstName) {
			this.firstName = firstName;
		}
		public String getLastName() {
			return lastName;
		}
		public void setLastName(String lastName) {
			this.lastName = lastName;
		}
	}
}
