package com.sa.base.mapper;

import com.sa.dao.provider.BaseDeleteProvider;
import org.apache.ibatis.annotations.DeleteProvider;
import tk.mybatis.mapper.annotation.RegisterMapper;


@RegisterMapper
public interface DeleteMapper<T> {


    @DeleteProvider(type = BaseDeleteProvider.class, method = "dynamicSQL")
    int delete(T record);

}