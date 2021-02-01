package com.sa.dao.provider;

import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;

import java.util.Set;


public class ExpandSelectProvider extends MapperTemplate {

    public ExpandSelectProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }


    public String selectOneExpand(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);

        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(selectColumns(entityClass));
        sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.whereAllIfColumns(entityClass, isNotEmpty()));
        sql.append(whereSuffixSql(false));
        return sql.toString();
    }


    public String selectExpand(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);

        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(selectColumns(entityClass));
        sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.whereAllIfColumns(entityClass, isNotEmpty()));
        sql.append(whereSuffixSql(false));
        sql.append(SqlHelper.orderByDefault(entityClass));
        return sql.toString();
    }


    public String selectByRowBoundsExpand(MappedStatement ms) {
        return selectExpand(ms);
    }


    public String selectByPrimaryKeyExpand(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);

        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(selectColumns(entityClass));
        sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.wherePKColumns(entityClass));
        sql.append(whereSuffixSql(false));
        return sql.toString();
    }


    public String selectAllExpand(MappedStatement ms) {
        final Class<?> entityClass = getEntityClass(ms);

        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder();
        sql.append(selectColumns(entityClass));
        sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.orderByDefault(entityClass));
        return sql.toString();
    }



    public String selectByExampleExpand(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);

        setResultType(ms, entityClass);
        StringBuilder sql = new StringBuilder("SELECT ");
        if (isCheckExampleEntityClass()) {
            sql.append(SqlHelper.exampleCheck(entityClass));
        }
        sql.append("<if test=\"distinct\">distinct</if>");

        sql.append(SqlHelper.exampleSelectColumns(entityClass));
        sql.append(SqlHelper.fromTable(entityClass, tableName(entityClass)));
        sql.append(SqlHelper.exampleWhereClause());
        sql.append(whereSuffixSql(true));
        sql.append(SqlHelper.exampleOrderBy(entityClass));
        sql.append(SqlHelper.exampleForUpdate());
        return sql.toString();
    }


    public String selectOneByExampleExpand(MappedStatement ms) {
        return selectByExampleExpand(ms);
    }




    private String whereSuffixSql(boolean isExample){
        StringBuilder sql = new StringBuilder(64);





        if(isExample){

            sql.append("<if test=\"@com.sa.dao.util.OGNL@hasWhereSuffixSql(_parameter)\">");
            sql.append("${whereSuffixSql}");
        }else {
            sql.append("<if test=\"whereSuffixSql != null and whereSuffixSql != ''\">");
            sql.append("${whereSuffixSql}");
        }

        sql.append("</if>");
        return sql.toString();
    }


    private String selectColumns(Class<?> entityClass) {
        StringBuilder sql = new StringBuilder();
        sql.append("SELECT ");







        sql.append("<choose>");
        sql.append("<when test=\"@tk.mybatis.mapper.util.OGNL@hasSelectColumns(selectColumns)\">");
        sql.append("<foreach collection=\"selectColumns\" item=\"selectColumn\" separator=\",\">");
        sql.append("${selectColumn}");
        sql.append("</foreach>");
        sql.append("</when>");
        sql.append("<otherwise>");
        sql.append(getAllColumns(entityClass));
        sql.append("</otherwise>");
        sql.append("</choose>");
        return sql.toString();
    }


    private String getAllColumns(Class<?> entityClass) {
        Set<EntityColumn> columnSet = EntityHelper.getColumns(entityClass);
        StringBuilder sql = new StringBuilder();
        for (EntityColumn entityColumn : columnSet) {
            sql.append(entityColumn.getColumn()).append(",");
        }
        return sql.substring(0, sql.length() - 1);
    }

}