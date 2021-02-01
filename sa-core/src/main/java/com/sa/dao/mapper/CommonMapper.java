package com.sa.dao.mapper;


import com.alibaba.fastjson.JSONObject;
import com.sa.dto.IDTO;
import com.sa.metadata.ValuePair;
import org.apache.ibatis.annotations.Param;

import java.util.List;
import java.util.Map;


public interface CommonMapper {

    List<ValuePair<?>> selectValuePair(String sql);


    List<JSONObject> selectJSONObject(String sql);


    List<Map> selectMap(String sql);


    <T extends IDTO> List<T> selectDto(@Param("value") String sql, @Param("resultType") Class<T> resultType);


    void execute(String sql);
}
