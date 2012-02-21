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

import java.util.List;

/**
 * @author Steve M. Jung
 * @since 2012. 2. 21. (version 0.0.1)
 */
public class Page<T> {
	private int index;
	private int lastIndex;
	private int size;
	private int totalSize;
	private List<T> list;
	public int getIndex() {
		return index;
	}
	public void setIndex(int pageIndex) {
		this.index = pageIndex;
	}
	public int getLastIndex() {
		return lastIndex;
	}
	public void setLastIndex(int lastPageIndex) {
		this.lastIndex = lastPageIndex;
	}
	public int getSize() {
		return size;
	}
	public void setSize(int pageSize) {
		this.size = pageSize;
	}
	public int getTotalSize() {
		return totalSize;
	}
	public void setTotalSize(int totalSize) {
		this.totalSize = totalSize;
	}
	public List<T> getList() {
		return list;
	}
	public void setList(List<T> list) {
		this.list = list;
	}
}
