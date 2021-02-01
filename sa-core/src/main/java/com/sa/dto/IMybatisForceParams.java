package com.sa.dto;

import javax.persistence.Transient;
import java.util.Map;
import java.util.Set;


public interface IMybatisForceParams extends IDTO {


    @Transient
    Map<String, Object> getSetForceParams();
    void setSetForceParams(Map<String, Object> setForceParams);


    @Transient
    Map<String, Object> getInsertForceParams();
    void setInsertForceParams(Map<String, Object> insertForceParams);


    @Transient
    Set<String> getSelectColumns();
    void setSelectColumns(Set<String> selectColumns);


    @Transient
    String getWhereSuffixSql();
    void setWhereSuffixSql(String whereSuffixSql);


    @Transient
    Boolean getCheckInjection();
    void setCheckInjection(Boolean checkInjection);
}
