package com.smartsheet.restapi.model;

import java.util.List;

public class SmartsheetPagingwrapper<T> {
	private int pageNumber;
	private int pageSize;
	private int totalPages;
	private int totalCount;
	private List<T> data;

	public int getPageNumber() {
		return pageNumber;
	}

	public void setPageNumber(int pageNumber) {
		this.pageNumber = pageNumber;
	}

	public int getPageSize() {
		return pageSize;
	}

	public void setPageSize(int pageSize) {
		this.pageSize = pageSize;
	}

	public int getTotalPages() {
		return totalPages;
	}

	public void setTotalPages(int totalPages) {
		this.totalPages = totalPages;
	}

	public int getTotalCount() {
		return totalCount;
	}

	public void setTotalCount(int totalCount) {
		this.totalCount = totalCount;
	}

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	@Override
	public String toString() {
		return "SmartsheetPagingwrapper [pageNumber=" + pageNumber + ", pageSize=" + pageSize + ", totalPages="
				+ totalPages + ", totalCount=" + totalCount + ", data=" + data + "]";
	}
	

}
