package com.sa.dao.provider;

import com.sa.dto.IMybatisForceParams;
import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.MapperException;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.*;

import java.util.Set;


public class ExactInsertProvider extends MapperTemplate {

    public ExactInsertProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }

    
    public String insertExact(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();

        Set<EntityColumn> columnList = EntityHelper.getColumns(entityClass);
        processKey(sql, entityClass, ms, columnList);
        sql.append(SqlHelper.insertIntoTable(entityClass, tableName(entityClass)));
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }
            if (column.isIdentity()) {
                sql.append(column.getColumn() + ",");
            } else {
                sql.append(SqlHelper.getIfNotNull(column, column.getColumn() + ",", isNotEmpty()));
            }
        }
        sql.append(buildInsertForceParams(entityClass));
        sql.append("</trim>");
        sql.append("<trim prefix=\"VALUES(\" suffix=\")\" suffixOverrides=\",\">");
        for (EntityColumn column : columnList) {
            if (!column.isInsertable()) {
                continue;
            }


            if (column.isIdentity()) {
                sql.append(SqlHelper.getIfCacheNotNull(column, column.getColumnHolder(null, "_cache", ",")));
            } else {

                sql.append(SqlHelper.getIfNotNull(column, column.getColumnHolder(null, null, ","), isNotEmpty()));
            }


            if (column.isIdentity()) {
                sql.append(SqlHelper.getIfCacheIsNull(column, column.getColumnHolder() + ","));
            }
        }
        sql.append(buildValuesForceParams(entityClass));
        sql.append("</trim>");
        return sql.toString();
    }



    
    private void processKey(StringBuilder sql, Class<?> entityClass, MappedStatement ms, Set<EntityColumn> columnList){

        Boolean hasIdentityKey = false;

        for (EntityColumn column : columnList) {
            if (column.isIdentity()) {


                sql.append(SqlHelper.getBindCache(column));


                if (hasIdentityKey) {

                    if (column.getGenerator() != null && "JDBC".equals(column.getGenerator())) {
                        continue;
                    }
                    throw new MapperException(ms.getId() + "对应的实体类" + entityClass.getCanonicalName() + "中包含多个MySql的自动增长列,最多只能有一个!");
                }

                SelectKeyHelper.newSelectKeyMappedStatement(ms, column, entityClass, isBEFORE(), getIDENTITY(column));
                hasIdentityKey = true;
            } else if(column.getGenIdClass() != null){
                sql.append("<bind name=\"").append(column.getColumn()).append("GenIdBind\" value=\"@tk.mybatis.mapper.genid.GenIdUtil@genId(");
                sql.append("_parameter").append(", '").append(column.getProperty()).append("'");
                sql.append(", @").append(column.getGenIdClass().getCanonicalName()).append("@class");
                sql.append(", '").append(tableName(entityClass)).append("'");
                sql.append(", '").append(column.getColumn()).append("')");
                sql.append("\"/>");
            }
        }
    }

    
    private String insertColumns(Class<?> entityClass, boolean skipId, boolean notNull, boolean notEmpty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<trim prefix=\"(\" suffix=\")\" suffixOverrides=\",\">");

        Set<EntityColumn> columnSet = EntityHelper.getColumns(entityClass);

        for (EntityColumn column : columnSet) {
            if (!column.isInsertable()) {
                continue;
            }
            if (skipId && column.isId()) {
                continue;
            }
            if (notNull) {
                sql.append(SqlHelper.getIfNotNull(column, column.getColumn() + ",", notEmpty));
            } else {
                sql.append(column.getColumn() + ",");
            }
        }
        sql.append(buildInsertForceParams(entityClass));
        sql.append("</trim>");
        return sql.toString();
    }

    
    private String buildInsertForceParams(Class<?> entityClass) {
        if(!IMybatisForceParams.class.isAssignableFrom(entityClass)){
            return "";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("<foreach collection=\"insertForceParams\" item=\"value\" index=\"key\" separator=\",\">");
        sql.append("${key}");
        sql.append("</foreach>");
        return sql.toString();
    }

    
    private String buildValuesForceParams(Class<?> entityClass) {
        if(!IMybatisForceParams.class.isAssignableFrom(entityClass)){
            return "";
        }
        StringBuilder sql = new StringBuilder();
        sql.append("<foreach collection=\"insertForceParams\" item=\"value\" index=\"key\" separator=\",\">");
        sql.append("#{value}");
        sql.append("</foreach>");
        return sql.toString();
    }

}