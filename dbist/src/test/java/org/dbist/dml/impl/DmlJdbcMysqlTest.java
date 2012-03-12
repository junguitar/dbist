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
package org.dbist.dml.impl;

import net.sf.common.util.BeanUtils;

import org.dbist.dml.AbstractDmlTest;
import org.dbist.dml.Dml;
import org.junit.runner.RunWith;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Steve Jung
 * @since 2012. 3. 12.
 */
@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "classpath:WEB-INF/beans.xml", "classpath:WEB-INF/beans-mysql.xml" })
public class DmlJdbcMysqlTest extends AbstractDmlTest {
	@Override
	public Dml getDml() {
		return BeanUtils.getInstance("dbist-example").get(DmlJdbc.class, Dml.class);
	}
}
