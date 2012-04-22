/**
 * Copyright 2011-2012 the original author or authors.
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

import java.util.ArrayList;
import java.util.List;

import net.sf.common.util.ValueUtils;

/**
 * @author Steve M. Jung
 * @since 2011. 6. 2. (version 0.0.1)
 */
public class Query extends Filters {
	private int pageIndex;
	private int pageSize;
	private int firstResultIndex;
	private int maxResultSize;
	private List<String> field;
	private List<String> group;
	private List<Order> order;
	public Query() {
		super();
	}
	public Query(String operator) {
		super(operator);
	}
	public Query(int pageIndex, int pageSize) {
		super();
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}
	public Query(int pageIndex, int pageSize, int firstResultIndex, int maxResultSize) {
		super();
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
		this.firstResultIndex = firstResultIndex;
		this.maxResultSize = maxResultSize;
	}
	public Query(String operator, int pageIndex, int pageSize) {
		super(operator);
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
	}
	public Query(String operator, int pageIndex, int pageSize, int firstResultIndex, int maxResultSize) {
		super(operator);
		this.pageIndex = pageIndex;
		this.pageSize = pageSize;
		this.firstResultIndex = firstResultIndex;
		this.maxResultSize = maxResultSize;
	}
	public int getPageIndex() {
		return pageIndex;
	}
	public void setPageIndex(int pageIndex) {
		this.pageIndex = pageIndex;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public int getFirstResultIndex() {
		return firstResultIndex;
	}
	public void setFirstResultIndex(int firstResultIndex) {
		this.firstResultIndex = firstResultIndex;
	}
	public int getMaxResultSize() {
		return maxResultSize;
	}
	public void setMaxResultSize(int maxResultSize) {
		this.maxResultSize = maxResultSize;
	}
	public List<String> getField() {
		return field;
	}
	public void setField(List<String> field) {
		this.field = field;
	}
	public Query addField(String... field) {
		if (ValueUtils.isEmpty(field))
			return this;
		if (this.field == null)
			this.field = new ArrayList<String>();
		for (String f : field)
			this.field.add(f);
		return this;
	}
	public List<String> getGroup() {
		return group;
	}
	public void setGroup(List<String> group) {
		this.group = group;
	}
	public Query addGroup(String... group) {
		if (ValueUtils.isEmpty(group))
			return this;
		if (this.group == null)
			this.group = new ArrayList<String>();
		for (String g : group)
			this.group.add(g);
		return this;
	}
	public List<Order> getOrder() {
		return order;
	}
	public void setOrder(List<Order> order) {
		this.order = order;
	}
	public Query addOrder(Order... order) {
		if (ValueUtils.isEmpty(order))
			return this;
		if (this.order == null)
			this.order = new ArrayList<Order>();
		for (Order o : order)
			this.order.add(o);
		return this;
	}
	public Query addOrder(String field, boolean ascending) {
		return addOrder(new Order(field, ascending));
	}
}
