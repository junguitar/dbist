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
package com.googlecode.dbist.dml;

import java.util.ArrayList;
import java.util.List;

public class Query extends Filters {
	private int pageNo;
	private int pageSize;
	private List<Order> order;
	private List<String> group;
	public int getPageNo() {
		return pageNo;
	}
	public void setPageNo(int pageNo) {
		this.pageNo = pageNo;
	}
	public int getPageSize() {
		return pageSize;
	}
	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}
	public List<Order> getOrder() {
		return order;
	}
	public void setOrder(List<Order> order) {
		this.order = order;
	}
	public Order addOrder(Order order) {
		if (this.order == null)
			this.order = new ArrayList<Order>();
		this.order.add(order);
		return order;
	}
	public List<String> getGroup() {
		return group;
	}
	public void setGroup(List<String> group) {
		this.group = group;
	}
	public String addGroup(String group) {
		if (this.group == null)
			this.group = new ArrayList<String>();
		this.group.add(group);
		return group;
	}
}
