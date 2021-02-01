package com.sa.domain;

import com.sa.dto.IDomain;

import javax.persistence.Transient;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;


public class Domain<KEY extends Serializable> implements IDomain<KEY> {





	@Transient
	protected KEY id;
	@Transient
	private Integer page;
	@Transient
	private Integer rows;

	@Transient
	private String sort;
	@Transient
	private String order;
	@Transient
	private Map metadata;

	@Override
	public KEY getId() {
		return id;
	}
	@Override
	public void setId(KEY id) {
		this.id = id;
	}

	@Override
	public Integer getPage() {
		return page;
	}

	@Override
	public void setPage(Integer page) {
		this.page = page;
	}

	@Override
	public Integer getRows() {
		return rows;
	}

	@Override
	public void setRows(Integer rows) {
		this.rows = rows;
	}

	@Override
	public String getSort() {
		return sort;
	}

	@Override
	public void setSort(String sort) {
		this.sort = sort;
	}

	@Override
	public String getOrder() {
		return order;
	}

	@Override
	public void setOrder(String order) {
		this.order = order;
	}

	@Override
	public void setMetadata(String key, Object value){
		if(metadata == null){
			metadata = new HashMap();
		}
		metadata.put(key, value);
	}

	@Override
	public Object getMetadata(String key){
		return metadata == null ? null : metadata.get(key);
	}

	@Override
	public Map getMetadata() {
		return metadata;
	}

	@Override
	public void setMetadata(Map metadata) {
		this.metadata = metadata;
	}


	@Override
	public Boolean containsMetadata(String key) {
		return metadata == null?false:metadata.containsKey(key);
	}
}
