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
public class Filter {
	private String leftOperand;
	private String operator;
	private List<?> rightOperand;
	private boolean caseSensitive = true;
	public Filter(String leftOperand, Object rightOperand) {
		this(leftOperand, "=", rightOperand);
	}
	public Filter(String leftOperand, String operator, Object rightOperand) {
		this.leftOperand = leftOperand;
		this.operator = operator;
		if (rightOperand != null)
			addRightOperand(rightOperand);
	}
	public Filter(String leftOperand, String operator, Object rightOperand, boolean caseSensitive) {
		this.leftOperand = leftOperand;
		this.operator = operator;
		addRightOperand(rightOperand);
		this.caseSensitive = caseSensitive;
	}
	public String getLeftOperand() {
		return leftOperand;
	}
	public void setLeftOperand(String leftOperand) {
		this.leftOperand = leftOperand;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public List<?> getRightOperand() {
		return rightOperand;
	}
	public void setRightOperand(List<?> rightOperand) {
		this.rightOperand = rightOperand;
	}
	public void setRightOperand(Object... rightOperand) {
		this.rightOperand = ValueUtils.toList(rightOperand);
	}
	public Filter addRightOperand(Object... rightOperand) {
		if (ValueUtils.isEmpty(rightOperand))
			return this;
		if (this.rightOperand == null)
			this.rightOperand = new ArrayList<Object>();
		@SuppressWarnings("unchecked")
		List<Object> list = (List<Object>) this.rightOperand;
		for (Object ro : rightOperand) {
			if (ro == null) {
				list.add(null);
			} else if (ro instanceof Object[]) {
				for (Object subRo : (Object[]) ro)
					list.add(subRo);
			} else if (ro instanceof List) {
				for (Object subRo : (List<?>) ro)
					list.add(subRo);
			} else {
				list.add(ro);
			}
		}
		return this;
	}
	public boolean isCaseSensitive() {
		return caseSensitive;
	}
	public void setCaseSensitive(boolean caseSensitive) {
		this.caseSensitive = caseSensitive;
	}
}
