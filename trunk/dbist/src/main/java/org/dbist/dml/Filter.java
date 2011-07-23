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

import net.sf.common.util.ValueUtils;

/**
 * @author Steve M. Jung
 * @since 2 June 2011 (version 0.0.1)
 */
public class Filter {
	private String leftOperand;
	private String operator;
	private List<Object> rightOperand;
	public Filter(String leftOperand) {
		this.leftOperand = leftOperand;
	}
	public Filter(String leftOperand, Object rightOperand) {
		this(leftOperand);
		if (!ValueUtils.isEmpty(rightOperand))
			addRightOperand(rightOperand);
	}
	public Filter(String leftOperand, String operator, Object rightOperand) {
		this(leftOperand, rightOperand);
		this.operator = operator;
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
	public void setRightOperand(List<Object> rightOperand) {
		this.rightOperand = rightOperand;
	}
	public <T> T addRightOperand(T rightOperand) {
		if (this.rightOperand == null)
			this.rightOperand = new ArrayList<Object>();
		this.rightOperand.add(rightOperand);
		return rightOperand;
	}
}
