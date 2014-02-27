CREATE TABLE "LOG" (
	"ID"		number(18, 0) NOT NULL,
	"TEXT"		varchar(1000),
	PRIMARY KEY ("ID")
);
CREATE SEQUENCE "SQ_LOG";

CREATE VIEW BLOG_OWNER (
	"ID",
	"OWNER",
	"NAME"
) AS 
	select blog.id, blog.owner, users.first_name || ' ' || users.last_name name
	from blog, users
	where blog.owner = users.username;

CREATE VIEW BLOG_POST (
	"ID",
	"BLOG_ID",
	"BLOG_NAME",
	"TITLE",
	"AUTHOR",
	"CREATED_AT",
	"UPDATED_AT",
	"CONTENT"
) AS 
	select post.id, post.blog_id, blog.name blog_name, post.title, post.author, post.created_at, post.updated_at, post.content
	from blog, post
	where post.blog_id = blog.id;