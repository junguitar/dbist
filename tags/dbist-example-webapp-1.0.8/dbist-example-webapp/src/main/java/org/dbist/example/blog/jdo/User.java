package org.dbist.example.blog.jdo;

import javax.jdo.annotations.Column;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(table = "users")
public class User {
	@PrimaryKey
	@Persistent
	@Column(length = 100)
	private String username;
	@Persistent(column = "pwd")
	@Column(length = 100)
	private String password;
	@Persistent(column = "first_name")
	@Column(length = 100)
	private String firstName;
	@Persistent(column = "last_name")
	@Column(length = 100)
	private String lastName;
	@Persistent
	@Column(length = 100)
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
