package com.sa.base.mapper;

import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.base.BaseInsertMapper;
import tk.mybatis.mapper.common.base.BaseSelectMapper;
import tk.mybatis.mapper.common.base.BaseUpdateMapper;


@RegisterMapper
public interface BaseMapper<T> extends
        BaseSelectMapper<T>,
        BaseInsertMapper<T>,
        BaseUpdateMapper<T>,
        BaseDeleteMapper<T> {


    default int deleteWithVersion(T t){
        int result = delete(t);
        if(result == 0){
            throw new RuntimeException("删除失败!");
        }
        return result;
    }


    default int updateByPrimaryKeyWithVersion(T t){
        int result = updateByPrimaryKey(t);
        if(result == 0){
            throw new RuntimeException("更新失败!");
        }
        return result;
    }


    default int updateByPrimaryKeySelectiveWithVersion(T t){
        int result = updateByPrimaryKeySelective(t);
        if(result == 0){
            throw new RuntimeException("更新失败!");
        }
        return result;
    }

}
