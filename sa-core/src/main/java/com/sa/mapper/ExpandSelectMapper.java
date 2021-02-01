package com.sa.mapper;

import com.sa.dao.provider.ExpandSelectProvider;
import org.apache.ibatis.annotations.SelectProvider;
import org.apache.ibatis.session.RowBounds;
import tk.mybatis.mapper.annotation.RegisterMapper;

import java.util.List;


@RegisterMapper
public interface ExpandSelectMapper<T> {


    @SelectProvider(type = ExpandSelectProvider.class, method = "dynamicSQL")
    List<T> selectExpand(T record);


    @SelectProvider(type = ExpandSelectProvider.class, method = "dynamicSQL")
    T selectOneExpand(T record);


    @SelectProvider(type = ExpandSelectProvider.class, method = "dynamicSQL")
    T selectByPrimaryKeyExpand(Object key);


    @SelectProvider(type = ExpandSelectProvider.class, method = "dynamicSQL")
    List<T> selectAllExpand();


    @SelectProvider(type = ExpandSelectProvider.class, method = "dynamicSQL")
    List<T> selectByRowBoundsExpand(T record, RowBounds rowBounds);


    @SelectProvider(type = ExpandSelectProvider.class, method = "dynamicSQL")
    List<T> selectByExampleExpand(Object example);


    @SelectProvider(type = ExpandSelectProvider.class, method = "dynamicSQL")
    T selectOneByExampleExpand(Object example);
}