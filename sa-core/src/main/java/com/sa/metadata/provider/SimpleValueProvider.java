package com.sa.metadata.provider;

import com.alibaba.fastjson.JSONObject;
import com.sa.dao.mapper.CommonMapper;
import com.sa.metadata.FieldMeta;
import com.sa.metadata.ValuePair;
import com.sa.metadata.ValueProvider;
import com.sa.util.SpringUtil;
import org.apache.commons.collections.map.HashedMap;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Map;


@Component
@Scope("prototype")
@ConditionalOnClass({ SqlSessionFactory.class, SqlSessionFactoryBean.class })
public class SimpleValueProvider implements ValueProvider {

    private String table;

    private String valueField;

    private String textField;

    private Object value;

    private String orderByClause;

    private Map<String, Object> queryParams = new HashedMap();

    protected static final String TABLE_KEY = "table";
    protected static final String VALUEFIELD_KEY = "valueField";
    protected static final String TEXTFIELD_KEY = "textField";
    protected static final String VALUE_KEY = "value";
    protected static final String ORDER_BY_CLAUSE_KEY = "orderByClause";

    @Autowired
    protected CommonMapper commonMapper;

    public SimpleValueProvider(){}

    protected void buildParam(Map paramMap){
        if(paramMap.get(TABLE_KEY) != null) {
            setTable(paramMap.get(TABLE_KEY).toString());
            paramMap.remove(TABLE_KEY);
        }
        if(paramMap.get(VALUEFIELD_KEY) != null) {
            setValueField(paramMap.get(VALUEFIELD_KEY).toString());
            paramMap.remove(VALUEFIELD_KEY);
        }
        if(paramMap.get(TEXTFIELD_KEY) != null) {
            setTextField(paramMap.get(TEXTFIELD_KEY).toString());
            paramMap.remove(TEXTFIELD_KEY);
        }
        if(paramMap.get(VALUE_KEY) != null) {
            setValue(paramMap.get(VALUE_KEY));
            paramMap.remove(VALUE_KEY);
        }
        if(paramMap.get(ORDER_BY_CLAUSE_KEY) != null) {
            setOrderByClause(paramMap.get(ORDER_BY_CLAUSE_KEY).toString());
            paramMap.remove(ORDER_BY_CLAUSE_KEY);
        }

        Object queryParams = paramMap.get(QUERY_PARAMS_KEY);
        if(queryParams != null) {
            getQueryParams().clear();
            setQueryParams(JSONObject.parseObject(queryParams.toString()));
        }
    }


    @Override
    public List<ValuePair<?>> getLookupList(Object value, Map paramMap, FieldMeta fieldMeta){
        buildParam(paramMap);
        List<ValuePair<?>> data = commonMapper.selectValuePair(buildSql());
        return data;
    }

    private String buildSql(){
        StringBuffer sql = new StringBuffer();
        sql.append("select ").append(getValueField()).append(" value, ").append(getTextField()).append(" text from ").append(getTable());
        if(getQueryParams()!= null && !getQueryParams().isEmpty()) {
            sql.append(" where 1=1 ");
            for(Map.Entry<String, Object> entry : Collections.unmodifiableMap(getQueryParams()).entrySet()){
                sql.append("and ").append(entry.getKey()).append("='").append(entry.getValue()).append("' ");
            }
        }
        if(value != null){
           sql.append("and "+ getValueField()+"='"+getValue()+"' ");
        }
        if(StringUtils.isNoneBlank(getOrderByClause())){
            sql.append("order by "+getOrderByClause());
        }
        return sql.toString();
    }


    @Override
    public String getDisplayText(Object value, Map paramMap, FieldMeta fieldMeta){
        if(value == null || "".equals(value)){
            return "";
        }
        paramMap.put(VALUE_KEY, value);
        buildParam(paramMap);
        List<ValuePair<?>> data = SpringUtil.getBean(CommonMapper.class).selectValuePair(buildSql());
        if(data.isEmpty()) {
            return "";
        }
        return data.get(0).getText();
    }

    public String getTable() {
        return table;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public String getValueField() {
        return valueField;
    }

    public void setValueField(String valueField) {
        this.valueField = valueField;
    }

    public String getTextField() {
        return textField;
    }

    public void setTextField(String textField) {
        this.textField = textField;
    }

    public Map<String, Object> getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(Map<String, Object> queryParams) {
        this.queryParams = queryParams;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }

    public String getOrderByClause() {
        return orderByClause;
    }

    public void setOrderByClause(String orderByClause) {
        this.orderByClause = orderByClause;
    }
}
