package com.sa.dao.util;


import com.sa.dto.DTOUtils;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.mapping.BoundSql;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.mapping.ParameterMapping;
import org.apache.ibatis.mapping.ParameterMode;
import org.apache.ibatis.reflection.MetaObject;
import org.apache.ibatis.reflection.SystemMetaObject;
import org.apache.ibatis.reflection.factory.DefaultObjectFactory;
import org.apache.ibatis.reflection.factory.ObjectFactory;
import org.apache.ibatis.reflection.wrapper.DefaultObjectWrapperFactory;
import org.apache.ibatis.reflection.wrapper.ObjectWrapperFactory;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.ResultHandler;
import org.apache.ibatis.session.RowBounds;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.type.JdbcType;
import org.apache.ibatis.type.TypeHandlerRegistry;
import tk.mybatis.mapper.entity.EntityColumn;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class SsSqlHelper {
    private static final ObjectFactory DEFAULT_OBJECT_FACTORY = new DefaultObjectFactory();
    private static final ObjectWrapperFactory DEFAULT_OBJECT_WRAPPER_FACTORY = new DefaultObjectWrapperFactory();


    public static String getMapperSql(Object mapper, String methodName, Object... args) {
        MetaObject metaObject = SystemMetaObject.forObject(mapper);
        SqlSession session = (SqlSession) metaObject.getValue("h.sqlSession");
        Class mapperInterface = (Class) metaObject.getValue("h.mapperInterface");
        String fullMethodName = mapperInterface.getCanonicalName() + "." + methodName;
        if (args == null || args.length == 0) {
            return getNamespaceSql(session, fullMethodName, null);
        } else {
            return getMapperSql(session, mapperInterface, methodName, args);
        }
    }


    public static String getMapperSql(SqlSession session, String fullMapperMethodName, Object... args) {
        if (args == null || args.length == 0) {
            return getNamespaceSql(session, fullMapperMethodName, null);
        }
        String methodName = fullMapperMethodName.substring(fullMapperMethodName.lastIndexOf('.') + 1);
        Class mapperInterface = null;
        try {
            mapperInterface = Class.forName(fullMapperMethodName.substring(0, fullMapperMethodName.lastIndexOf('.')));
            return getMapperSql(session, mapperInterface, methodName, args);
        } catch (ClassNotFoundException e) {
            throw new IllegalArgumentException("参数" + fullMapperMethodName + "无效！");
        }
    }


    public static String getMapperSql(SqlSession session, Class mapperInterface, String methodName, Object... args) {
        String fullMapperMethodName = mapperInterface.getCanonicalName() + "." + methodName;
        if (args == null || args.length == 0) {
            return getNamespaceSql(session, fullMapperMethodName, null);
        }
        Method method = getDeclaredMethods(mapperInterface, methodName);
        Map params = new HashMap();
        final Class<?>[] argTypes = method.getParameterTypes();
        for (int i = 0; i < argTypes.length; i++) {
            if (!RowBounds.class.isAssignableFrom(argTypes[i]) && !ResultHandler.class.isAssignableFrom(argTypes[i])) {
                String paramName = "param" + String.valueOf(params.size() + 1);
                paramName = getParamNameFromAnnotation(method, i, paramName);
                params.put(paramName, i >= args.length ? null : args[i]);
            }
        }
        if (args != null && args.length == 1) {
            Object _params = wrapCollection(args[0]);
            if (_params instanceof Map) {
                params.putAll((Map) _params);
            }else if(DTOUtils.isProxy(_params)){
                params.putAll(DTOUtils.go(_params));
            }else if(null != args[0]){
                return getNamespaceSql(session, fullMapperMethodName, args[0]);
            }
        }
        return getNamespaceSql(session, fullMapperMethodName, params);
    }



    public static String getNamespaceSql(SqlSession session, String namespace) {
        return getNamespaceSql(session, namespace, null);
    }


    public static String getNamespaceSql(SqlSession session, String namespace, Object params) {
        params = wrapCollection(params);
        Configuration configuration = session.getConfiguration();
        MappedStatement mappedStatement = configuration.getMappedStatement(namespace);
        TypeHandlerRegistry typeHandlerRegistry = mappedStatement.getConfiguration().getTypeHandlerRegistry();
        BoundSql boundSql = mappedStatement.getBoundSql(params);
        List<ParameterMapping> parameterMappings = boundSql.getParameterMappings();
        String sql = boundSql.getSql();
        if (parameterMappings != null) {
            for (int i = 0; i < parameterMappings.size(); i++) {
                ParameterMapping parameterMapping = parameterMappings.get(i);
                if (parameterMapping.getMode() != ParameterMode.OUT) {
                    Object value;
                    String propertyName = parameterMapping.getProperty();
                    if (boundSql.hasAdditionalParameter(propertyName)) {
                        value = boundSql.getAdditionalParameter(propertyName);
                    } else if (params == null) {
                        value = null;
                    } else if (typeHandlerRegistry.hasTypeHandler(params.getClass())) {
                        value = params;
                    } else {
                        MetaObject metaObject = configuration.newMetaObject(params);
                        value = metaObject.getValue(propertyName);
                    }
                    JdbcType jdbcType = parameterMapping.getJdbcType();
                    if (value == null && jdbcType == null) {
                        jdbcType = configuration.getJdbcTypeForNull();
                    }
                    sql = replaceParameter(sql, value, jdbcType, parameterMapping.getJavaType());
                }
            }
        }
        return sql;
    }


    public static String notAllNullParameterCheck(String parameterName, Set<EntityColumn> columnSet) {
        StringBuilder sql = new StringBuilder();
        sql.append("<bind name=\"notAllNullParameterCheck\" value=\"@com.sa.dao.util.OGNL@notAllNullParameterCheck(");
        sql.append(parameterName).append(", '");
        StringBuilder fields = new StringBuilder();
        for (EntityColumn column : columnSet) {
            if(fields.length() > 0){
                fields.append(",");
            }
            fields.append(column.getProperty());
        }
        sql.append(fields);
        sql.append("')\"/>");
        return sql.toString();
    }


    private static String replaceParameter(String sql, Object value, JdbcType jdbcType, Class javaType) {
        String strValue = String.valueOf(value);
        if (jdbcType != null) {
            switch (jdbcType) {

                case BIT:
                case TINYINT:
                case SMALLINT:
                case INTEGER:
                case BIGINT:
                case FLOAT:
                case REAL:
                case DOUBLE:
                case NUMERIC:
                case DECIMAL:
                    break;

                case DATE:
                case TIME:
                case TIMESTAMP:

                default:
                    strValue = "'" + strValue + "'";


            }
        } else if (Number.class.isAssignableFrom(javaType)) {

        } else {
            strValue = "'" + strValue + "'";
        }
        return sql.replaceFirst("\\?", strValue);
    }


    private static Method getDeclaredMethods(Class clazz, String methodName) {
        Method[] methods = clazz.getDeclaredMethods();
        for (Method method : methods) {
            if (method.getName().equals(methodName)) {
                return method;
            }
        }
        throw new IllegalArgumentException("方法" + methodName + "不存在！");
    }


    private static String getParamNameFromAnnotation(Method method, int i, String paramName) {
        final Object[] paramAnnos = method.getParameterAnnotations()[i];
        for (Object paramAnno : paramAnnos) {
            if (paramAnno instanceof Param) {
                paramName = ((Param) paramAnno).value();
            }
        }
        return paramName;
    }


    private static Object wrapCollection(final Object object) {
        if (object instanceof List) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("list", object);
            return map;
        } else if (object != null && object.getClass().isArray()) {
            Map<String, Object> map = new HashMap<String, Object>();
            map.put("array", object);
            return map;
        }
        return object;
    }
}