CREATE TABLE users (
    username character varying(100) NOT NULL,
    pwd character varying(100),
    first_name character varying(100),
    last_name character varying(100),
    email character varying(100)
);
ALTER TABLE ONLY users
    ADD CONSTRAINT users_pkey PRIMARY KEY (username);

CREATE TABLE blog (
    id character varying(100) NOT NULL,
    name character varying(100),
    description character varying(200),
    owner character varying(100),
    created_at date,
    updated_at date
);
ALTER TABLE ONLY blog
    ADD CONSTRAINT blog_pkey PRIMARY KEY (id);

CREATE TABLE post (
    id character varying(100) NOT NULL,
    blog_id character varying(100),
    title character varying(200),
    author character varying(100),
    created_at date,
    updated_at date,
    content text
);
ALTER TABLE ONLY post
    ADD CONSTRAINT post_pkey PRIMARY KEY (id);

CREATE TABLE comments (
    id character varying(100) NOT NULL,
    post_id character varying(100),
    author character varying(100),
    created_at date,
    content character varying(1000)
);
ALTER TABLE ONLY comments
    ADD CONSTRAINT comments_pkey PRIMARY KEY (id);