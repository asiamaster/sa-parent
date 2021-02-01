package com.sa.mapper;

import com.sa.dao.provider.ExactUpdateProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.UpdateProvider;
import tk.mybatis.mapper.annotation.RegisterMapper;


@RegisterMapper
public interface UpdateByExampleExactMapper<T> {


    @UpdateProvider(type = ExactUpdateProvider.class, method = "dynamicSQL")
    @Options(useCache = false, useGeneratedKeys = false)
    int updateByExampleExact(@Param("record") T record, @Param("example") Object example);
}