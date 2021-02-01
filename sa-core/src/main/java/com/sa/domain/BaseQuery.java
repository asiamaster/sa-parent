package com.sa.domain;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


@Deprecated

public class BaseQuery implements Serializable {

	private static final String DESC ="DESC";
	private static final String ASC ="ASC";

	private static final long serialVersionUID = 1L;


	private transient String orderFieldType;

	private transient String orderField;


	private transient Map<String, Object> queryData;


	private transient String keyword;


	public String getKeyword() {
		return keyword;
	}

	public String getOrderField() {
		return orderField;
	}

	public void setOrderField(String orderField) {
		this.orderField = orderField;
	}

	public String getOrderFieldType() {
		if(DESC.equalsIgnoreCase(orderFieldType) || ASC.equalsIgnoreCase(orderFieldType)) {
			return orderFieldType.toUpperCase();
		}
		return null;
	}


	public String getOrderFieldNextType() {
		if(DESC.equalsIgnoreCase(orderFieldType)) {
			return DESC;
		}
		return ASC;
	}

	public void setOrderFieldType(String orderFieldType) {
		this.orderFieldType = orderFieldType;
	}


	public Map<String, Object> getQueryData() {
		if(queryData != null && queryData.size() > 0) {
			return queryData;
		}
		return null;
	}

	public void setQueryData(Map<String, Object> queryData) {
		this.queryData = queryData;
	}


	public void addQueryData(String key,Object value) {
		if(queryData == null) {
			queryData = new HashMap<String, Object>();
		}
		queryData.put(key, value);
	}

	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}



}
