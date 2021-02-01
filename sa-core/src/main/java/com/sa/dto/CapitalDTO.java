package com.sa.dto;


public class CapitalDTO extends DTO{
	private static final long serialVersionUID = -688089562635699991L;


	@Override
	public boolean containsKey(Object key) {
		return super.containsKey(((String)key).toUpperCase());
	}

	@Override
	public Object put(String key, Object value) {
		return super.put(key.toUpperCase(), value);
	}


	@Override
	public Object get(Object key) {
		return super.get(((String)key).toUpperCase());
	}

	@Override
	public Object remove(Object key) {
		return super.remove(((String)key).toUpperCase());
	}

}