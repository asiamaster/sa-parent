package com.sa.mbg;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.google.common.collect.Lists;
import com.sa.mbg.beetl.BeetlTemplateUtil;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.messages.Messages;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;


public class MyProviderPlugin extends PluginAdapter {

    private String targetPackage = null;
    private String targetProject = null;

    private boolean generateOnce = false;
    private String tableName = null;

    public MyProviderPlugin() {
    }
    @Override
    public boolean validate(List<String> warnings) {
        boolean valid = true;
        if(!StringUtility.stringHasValue(this.properties.getProperty("targetProject"))) {
            warnings.add(Messages.getString("ValidationError.18", "MyProviderPlugin", "targetProject"));
            valid = false;
        }
        if(!StringUtility.stringHasValue(this.properties.getProperty("targetPackage"))) {
            warnings.add(Messages.getString("ValidationError.18", "MyProviderPlugin", "targetPackage"));
            valid = false;
        }
	    targetProject = this.properties.getProperty("targetProject");
        targetPackage = this.properties.getProperty("targetPackage");
	    tableName = this.properties.getProperty("tableName");
        return valid;
    }

    @Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        List<GeneratedJavaFile> mapperJavaFiles = new ArrayList<GeneratedJavaFile>();

        if(StringUtils.isNotBlank(tableName) && introspectedTable.getAliasedFullyQualifiedTableNameAtRuntime().equals(tableName.trim())){
			Connection connection = DBHelper.getConnection(getContext());
	        try {
		        PreparedStatement preparedStatement = connection.prepareStatement("select provider_name, `value`, `text`, `type`, `order_number` from "+tableName+" order by provider_name, order_number");

		        ResultSet resultSet = preparedStatement.executeQuery();
		        List<JSONObject> jsonObjectList = Lists.newArrayList();
		        JSONObject jsonObject = new JSONObject();
		        while (resultSet.next()){
		        	String providerName = resultSet.getString("provider_name");
			        JSONObject dataJo = new JSONObject();
			        dataJo.put("value", resultSet.getString("value"));
			        dataJo.put("text", resultSet.getString("text"));

		        	if(jsonObject.containsKey("provider") && jsonObject.get("provider").equals(providerName)){
				        jsonObject.getJSONArray("data").add(dataJo);
			        }else{
				        jsonObject = new JSONObject();
				        jsonObject.put("provider", providerName);
				        JSONArray ja = new JSONArray();
				        ja.add(dataJo);
				        jsonObject.put("data", ja);
				        jsonObjectList.add(jsonObject);
			        }
		        }
		        for(JSONObject jo : jsonObjectList) {
			        generateByJSONObject(jo, mapperJavaFiles);
		        }
	        } catch (SQLException e) {
		        e.printStackTrace();
	        }
        }


