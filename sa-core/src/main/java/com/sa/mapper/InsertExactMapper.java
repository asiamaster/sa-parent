package com.sa.mapper;

import com.sa.dao.provider.ExactInsertProvider;
import org.apache.ibatis.annotations.InsertProvider;
import tk.mybatis.mapper.annotation.RegisterMapper;


@RegisterMapper
public interface InsertExactMapper<T> {

    @InsertProvider(type = ExactInsertProvider.class, method = "dynamicSQL")
    int insertExact(T record);
}