package com.sa.dao.provider;

import com.sa.dto.IMybatisForceParams;
import org.apache.commons.lang3.StringUtils;
import org.apache.ibatis.mapping.MappedStatement;
import tk.mybatis.mapper.annotation.Version;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.EntityHelper;
import tk.mybatis.mapper.mapperhelper.MapperHelper;
import tk.mybatis.mapper.mapperhelper.MapperTemplate;
import tk.mybatis.mapper.mapperhelper.SqlHelper;
import tk.mybatis.mapper.version.VersionException;

import java.util.Set;


public class ExactUpdateProvider extends MapperTemplate {

    public ExactUpdateProvider(Class<?> mapperClass, MapperHelper mapperHelper) {
        super(mapperClass, mapperHelper);
    }


    public String updateByExampleExact(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        if(isCheckExampleEntityClass()){
            sql.append(SqlHelper.exampleCheck(entityClass));
        }


        if (getConfig().isSafeUpdate()) {
            sql.append(SqlHelper.exampleHasAtLeastOneCriteriaCheck("example"));
        }
        sql.append(SqlHelper.updateTable(entityClass, tableName(entityClass), "example"));
        sql.append(updateSetColumns(entityClass, "record", true, isNotEmpty()));
        sql.append(SqlHelper.updateByExampleWhereClause());
        return sql.toString();
    }


    public String updateByPrimaryKeyExact(MappedStatement ms) {
        Class<?> entityClass = getEntityClass(ms);
        StringBuilder sql = new StringBuilder();
        sql.append(SqlHelper.updateTable(entityClass, tableName(entityClass)));
        sql.append(updateSetColumns(entityClass, null, true, isNotEmpty()));
        sql.append(SqlHelper.wherePKColumns(entityClass, true));
        return sql.toString();
    }




    private String updateSetColumns(Class<?> entityClass, String entityName, boolean notNull, boolean notEmpty) {
        StringBuilder sql = new StringBuilder();
        sql.append("<set>");

        Set<EntityColumn> columnSet = EntityHelper.getColumns(entityClass);

        EntityColumn versionColumn = null;

        for (EntityColumn column : columnSet) {
            if (column.getEntityField().isAnnotationPresent(Version.class)) {
                if (versionColumn != null) {
                    throw new VersionException(entityClass.getCanonicalName() + " 中包含多个带有 @Version 注解的字段，一个类中只能存在一个带有 @Version 注解的字段!");
                }
                versionColumn = column;
            }
            if (!column.isId() && column.isUpdatable()) {
                if (column == versionColumn) {
                    Version version = versionColumn.getEntityField().getAnnotation(Version.class);
                    String versionClass = version.nextVersion().getCanonicalName();

                    sql.append(column.getColumn())
                            .append(" = ${@tk.mybatis.mapper.version.VersionUtil@nextVersion(")
                            .append("@").append(versionClass).append("@class, ")
                            .append(column.getProperty()).append(")},");
                } else if (notNull) {
                    sql.append(SqlHelper.getIfNotNull(entityName, column, column.getColumnEqualsHolder(entityName) + ",", notEmpty));
                } else {
                    sql.append(column.getColumnEqualsHolder(entityName) + ",");
                }
            } else if(column.isId() && column.isUpdatable()){

                sql.append(column.getColumn()).append(" = ").append(column.getColumn()).append(",");
            }
        }
        sql.append(buildSetForceParams(entityClass, entityName));
        sql.append("</set>");
        return sql.toString();
    }





    private String buildSetForceParams(Class<?> entityClass, String entityName) {
        if(!IMybatisForceParams.class.isAssignableFrom(entityClass)){
            return "";
        }
        StringBuilder sql = new StringBuilder();
        entityName = StringUtils.isBlank(entityName) ? "" : entityName+".";
        sql.append("<foreach collection=\""+entityName+"setForceParams\" item=\"value\" index=\"key\" separator=\",\">");
        sql.append("${key} = #{value}");
        sql.append("</foreach>");
        return sql.toString();
    }

}