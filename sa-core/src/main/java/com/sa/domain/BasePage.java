package com.sa.domain;

import java.util.Collections;
import java.util.List;



@Deprecated
public class BasePage<T> extends BaseQuery{

	private static final long serialVersionUID = 11234564786156318L;

	public static final String PAGE_SIZE_KEY="rows";

	public static final String PAGE_INDEX_KEY="page";

	public static final Integer DEFAULT_PAGE_SIZE = 20;


	private Integer page = 1;


	private Integer rows = DEFAULT_PAGE_SIZE;


	private List<T> datas = Collections.EMPTY_LIST;


	private Integer totalPage = 0;

	private transient Long startIndex = 1L;


	private Long totalItem = 0L;


	public BasePage() {

	}



	public Long getStartIndex() {
		return startIndex;
	}
	public void setStartIndex(Long startIndex) {
		this.startIndex = startIndex;
	}


	public Integer startIndex() {
		return (getPage() - 1) * this.rows;
	}


	public Integer endIndex() {
		return getPage() * this.rows;
	}


	public boolean firstPage() {
		return getPage() <= 1;
	}


	public boolean lastPage() {
		return getPage() >= pageCount();
	}


	public Integer nextPage() {
		if (lastPage()) {
			return getPage();
		} 
		return getPage() + 1;
	}


	public Integer previousPage() {
		if (firstPage()) {
			return 1;
		}
		return getPage() - 1;
	}


	public Integer getPage() {
		if (page == 0) {
			page = 1;
		}
		return page;
	}


	public Long pageCount() {
		if (totalItem % rows == 0) {
			return totalItem / rows;
		} else {
			return totalItem / rows + 1;
		}
	}


	public Long getTotalItem() {
		return this.totalItem;
	}


	public void setPage(Integer page) {
		this.page = page;
	}

	public Integer getTotalPage() {
		return totalPage;
	}

	public void setTotalPage(Integer totalPage) {
		this.totalPage = totalPage;
	}


	public Integer getRows() {
		return rows;
	}
	
	public void setRows(Integer rows) {
		this.rows = rows;
	}

	public boolean hasNextPage() {
		return getPage() < pageCount();
	}


	public boolean hasPreviousPage() {
		return getPage() > 1;
	}



	public List<T> getDatas() {
		return datas;
	}


	public void setDatas(List<T> data) {
		this.datas = data;
	}


	public void setTotalItem(Long totalItem) {
		this.totalItem = totalItem;

	}

	public boolean isNextPageAvailable() {
		return this.page >this.totalPage;
	}
	public boolean isPreviousPageAvailable() {
		return this.page <this.totalPage&&this.page >1;
	}

	public void repaginate() {
		if (totalItem > 0) {
			setTotalPage((int) (totalItem / rows + (totalItem % rows > 0 ? 1 : 0)));
			if(page > totalPage) {
				setPage(totalPage);
			}
			this.setTotalPage(totalPage);
			setStartIndex((long) ((page - 1)* rows));
			if(startIndex<0){
				setStartIndex(0L);
			}
			if(startIndex>totalItem){
				setStartIndex(totalItem);
			}
		}
	}

}
