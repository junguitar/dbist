package org.dbist.dml;

import java.util.List;

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
