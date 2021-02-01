package com.sa.metadata.provider;

import com.alibaba.fastjson.JSONObject;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
public class SimpleBatchDisplayTextProvider extends BatchSqlDisplayTextProviderAdaptor {

    protected static final String RELATION_TABLE_KEY = "_relationTable";

    protected static final String RELATION_TABLE_PK_FIELD_KEY = "_relationTablePkField";

    protected static final String QUERY_PARAMS_KEY = "queryParams";

    @Override
    protected Map<String, String> getEscapeFileds(Map metaMap){
        if(metaMap.get(ESCAPE_FILEDS_KEY) instanceof Map){
            return (Map)metaMap.get(ESCAPE_FILEDS_KEY);
        }else{
            String escapeField = (String)metaMap.get(ESCAPE_FILEDS_KEY);
            Map<String, String> map = new HashMap(1);
            map.put((String)metaMap.get(FIELD_KEY), escapeField);
            return map;
        }
    }








    @Override
    protected String getFkField(Map metaMap){
        return (String)metaMap.get(FK_FILED_KEY);
    }


    @Override
    protected String getRelationTable(Map metaMap){
        return (String)metaMap.get(RELATION_TABLE_KEY);
    }


    @Override
    protected String getRelationTablePkField(Map metaMap){
        return metaMap.get(RELATION_TABLE_PK_FIELD_KEY) == null ? "id" : (String)metaMap.get(RELATION_TABLE_PK_FIELD_KEY);
    }

    @Override
    protected JSONObject getQueryParams(Map metaMap){
        return metaMap.get(QUERY_PARAMS_KEY) == null ? null : JSONObject.parseObject(metaMap.get(QUERY_PARAMS_KEY).toString());
    }
}