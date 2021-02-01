package com.sa.mvc.controller;

import com.alibaba.fastjson.JSONObject;
import com.github.pagehelper.Page;
import com.sa.base.BaseServiceImpl;
import com.sa.constant.SsConstants;
import com.sa.domain.ConditionItems;
import com.sa.domain.EasyuiPageOutput;
import com.sa.glossary.RelationOperator;
import com.sa.metadata.ValueProviderUtils;
import com.sa.service.CommonService;
import com.sa.util.POJOUtils;
import com.sa.util.ReflectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.poi.ss.formula.functions.T;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Column;
import javax.persistence.Table;
import javax.persistence.Transient;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.List;


@Controller
@ConditionalOnBean(name = "commonMapper")
@RequestMapping("/common")
public class CommonController {
    private static final Logger logger = LoggerFactory.getLogger(BaseServiceImpl.class);
    @Autowired
    CommonService commonService;


    @RequestMapping(value="/listEasyuiPageByConditionItems.action", method = {RequestMethod.GET, RequestMethod.POST})
    public @ResponseBody String listEasyuiPageByConditionItems(@ModelAttribute ConditionItems conditionItems) throws Exception {
        if(conditionItems.getConditionRelationField() == null || conditionItems.getConditionItems() == null || conditionItems.getDtoClass() == null) {
            return null;
        }
        Class<?> dtoClass = Class.forName(conditionItems.getDtoClass());
        Table table = dtoClass.getAnnotation(Table.class);

        String tableName = table == null ? POJOUtils.humpToLineFast(dtoClass.getSimpleName()) : table.name();
        StringBuilder stringBuilder = new StringBuilder("select ");
        if(dtoClass.isInterface()){

            for (Method method : ReflectionUtils.getAccessibleMethods(dtoClass)) {
                if(!POJOUtils.isGetMethod(method)){
                    continue;
                }

                Transient aTransient = method.getAnnotation(Transient.class);
                if (aTransient != null) {
                    continue;
                }
                Column column = method.getAnnotation(Column.class);
                String dbFieldName = column == null ? POJOUtils.humpToLineFast(POJOUtils.getBeanField(method)) : column.name();
                stringBuilder.append(dbFieldName).append(" ").append(POJOUtils.getBeanField(method)).append(", ");
            }
        }else {

            for (Field field : ReflectionUtils.getAccessibleFields(dtoClass, true, true)) {

                Transient aTransient = field.getAnnotation(Transient.class);
                if (aTransient != null) {
                    continue;
                }
                Column column = field.getAnnotation(Column.class);
                String dbFieldName = column == null ? POJOUtils.humpToLineFast(field.getName()) : column.name();
                stringBuilder.append(dbFieldName).append(" ").append(field.getName()).append(", ");
            }
        }

        String beforeFromSql = stringBuilder.substring(0, stringBuilder.length()-2);
        stringBuilder = new StringBuilder(beforeFromSql);
        stringBuilder.append(" from ").append(tableName);

        String sqlStart = stringBuilder.toString();

        String sql = null;
        if("none".equals(conditionItems.getConditionRelationField())){
            sql = sqlStart;
        }else {
            stringBuilder = new StringBuilder();
            for (String str : conditionItems.getConditionItems()) {
                if(StringUtils.isBlank(str)){
                    continue;
                }
                String[] condition = str.split(":");
                String conditionField = condition[0];
                String relationField = condition[1];

                String conditionValueField = condition.length<3 ? "" : condition[2].replaceAll(SsConstants.COLON_ENCODE, ":");
                stringBuilder.append(" ").append(conditionItems.getConditionRelationField()).append(" ")
                        .append(conditionField).append(" ").append(RelationOperator.valueOf(relationField).getValue()).append(" ");

                if (relationField.equals(RelationOperator.Match.name()) || relationField.equals(RelationOperator.NotMatch.name())) {
                    stringBuilder.append("'%")
                            .append(conditionValueField)
                            .append("%'");

                } else if (relationField.equals(RelationOperator.Is.name()) || relationField.equals(RelationOperator.IsNot.name())) {
                    stringBuilder.append("null");
                } else {
                    stringBuilder.append("'")
                            .append(conditionValueField)
                            .append("'");
                }
            }


            if(stringBuilder.length()<=0){
                sql = sqlStart;
            }else {
                sql = sqlStart + " where" + stringBuilder.substring(conditionItems.getConditionRelationField().length() + 1);
            }
        }
        logger.info("listEasyuiPageByConditionItems_sql:"+sql);
        Integer page = conditionItems.getPage();
        page = (page == null) ? Integer.valueOf(1) : page;
        Integer rows = conditionItems.getRows() == null ? Integer.valueOf(Integer.MAX_VALUE) : conditionItems.getRows();
        EasyuiPageOutput easyuiPageOutput = new EasyuiPageOutput();
        List<JSONObject> list = commonService.selectJSONObject(sql, page, rows);
        Page<T> pageList = (Page)list;
        ValueProviderUtils.buildDataByProvider(conditionItems.getMetadata(), list);
        return new EasyuiPageOutput(pageList.getTotal(), pageList).toString();
    }




}