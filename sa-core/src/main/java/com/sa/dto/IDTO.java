package com.sa.dto;

import com.alibaba.fastjson.annotation.JSONField;
import com.sa.util.POJOUtils;
import org.apache.commons.collections.map.HashedMap;

import javax.persistence.Transient;
import java.io.Serializable;
import java.lang.reflect.Field;
import java.util.Map;


public interface IDTO extends Serializable {

	String AND_CONDITION_EXPR = "_andConditionExpr";
	String OR_CONDITION_EXPR = "_orConditionExpr";


	@Transient
	String NULL_VALUE_FIELD = "null_value_field";

	String ERROR_MSG_KEY = "errorMessage";

	default Object aget(String property){
		return POJOUtils.getProperty(this, property);
	}


	default DTO aget(){
		return new DTO();
	}


	default void aset(String property, Object value){
		POJOUtils.setProperty(this, property, value);
	}


	default void aset(DTO dto){
	}


	default Object mget(String key){return null;}


	default Map<String, Object> mget(){return new HashedMap();}


	default void mset(String key, Object value){}


	default void mset(Map<String, Object> metadata){}


	@JSONField(serialize=false)
	@Transient
	default Map<String, Field> getFields(){return null;}
}