        for (IntrospectedColumn column : introspectedTable.getAllColumns()) {

			if(StringUtils.isBlank(column.getRemarks())){
				continue;
			}

	        JSONObject jsonObject = BeetlTemplateUtil.getJsonObject(column.getRemarks());
			generateByJSONObject(jsonObject, mapperJavaFiles);
        }
        return mapperJavaFiles;
    }

    private void generateByJSONObject(JSONObject jsonObject, List<GeneratedJavaFile> mapperJavaFiles) {

	    if(jsonObject == null || !jsonObject.containsKey("provider") || !jsonObject.containsKey("data")){
		    return;
	    }
	    String providerSuperInterface = "com.sa.metadata.ValueProvider";
	    JavaFormatter javaFormatter = context.getJavaFormatter();

	    String provider = jsonObject.getString("provider");

	    JSONArray data = jsonObject.getJSONArray("data");

	    TopLevelClass clazz = new TopLevelClass(targetPackage + "." + StringUtils.capitalize(provider));
	    clazz.setVisibility(JavaVisibility.PUBLIC);

	    addJavaDocLine(clazz);

	    clazz.addAnnotation("@Component");

	    FullyQualifiedJavaType providerSuperInterfaceType = new FullyQualifiedJavaType(providerSuperInterface);
	    clazz.addSuperInterface(providerSuperInterfaceType);

	    addImport(clazz);

	    addFields(clazz);

	    addInitializationBlock(clazz, data);

	    addGetLookupListMethod(clazz);

	    addGetDisplayTextMethod(clazz);

	    GeneratedJavaFile providerJavafile = new GeneratedJavaFile(clazz, targetProject, javaFormatter);
	    mapperJavaFiles.add(providerJavafile);
    }


    private void addInitializationBlock(TopLevelClass clazz, JSONArray data){
	    InitializationBlock initializationBlock = new InitializationBlock(true);
	    initializationBlock.addBodyLine("buffer = new ArrayList<ValuePair<?>>();");
	    for(Object obj : data){
		    JSONObject jo = (JSONObject)obj;
		    String value = jo.get("value") == null ? "" : jo.get("value").toString();
		    initializationBlock.addBodyLine("buffer.add(new ValuePairImpl(\""+jo.get("text")+"\", \""+value+"\"));");
	    }
	    clazz.addInitializationBlock(initializationBlock);
    }

	private void addFields(TopLevelClass clazz){

		Field buffer = new Field();
		buffer.setName("buffer");
		buffer.setType(new FullyQualifiedJavaType("List<ValuePair<?>>"));
		buffer.setFinal(true);
		buffer.setStatic(true);
		buffer.setVisibility(JavaVisibility.PRIVATE);
		clazz.addField(buffer);
	}


    private void addImport(TopLevelClass clazz){
	    clazz.addImportedType(new FullyQualifiedJavaType("java.util.ArrayList"));
	    clazz.addImportedType(new FullyQualifiedJavaType("java.util.List"));
	    clazz.addImportedType(new FullyQualifiedJavaType("java.util.Map"));
	    clazz.addImportedType(new FullyQualifiedJavaType("com.sa.metadata.FieldMeta"));
	    clazz.addImportedType(new FullyQualifiedJavaType("com.sa.metadata.ValuePair"));
	    clazz.addImportedType(new FullyQualifiedJavaType("com.sa.metadata.ValuePairImpl"));
	    clazz.addImportedType(new FullyQualifiedJavaType("com.sa.metadata.ValueProvider"));
	    clazz.addImportedType(new FullyQualifiedJavaType("org.springframework.stereotype.Component"));
    }


    private void addJavaDocLine(TopLevelClass clazz){
        clazz.addJavaDocLine("");
    }


    private void addGetLookupListMethod(TopLevelClass clazz){
        Method getLookupListMethod = new Method("getLookupList");
        getLookupListMethod.setReturnType(new FullyQualifiedJavaType("List<ValuePair<?>>"));
        getLookupListMethod.setVisibility(JavaVisibility.PUBLIC);
	    getLookupListMethod.addAnnotation("@Override");
        getLookupListMethod.addBodyLines(Lists.newArrayList("return buffer;"));
	    Parameter objParameter = new Parameter(new FullyQualifiedJavaType("Object"), "obj");
	    getLookupListMethod.addParameter(objParameter);
	    Parameter metaMapParameter = new Parameter(new FullyQualifiedJavaType("Map"), "metaMap");
	    getLookupListMethod.addParameter(metaMapParameter);
	    Parameter fieldMetaParameter = new Parameter(new FullyQualifiedJavaType("FieldMeta"), "fieldMeta");
	    getLookupListMethod.addParameter(fieldMetaParameter);
        clazz.addMethod(getLookupListMethod);
    }


	private void addGetDisplayTextMethod(TopLevelClass clazz){
		Method getDisplayTextMethod = new Method("getDisplayText");
		getDisplayTextMethod.setReturnType(new FullyQualifiedJavaType("String"));
		getDisplayTextMethod.setVisibility(JavaVisibility.PUBLIC);
		getDisplayTextMethod.addAnnotation("@Override");
		Parameter objParameter = new Parameter(new FullyQualifiedJavaType("Object"), "obj");
		getDisplayTextMethod.addParameter(objParameter);
		Parameter metaMapParameter = new Parameter(new FullyQualifiedJavaType("Map"), "metaMap");
		getDisplayTextMethod.addParameter(metaMapParameter);
		Parameter fieldMetaParameter = new Parameter(new FullyQualifiedJavaType("FieldMeta"), "fieldMeta");
		getDisplayTextMethod.addParameter(fieldMetaParameter);

		String bodyline = "if(obj == null || obj.equals(\"\")) return null;\n" +
				"        for(ValuePair<?> valuePair : buffer){\n" +
				"            if(obj.toString().equals(valuePair.getValue())){\n" +
				"                return valuePair.getText();\n" +
				"            }\n" +
				"        }\n" +
				"        return null;";
		getDisplayTextMethod.addBodyLines(Lists.newArrayList(bodyline));
		clazz.addMethod(getDisplayTextMethod);
	}
}
