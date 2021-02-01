package com.sa.dao.util;

import com.sa.dao.ExampleExpand;
import com.sa.dto.DTOUtils;
import org.apache.commons.lang3.StringUtils;
import tk.mybatis.mapper.MapperException;
import tk.mybatis.mapper.entity.EntityColumn;
import tk.mybatis.mapper.mapperhelper.EntityHelper;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;


public abstract class OGNL {
    public static final String SAFE_DELETE_ERROR = "通用 Mapper 安全检查: 对查询条件参数进行检查时出错!";
    public static final String SAFE_DELETE_EXCEPTION = "通用 Mapper 安全检查: 当前操作的方法没有指定查询条件，不允许执行该操作!";

    public OGNL() {
    }


    public static boolean hasWhereSuffixSql(Object parameter) {
        if(parameter != null && parameter instanceof ExampleExpand) {
            ExampleExpand example = (ExampleExpand)parameter;
            if(StringUtils.isNotBlank(example.getWhereSuffixSql())) {
                return true;
            }
        }
        return false;
    }


    public static boolean notAllNullParameterCheck(Object parameter, String fields) {
        if (parameter != null) {
            try {
                Set<EntityColumn> columns = EntityHelper.getColumns(DTOUtils.getDTOClass(parameter));
                Set<String> fieldSet = new HashSet<String>(Arrays.asList(fields.split(",")));
                for (EntityColumn column : columns) {
                    if (fieldSet.contains(column.getProperty())) {
                        Object value = column.getEntityField().getValue(parameter);
                        if (value != null) {
                            return true;
                        }
                    }
                }
            } catch (Exception e) {
                throw new MapperException(SAFE_DELETE_ERROR, e);
            }
        }
        throw new MapperException(SAFE_DELETE_EXCEPTION);
    }
}