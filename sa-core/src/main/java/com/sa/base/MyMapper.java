

package com.sa.base;

import com.sa.base.mapper.Mapper;
import com.sa.mapper.SsCustomMapper;
import tk.mybatis.mapper.annotation.RegisterMapper;
import tk.mybatis.mapper.common.MySqlMapper;


@RegisterMapper
public interface MyMapper<T> extends Mapper<T>, MySqlMapper<T>, SsCustomMapper<T> {


}
