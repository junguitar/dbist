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

import java.util.ArrayList;
import java.util.List;

/**
 * @author Steve M. Jung
 * @since 2 June 2011 (version 0.0.1)
 */
public class Filters {
	private String operator;
	private List<Filter> filter;
	private List<Filters> filters;
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public List<Filter> getFilter() {
		return filter;
	}
	public void setFilter(List<Filter> filter) {
		this.filter = filter;
	}
	public Filters addFilter(Filter filter) {
		if (this.filter == null)
			this.filter = new ArrayList<Filter>();
		this.filter.add(filter);
		return this;
	}
	public Filters addFilter(String leftOperand, Object rightOperand) {
		addFilter(new Filter(leftOperand, rightOperand));
		return this;
	}
	public Filters addFilter(String leftOperand, String operator, Object rightOperand) {
		addFilter(new Filter(leftOperand, operator, rightOperand));
		return this;
	}
	public List<Filters> getFilters() {
		return filters;
	}
	public void setFilters(List<Filters> filters) {
		this.filters = filters;
	}
	public Filters addFilters(Filters filters) {
		if (this.filters == null)
			this.filters = new ArrayList<Filters>();
		this.filters.add(filters);
		return this;
	}
}
