package com.sa.mapper;

import tk.mybatis.mapper.annotation.RegisterMapper;


@RegisterMapper
public interface UpdateExactMapper<T> extends UpdateByExampleExactMapper<T>, UpdateByPrimaryKeyExactMapper<T> {

}