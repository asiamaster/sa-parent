package com.sa.mapper;

import tk.mybatis.mapper.annotation.RegisterMapper;


@RegisterMapper
public interface SsCustomMapper<T> extends UpdateExactMapper<T>, InsertExactMapper<T>, ExpandSelectMapper<T> {
}
