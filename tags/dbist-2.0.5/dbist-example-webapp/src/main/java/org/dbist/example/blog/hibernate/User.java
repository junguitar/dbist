package org.dbist.example.blog.hibernate;

import org.dbist.annotation.Column;
import org.dbist.annotation.ColumnType;

public class User {
	private String username;
	private String password;
	@Column(type = ColumnType.TITLE)
	private String firstName;
	@Column(type = ColumnType.TITLE)
	private String lastName;
	private String email;
	public String getUsername() {
		return username;
	}
	public void setUsername(String username) {
		this.username = username;
	}
	public String getPassword() {
		return password;
	}
	public void setPassword(String password) {
		this.password = password;
	}
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
	public String getEmail() {
		return email;
	}
	public void setEmail(String email) {
		this.email = email;
	}
}
