package com.sa.metadata.provider;

import com.alibaba.fastjson.JSONObject;
import com.sa.service.CommonService;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Component
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
public abstract class BatchSqlDisplayTextProviderAdaptor extends BatchDisplayTextProviderAdaptor {

    @Autowired
    protected CommonService commonService;

    @Override
    protected List<Map> getFkList(List<String> relationIds, Map metaMap) {
        StringBuilder sqlBuilder = new StringBuilder("select * from ");
        sqlBuilder.append(getRelationTable(metaMap));
        sqlBuilder.append(" where ");
        sqlBuilder.append(getRelationTablePkField(metaMap));
        sqlBuilder.append(" in('");
        sqlBuilder.append(relationIds.get(0));
        sqlBuilder.append("'");
        for(int i=1; i<relationIds.size(); i++){
            sqlBuilder.append(",'").append(relationIds.get(i)).append("'");
        }
        sqlBuilder.append(")");

        JSONObject conditionJson = getQueryParams(metaMap);
        if(conditionJson != null && !conditionJson.isEmpty()) {
            for(Map.Entry<String, Object> conditionEntry : Collections.unmodifiableMap(conditionJson).entrySet()){
                sqlBuilder.append(" and `").append(conditionEntry.getKey()).append("`='").append(conditionEntry.getValue()).append("'");
            }
        }
        return commonService.selectMap(sqlBuilder.toString(), 1, relationIds.size());
    }


    protected abstract String getRelationTable(Map metaMap);


    protected JSONObject getQueryParams(Map metaMap){
        return null;
    }
}