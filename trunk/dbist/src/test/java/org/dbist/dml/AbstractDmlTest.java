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
import java.util.Map;

import junit.framework.Assert;

import org.dbist.dml.impl.Blog;
import org.dbist.exception.DbistRuntimeException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Steve Jung
 * @since 2012. 3. 6.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = "classpath:WEB-INF/beans.xml")
public abstract class AbstractDmlTest {
	int i = 0;
	protected Logger logger = LoggerFactory.getLogger(getClass());
	protected Dml dml;

	@Before
	public void beforeTest() {
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
		} catch (Exception e) {

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
			dml.select(Blog.class, new Query().addField("id", "name").addFilter("id", "1"));
			dml.selectWithLock(Blog.class, new Query().addField("id", "name").addFilter("id", "1"));
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

	@Test
	public void selectList() throws Exception {
		logger.info("case " + i++ + ": select list");
		{
			for (Blog data : dml.selectList(Blog.class, new Query(0, 10)))
				logger.debug("selected data: " + data.getId());
			for (Blog data : dml.selectListWithLock(Blog.class, new Query(0, 10)))
				logger.debug("selected data: " + data.getId());
		}

		logger.info("case " + i++ + ": select group by list");
		{
			for (Blog data : dml.selectList(Blog.class, new Query().addGroup("owner", "name")))
				logger.debug("selected data: " + data.getId());
			try {
				for (Blog data : dml.selectListWithLock(Blog.class, new Query().addGroup("owner", "name")))
					logger.debug("selected data: " + data.getId());
				Assert.fail("Grouping query was executed but with lock?");
			} catch (DbistRuntimeException e) {
			}
		}
	}

	@Test
	public void testCombinedSql() throws Exception {
		logger.info("case " + i++ + ": select list with combined condition");
		{
			Query query = new Query();
			query.addField("id", "name");
			query.addOrder("name", true);
			query.addFilter("description", "like", "%the%", false);
			query.addFilter("createdAt", "!=", null);
			query.addFilter("owner", new String[] { "junguitar@gmail.com", "junguita@hotmail.com" });
			dml.selectList(Blog.class, query);
			dml.selectListWithLock(Blog.class, query);
		}

		logger.info("case " + i++ + ": select group by list with combined condition");
		{
			Query query = new Query();
			query.addOrder("name", true);
			query.addFilter("description", "like", "%the%", false);
			query.addFilter("createdAt", "!=", null);
			query.addFilter("owner", new String[] { "junguitar@gmail.com", "junguita@hotmail.com" });
			query.addGroup("owner", "name");
			dml.selectList(Blog.class, query);
		}

		logger.info("case " + i++ + ": select list with combined condition2");
		{
			Query query = new Query();
			query.addField("id", "name");
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
}
