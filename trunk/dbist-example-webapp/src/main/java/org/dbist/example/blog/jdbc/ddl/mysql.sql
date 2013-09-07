CREATE TABLE `users` (
	`username`   varchar(100) NOT NULL,
	`pwd`        varchar(100),
	`first_name` varchar(100),
	`last_name`  varchar(100),
	`email`      varchar(100),
	PRIMARY KEY (`username`)
)

CREATE TABLE `blog` (
	`id`          varchar(100) NOT NULL,
	`name`        varchar(100),
	`description` varchar(200),
	`owner`       varchar(100),
	`created_at`  date,
	`updated_at`  date,
	PRIMARY KEY (`id`)
)

CREATE TABLE `post` (
	`id`         varchar(100) NOT NULL,
	`blog_id`    varchar(100),
	`title`      varchar(200),
	`author`     varchar(100),
	`created_at` date,
	`updated_at` date,
	`content`    text,
	PRIMARY KEY (`id`)
)

CREATE TABLE `comments` (
	`id`         varchar(100) NOT NULL,
	`post_id`    varchar(100),
	`author`     varchar(100),
	`created_at` date,
	`content`    varchar(1000),
	PRIMARY KEY (`id`)
)