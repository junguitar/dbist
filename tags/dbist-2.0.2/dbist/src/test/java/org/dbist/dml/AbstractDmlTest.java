/**
 * Copyright 2011 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.dbist.dml;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.common.util.ValueUtils;

import org.dbist.exception.DbistRuntimeException;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author Steve Jung
 * @since 2012. 3. 6.
 */
public abstract class AbstractDmlTest {
	private int i = 0;
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected static Dml dml;

	@Before
	public void beforeTest() throws Exception {
		if (dml == null)
			dml = getDml();

		try {
			Blog blog = dml.select(Blog.class, "1");
			if (blog == null) {
				blog = new Blog();
				blog.setId("1");
				blog.setName("1");
				blog.setOwner("junguitar@gmail.com");
				blog.setDescription("the 1");
				dml.insert(blog);
			}
			if (dml.select(Blog.class, "2") == null) {
				blog.setId("2");
				dml.insert(blog, "id", "name");
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}

		Log log = new Log();
		log.setText("sequence test.");
		dml.insert(log);
		dml.insert(log, "text");

		try {
			Post post = dml.select(Post.class, "1");
			if (post == null) {
				post = new Post();
				post.setId("1");
				post.setBlogId("1");
				post.setTitle("The title of test post.");
				post.setAuthor("junguitar@gmail.com");
				post.setContent("The content of test post");
				dml.insert(post);
			}
		} catch (Exception e) {
			logger.warn(e.getMessage(), e);
		}
	}
	@AfterClass
	public static void afterClass() {
		try {
			dml.delete(Blog.class, "1");
			dml.delete(Blog.class, "2");
			dml.deleteList(Log.class, new Query());
			dml.delete(Post.class, "1");
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	public abstract Dml getDml();

	@Test
	public void select() throws Exception {
		logger.info("case " + i++ + ": select by id value");
		{
			dml.select(Blog.class, "1");
			dml.selectWithLock(Blog.class, "1");
		}
		logger.info("case " + i++ + ": select by sql to Map");
		{
			@SuppressWarnings("unchecked")
			Map<String, Object> data = (Map<String, Object>) dml.selectBySql("select id iD2 from blog where id = '1'", ValueUtils.toMap("id:1"),
					Map.class);
			for (String column : data.keySet())
				Assert.assertEquals("ID2", column);
		}

		logger.info("case " + i++ + ": select by data object");
		{
			Blog blog = new Blog();
			blog.setId("1");
			blog = dml.select(blog);
			blog = dml.selectWithLock(blog);
		}

		logger.info("case " + i++ + ": select by Filter");
		{
			dml.select(Blog.class, new Filter("id", "1"));
			dml.selectWithLock(Blog.class, new Filter("id", "1"));
		}

		logger.info("case " + i++ + ": select by Filter[]");
		{
			dml.select(Blog.class, new Object[] { new Filter("id", "1") });
			dml.selectWithLock(Blog.class, new Object[] { new Filter("id", "1") });
		}

		logger.info("case " + i++ + ": select by Query object");
		{
			dml.select(Blog.class, new Query().addFilter("id", "1"));
			dml.selectWithLock(Blog.class, new Query().addFilter("id", "1"));
		}

		logger.info("case " + i++ + ": select by Map");
		{
			Map<String, Object> id = new HashMap<String, Object>();
			id.put("id", "1");
			dml.select(Blog.class, id);
			dml.selectWithLock(Blog.class, id);
		}

		logger.info("case " + i++ + ": select by another object");
		{
			Id id = new Id();
			id.setId("1");
			dml.select(Blog.class, id);
			dml.selectWithLock(Blog.class, id);
		}

		logger.info("case " + i++ + ": select by tableName");
		{
			dml.select("blog", "1", Map.class);
			dml.selectWithLock("blog", "1", Map.class);
		}

		logger.info("case " + i++ + ": select partial fields");
		{
			dml.select(Blog.class, new Query().addSelect("id", "name").addFilter("id", "1"));
			dml.selectWithLock(Blog.class, new Query().addSelect("id", "name").addFilter("id", "1"));
		}

		logger.info("case " + i++ + ": select size by sql");
		{
			logger.info("size: " + dml.selectBySql("select count(*) from blog", null, Integer.class));
		}

		logger.info("case " + i++ + ": select data that has relation.");
		{
			Post post = dml.select(Post.class, "1");
			logger.info("post: " + post);
		}
	}

	class Id {
		private String id;
		public String getId() {
			return id;
		}
		public void setId(String id) {
			this.id = id;
		}
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void selectList() throws Exception {
		logger.info("case " + i++ + ": select list");
		{
			for (Blog data : dml.selectList(Blog.class, new Query(0, 10)))
				logger.debug("selected data: " + data.getId());
			for (Blog data : dml.selectListWithLock(Blog.class, new Query()))
				logger.debug("selected data: " + data.getId());
			try {
				for (Blog data : dml.selectListWithLock(Blog.class, new Query(0, 10)))
					logger.debug("selected data: " + data.getId());
			} catch (DbistRuntimeException e) {
				logger.info(e.getMessage());
			}
			for (Blog data : dml.selectList(Blog.class, new Query(1, 10)))
				logger.debug("selected data: " + data.getId());
			try {
				for (Blog data : dml.selectListWithLock(Blog.class, new Query(1, 10)))
					logger.debug("selected data: " + data.getId());
				Assert.fail("pageIndex 1 query was executed but with lock?");
			} catch (DbistRuntimeException e) {
				logger.info(e.getMessage());
			}
		}

		logger.info("case " + i++ + ": select list by subfilters");
		{
			Query query = new Query("or", 0, 10);
			query.addFilter("owner", "!=", "junguita@hotmail.com");
			query.addFilters(new Filters("and").addFilter("name", "test").addFilter("name", "1"));
			query.addFilters(new Filters("and").addFilter("name", "test2").addFilter("name", "2"));
			for (Blog data : dml.selectList(Blog.class, query))
				logger.debug("selected data: " + data.getId());
			query.setPageSize(0);
			for (Blog data : dml.selectListWithLock(Blog.class, query))
				logger.debug("selected data: " + data.getId());
			try {
				query.setPageSize(10);
				for (Blog data : dml.selectListWithLock(Blog.class, query))
					logger.debug("selected data: " + data.getId());
			} catch (DbistRuntimeException e) {
				logger.info(e.getMessage());
			}
			query.setPageIndex(1);
			for (Blog data : dml.selectList(Blog.class, query))
				logger.debug("selected data: " + data.getId());
			try {
				for (Blog data : dml.selectListWithLock(Blog.class, query))
					logger.debug("selected data: " + data.getId());
				Assert.fail("pageIndex 1 query was executed but with lock?");
			} catch (DbistRuntimeException e) {
				logger.info(e.getMessage());
			}
		}

		logger.info("case " + i++ + ": select list by subfilters only");
		{
			Query query = new Query("or", 0, 10);
			query.addFilters(new Filters("and").addFilter("name", "test").addFilter("name", "1"));
			query.addFilters(new Filters("and").addFilter("name", "test2").addFilter("name", "2"));
			for (Blog data : dml.selectList(Blog.class, query))
				logger.debug("selected data: " + data.getId());
			query.setPageSize(0);
			for (Blog data : dml.selectListWithLock(Blog.class, query))
				logger.debug("selected data: " + data.getId());
			query.setPageSize(10);
			try {
				for (Blog data : dml.selectListWithLock(Blog.class, query))
					logger.debug("selected data: " + data.getId());
			} catch (DbistRuntimeException e) {
				logger.info(e.getMessage());
			}
			query.setPageIndex(1);
			for (Blog data : dml.selectList(Blog.class, query))
				logger.debug("selected data: " + data.getId());
			try {
				for (Blog data : dml.selectListWithLock(Blog.class, query))
					logger.debug("selected data: " + data.getId());
				Assert.fail("pageIndex 1 query was executed but with lock?");
			} catch (DbistRuntimeException e) {
				logger.info(e.getMessage());
			}
		}

		logger.info("case " + i++ + ": select group by list");
		{
			for (Blog data : dml.selectList(Blog.class, new Query().addGroup("owner", "name")))
				logger.debug("selected data: " + data.getOwner() + ", " + data.getName());
			logger.debug("selected count: " + dml.selectSize(Blog.class, new Query().addGroup("owner", "name")));
			try {
				for (Blog data : dml.selectListWithLock(Blog.class, new Query().addGroup("owner", "name")))
					logger.debug("selected data: " + data.getOwner() + ", " + data.getName());
				Assert.fail("Grouping query was executed but with lock?");
			} catch (DbistRuntimeException e) {
				logger.info(e.getMessage());
			}
		}

		logger.info("case " + i++ + ": select list by sql");
		{
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("name", "test");
			for (Map<String, Object> map : dml.selectListBySql("select * from blog where name <> :name", paramMap, Map.class, 0, 10)) {
				logger.debug("\r\n\r\nselected data: ");
				for (String key : map.keySet())
					logger.debug("\t" + map.get(key));
			}
		}

		logger.info("case " + i++ + ": select group by sql");
		{
			List<Map> list = dml.selectListBySqlPath("org/dbist/dml/test.sql", null, Map.class, 0, 10);
			for (Map<String, Object> map : list) {
				logger.debug("\r\n\r\nselected data: ");
				for (String key : map.keySet())
					logger.debug("\t" + map.get(key));
			}
		}
		{
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("abc", "test");
			List<Map> list = dml.selectListBySqlPath("org/dbist/dml/test", paramMap, Map.class, 0, 10);
			for (Map<String, Object> map : list) {
				logger.debug("\r\n\r\nselected data: ");
				for (String key : map.keySet())
					logger.debug("\t" + map.get(key));
			}
		}

		logger.info("case " + i++ + ": select data that has relation and relational conditions.");
		{
			List<Post> list = dml.selectList(Post.class, new Query(0, 10).addSelect("id", "title").addFilter("blog.blogName", "1 Name"));
			logger.info("post: " + list);
		}

		logger.info("case " + i++ + ": select data that has relation and relational conditions and unselect some fields.");
		{
			List<Post> list = dml.selectList(Post.class, new Query(0, 10).addUnselect("blog", "authorName").addFilter("blog.blogName", "1 Name"));
			logger.info("post: " + list);
		}
	}

	@Test
	public void selectSize() throws Exception {
		logger.info("case " + i++ + ": select size");
		{
			logger.debug("selected size: " + dml.selectSize(Blog.class, new Query()));
			logger.debug("selected size: " + dml.selectSize(Blog.class, new Query(1, 10)));
		}

		logger.info("case " + i++ + ": select size by subfilters");
		{
			Query query = new Query("or", 0, 10);
			query.addFilter("owner", "!=", "junguita@hotmail.com");
			query.addFilters(new Filters("and").addFilter("name", "test").addFilter("name", "1"));
			query.addFilters(new Filters("and").addFilter("name", "test2").addFilter("name", "2"));
			logger.debug("selected size: " + dml.selectSize(Blog.class, query));
			query.setPageIndex(1);
			logger.debug("selected size: " + dml.selectSize(Blog.class, query));
		}

		logger.info("case " + i++ + ": select size by subfilters only");
		{
			Query query = new Query("or", 0, 10);
			query.addFilters(new Filters("and").addFilter("name", "test").addFilter("name", "1"));
			query.addFilters(new Filters("and").addFilter("name", "test2").addFilter("name", "2"));
			logger.debug("selected size: " + dml.selectSize(Blog.class, query));
			query.setPageIndex(1);
			logger.debug("selected size: " + dml.selectSize(Blog.class, query));
		}

		logger.info("case " + i++ + ": select size group by list");
		{
			logger.debug("selected count: " + dml.selectSize(Blog.class, new Query().addGroup("owner", "name")));
		}

		logger.info("case " + i++ + ": select size by sql");
		{
			Map<String, Object> paramMap = new HashMap<String, Object>();
			paramMap.put("name", "test");
			logger.debug("selected size: " + dml.selectSizeBySql("select * from blog where name <> :name", paramMap));
		}

		logger.info("case " + i++ + ": select size group by sql");
		{
			logger.debug("selected size: " + dml.selectSizeBySqlPath("org/dbist/dml/test.sql", null));
		}

		logger.info("case " + i++ + ": select size that has relation and relational conditions.");
		{
			logger.debug("selected size: "
					+ dml.selectSize(Post.class, new Query(0, 10).addSelect("id", "title").addFilter("blog.blogName", "1 Name")));
		}

		logger.info("case " + i++ + ": select size that has relation and relational conditions and unselect some fields.");
		{
			logger.debug("selected size: "
					+ dml.selectSize(Post.class, new Query(0, 10).addUnselect("blog", "authorName").addFilter("blog.blogName", "1 Name")));
		}
	}

	@Test
	public void testCombinedSql() throws Exception {
		logger.info("case " + i++ + ": select list with combined condition");
		{
			Query query = new Query();
			query.addSelect("id", "name");
			query.addOrder("name", true);
			query.addFilter("name", "!=", "test");
			query.addFilter("description", "like", "%the%\\_", false, '\\');
			query.addFilter("createdAt", "!=", null);
			query.addFilter("owner", new String[] { "junguitar@gmail.com", "junguita@hotmail.com" });
			dml.selectList(Blog.class, query);
			dml.selectListWithLock(Blog.class, query);
		}

		logger.info("case " + i++ + ": select group by list with combined condition");
		{
			Query query = new Query();
			query.addOrder("name", true);
			query.addFilter("name", "!=", "test");
			query.addFilter("description", "like", "%the%", false);
			query.addFilter("createdAt", "!=", null);
			query.addFilter("owner", new String[] { "junguitar@gmail.com", "junguita@hotmail.com" });
			query.addGroup("owner", "name");
			dml.selectList(Blog.class, query);
			dml.selectSize(Blog.class, query);
		}

		logger.info("case " + i++ + ": select list with combined condition2");
		{
			Query query = new Query();
			query.addSelect("id", "name");
			query.addOrder("name", true);
			query.setOperator("or");
			query.addFilter("description", "like", "%the%", false);
			query.addFilter("createdAt", "!=", null);
			query.addFilter("owner", new String[] { "junguitar@gmail.com", "junguita@hotmail.com" });
			dml.selectList(Blog.class, query);
			dml.selectListWithLock(Blog.class, query);
		}

		logger.info("case " + i++ + ": select group by list with combined condition2");
		{
			Query query = new Query();
			query.addOrder("name", true);
			query.setOperator("or");
			query.addFilter("description", "like", "%the%", false);
			query.addFilter("createdAt", "!=", null);
			query.addFilter("owner", new String[] { "junguitar@gmail.com", "junguita@hotmail.com" });
			query.addGroup("owner", "name");
			dml.selectList(Blog.class, query);
		}
	}

	@Test
	public void update() throws Exception {
		Blog blog = dml.select(Blog.class, "1");
		blog.setName("1 Name");
		dml.update(blog, "name");
	}

	@Test
	public void updateBatch() throws Exception {
		Blog blog = dml.select(Blog.class, "1");
		Blog blog2 = dml.select(Blog.class, "2");
		blog.setName("1 Name");
		blog2.setName("2 Name");
		dml.updateBatch(ValueUtils.toList(blog, blog2), "name");
	}
}
