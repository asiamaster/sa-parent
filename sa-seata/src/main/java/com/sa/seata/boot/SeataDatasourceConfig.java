package com.sa.seata.boot;

import com.alibaba.druid.pool.DruidDataSource;
import io.seata.rm.datasource.DataSourceProxy;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.autoconfigure.jdbc.DataSourceProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;



public class SeataDatasourceConfig {

    @Autowired
    private DataSourceProperties dataSourceProperties;

    @Bean("druidDataSourceProperties")
    public Map<String, String> druidDataSourceProperties() {
        return new HashMap<>();
    }





    @Primary
    @Bean("dataSource")
    public DataSourceProxy dataSource(@Qualifier("druidDataSourceProperties") Map<String, String> druidDataSourceProperties) throws InvocationTargetException, IllegalAccessException {
        DruidDataSource druidDataSource = new DruidDataSource();
        BeanUtils.copyProperties(dataSourceProperties, druidDataSource);
        org.apache.commons.beanutils.BeanUtils.populate(druidDataSource, druidDataSourceProperties);
        return new DataSourceProxy(druidDataSource);
    }

}
