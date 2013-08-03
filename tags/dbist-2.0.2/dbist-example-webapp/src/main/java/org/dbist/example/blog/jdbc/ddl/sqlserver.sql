CREATE TABLE [USERS] (
	[username]   [varchar](100) NOT NULL,
	[pwd]        [varchar](100),
	[first_name] [varchar](100),
	[last_name]  [varchar](100),
	[email]      [varchar](100),
	PRIMARY KEY ([username])
)

CREATE TABLE [BLOG] (
	[id]          [varchar](100) NOT NULL,
	[name]        [varchar](100),
	[description] [varchar](200),
	[owner]       [varchar](100),
	[created_at]  [datetime],
	[updated_at]  [datetime],
	PRIMARY KEY ([id])
)

CREATE TABLE [POST] (
	[id]         [varchar](100) NOT NULL,
	[blog_id]    [varchar](100),
	[title]      [varchar](200),
	[author]     [varchar](100),
	[created_at] [datetime],
	[updated_at] [datetime],
	[content]    [text],
	PRIMARY KEY ([id])
)

CREATE TABLE [COMMENTS] (
	[id]         [varchar](100) NOT NULL,
	[post_id]    [varchar](100),
	[author]     [varchar](100),
	[created_at] [datetime],
	[content]    [varchar](1000),
	PRIMARY KEY ([id])
)