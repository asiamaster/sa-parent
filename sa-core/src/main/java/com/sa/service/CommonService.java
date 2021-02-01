package com.sa.service;

import com.alibaba.fastjson.JSONObject;
import com.sa.dto.IDTO;
import com.sa.metadata.ValuePair;

import java.util.List;
import java.util.Map;


public interface CommonService {

	List<ValuePair<?>> selectValuePair(String sql);

	List<JSONObject> selectJSONObject(String sql, Integer page, Integer rows);

	List<Map> selectMap(String sql, Integer page, Integer rows);

	<T extends IDTO> List<T> selectDto(String sql, Class<T> resultType);

	<T extends IDTO> List<T> selectDto(String sql, Class<T> resultType, Integer page, Integer rows);

	void execute(String sql);
}
