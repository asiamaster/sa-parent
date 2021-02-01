package com.sa.base;


import com.sa.dto.IBaseDomain;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.Serializable;



@Service
public abstract class BaseServiceImpl<T extends IBaseDomain, KEY extends Serializable> extends BaseServiceAdaptor<T, KEY> {
	protected static final Logger LOGGER = LoggerFactory.getLogger(BaseServiceImpl.class);

	@Autowired
	private MyMapper<T> mapper;


	@Override
	public MyMapper<T> getDao(){
		return this.mapper;
	}

}