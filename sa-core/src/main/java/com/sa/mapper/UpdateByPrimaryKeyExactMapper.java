package com.sa.mapper;

import com.sa.dao.provider.ExactUpdateProvider;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.UpdateProvider;
import tk.mybatis.mapper.annotation.RegisterMapper;


@RegisterMapper
public interface UpdateByPrimaryKeyExactMapper<T> {


    @UpdateProvider(type = ExactUpdateProvider.class, method = "dynamicSQL")
    @Options(useCache = false, useGeneratedKeys = false)
    int updateByPrimaryKeyExact(T record);
}