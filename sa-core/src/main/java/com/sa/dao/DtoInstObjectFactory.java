package com.sa.dao;

import com.sa.dto.DTOUtils;
import com.sa.dto.IDTO;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;

import java.util.List;
import java.util.Properties;


public class DtoInstObjectFactory extends DefaultObjectFactory {

	private static final long serialVersionUID = 908294397084500018L;

	private Properties properties;

	@Override
	public <T> T create(Class<T> type, List<Class<?>> constructorArgTypes,
	                    List<Object> constructorArgs) {
		return super.create(type, constructorArgTypes, constructorArgs);
	}


	@Override
	public <T> T create(Class<T> type) {
		if(type.isInterface() && IDTO.class.isAssignableFrom(type)){
			return (T) DTOUtils.newInstance((Class<IDTO>)type);
		}else {
			return super.create(type);
		}
	}

	@Override
	public void setProperties(Properties properties) {
		this.properties = properties;
		super.setProperties(properties);
	}

}
