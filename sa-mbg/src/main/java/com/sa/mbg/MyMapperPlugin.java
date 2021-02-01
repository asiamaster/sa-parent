package com.sa.mbg;

import com.alibaba.fastjson.JSONObject;
import com.sa.mbg.beetl.BeetlTemplateUtil;
import com.sa.util.POJOUtils;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.*;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.api.dom.xml.Attribute;
import org.mybatis.generator.api.dom.xml.XmlElement;
import org.mybatis.generator.config.CommentGeneratorConfiguration;
import org.mybatis.generator.config.Context;
import org.mybatis.generator.internal.DefaultShellCallback;
import org.mybatis.generator.internal.util.StringUtility;
import tk.mybatis.mapper.generator.MapperCommentGenerator;

import java.util.*;


public class MyMapperPlugin extends PluginAdapter {

	private Set<String> mappers = new HashSet<String>();
	private boolean caseSensitive = false;

	private String beginningDelimiter = "";

	private String endingDelimiter = "";

	private Boolean isDTO = false;

	private String schema;

	private CommentGeneratorConfiguration commentCfg;

	public MyMapperPlugin() {
	}

	@Override
	public void setContext(Context context) {
		super.setContext(context);

		commentCfg = new CommentGeneratorConfiguration();
		commentCfg.setConfigurationType(MapperCommentGenerator.class.getCanonicalName());
		context.setCommentGeneratorConfiguration(commentCfg);

		context.getJdbcConnectionConfiguration().addProperty("remarksReporting", "true");
	}

	@Override
	public void setProperties(Properties properties) {
		super.setProperties(properties);
		String mappers = this.properties.getProperty("mappers");
		if (StringUtility.stringHasValue(mappers)) {
			for (String mapper : mappers.split(",")) {
				this.mappers.add(mapper);
			}
		} else {
			throw new RuntimeException("Mapper插件缺少必要的mappers属性!");
		}
		String caseSensitive = this.properties.getProperty("caseSensitive");
		if (StringUtility.stringHasValue(caseSensitive)) {
			this.caseSensitive = "TRUE".equalsIgnoreCase(caseSensitive);
		}
		String isDTO = this.properties.getProperty("isDTO", "false");
		if (StringUtility.stringHasValue(isDTO)) {
			this.isDTO = "TRUE".equalsIgnoreCase(isDTO);
		}
		String beginningDelimiter = this.properties.getProperty("beginningDelimiter");
		if (StringUtility.stringHasValue(beginningDelimiter)) {
			this.beginningDelimiter = beginningDelimiter;
		}
		commentCfg.addProperty("beginningDelimiter", this.beginningDelimiter);
		String endingDelimiter = this.properties.getProperty("endingDelimiter");
		if (StringUtility.stringHasValue(endingDelimiter)) {
			this.endingDelimiter = endingDelimiter;
		}
		commentCfg.addProperty("endingDelimiter", this.endingDelimiter);
		String schema = this.properties.getProperty("schema");
		if (StringUtility.stringHasValue(schema)) {
			this.schema = schema;
		}
	}

	public String getDelimiterName(String name) {
		StringBuilder nameBuilder = new StringBuilder();
		if (StringUtility.stringHasValue(schema)) {
			nameBuilder.append(schema);
			nameBuilder.append(".");
		}
		nameBuilder.append(beginningDelimiter);
		nameBuilder.append(name);
		nameBuilder.append(endingDelimiter);
		return nameBuilder.toString();
	}

	@Override
	public boolean validate(List<String> warnings) {
		return true;
	}

	
	@Override
	public boolean clientGenerated(Interface interfaze, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

		FullyQualifiedJavaType entityType = new FullyQualifiedJavaType(introspectedTable.getBaseRecordType());

		for (String mapper : mappers) {
			interfaze.addImportedType(new FullyQualifiedJavaType(mapper));
			interfaze.addSuperInterface(new FullyQualifiedJavaType(mapper + "<" + entityType.getShortName() + ">"));
		}

		interfaze.addImportedType(entityType);
		return true;
	}

	
	private void processEntityClass(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

		FullyQualifiedJavaType superClassType = new FullyQualifiedJavaType("BaseDomain");
		topLevelClass.setSuperClass(superClassType);

		topLevelClass.addImportedType("javax.persistence.*");
		topLevelClass.addImportedType("com.sa.domain.BaseDomain");

		String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();

		if (StringUtility.stringContainsSpace(tableName)) {
			tableName = context.getBeginningDelimiter()
					+ tableName
					+ context.getEndingDelimiter();
		}

		topLevelClass.addJavaDocLine("");

		if (caseSensitive && !topLevelClass.getType().getShortName().equals(tableName)) {
			topLevelClass.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
		} else if (!topLevelClass.getType().getShortName().equalsIgnoreCase(tableName)) {
			topLevelClass.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
		} else if (StringUtility.stringHasValue(schema)
				|| StringUtility.stringHasValue(beginningDelimiter)
				|| StringUtility.stringHasValue(endingDelimiter)) {
			topLevelClass.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
		}
		processEntityGetMethodAnnotation(topLevelClass, introspectedTable);
	}

	
	private void processEntityGetMethodAnnotation(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		topLevelClass.addImportedType("com.sa.metadata.annotation.FieldDef");
		topLevelClass.addImportedType("com.sa.metadata.annotation.EditMode");
		topLevelClass.addImportedType("com.sa.metadata.FieldEditor");
		List<Method> methods = topLevelClass.getMethods();

		for (Method method : methods) {
			if (POJOUtils.isGetMethod(method.getName())) {
				IntrospectedColumn introspectedColumn = introspectedTable.getColumn(POJOUtils.humpToLineFast(POJOUtils.getBeanField(method.getName())));
				String fieldLabel = StringUtils.isBlank(introspectedColumn.getRemarks()) ? introspectedColumn.getJavaProperty() : BeetlTemplateUtil.getFieldName(introspectedColumn.getRemarks());
				if ("VARCHAR".equals(introspectedColumn.getJdbcTypeName())) {
					method.addAnnotation("@FieldDef(label=\"" + fieldLabel + "\", maxLength = " + introspectedColumn.getLength() + ")");
				} else {
					method.addAnnotation("@FieldDef(label=\"" + fieldLabel + "\")");
				}
				JSONObject jsonObject = BeetlTemplateUtil.getJsonObject(introspectedColumn.getRemarks());

				if (jsonObject != null) {
					method.addAnnotation("@EditMode(editor = FieldEditor.Combo, required = " + !introspectedColumn.isNullable() + ", params=\"" + jsonObject.toJSONString().replaceAll("\"", "\\\\\"") + "\")");
				} else {
					if ("TIMESTAMP".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Datetime, required = " + !introspectedColumn.isNullable() + ")");
					} else if ("DATE".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Date, required = " + !introspectedColumn.isNullable() + ")");
					} else if ("INTEGER".equals(introspectedColumn.getJdbcTypeName()) || "BIGINT".equals(introspectedColumn.getJdbcTypeName()) || "BIT".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Number, required = " + !introspectedColumn.isNullable() + ")");
					} else {
						method.addAnnotation("@EditMode(editor = FieldEditor.Text, required = " + !introspectedColumn.isNullable() + ")");
					}
				}
			}
		}
	}

	
	@Override
	public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(
			IntrospectedTable introspectedTable) {
		if(!isDTO) {
			return null;
		}
		String targetPackage = this.getContext().getJavaModelGeneratorConfiguration().getTargetPackage();
		String targetProject = this.getContext().getJavaModelGeneratorConfiguration().getTargetProject();
		ShellCallback shellCallback = new DefaultShellCallback(false);
		JavaFormatter javaFormatter = context.getJavaFormatter();
		List<GeneratedJavaFile> mapperJavaFiles = new ArrayList<GeneratedJavaFile>();
		String shortName = introspectedTable.getFullyQualifiedTable().getDomainObjectName();
		GeneratedJavaFile dtoJavafile = null;
		String dtoSuperInterface = "com.sa.dto.IBaseDomain";
		if (StringUtility.stringHasValue(targetPackage)) {
			Interface dtoInterface = new Interface(targetPackage + "." + shortName);
			dtoInterface.setVisibility(JavaVisibility.PUBLIC);

			FullyQualifiedJavaType dtoSuperType = new FullyQualifiedJavaType(dtoSuperInterface);
			dtoInterface.addImportedType(dtoSuperType);
			dtoInterface.addSuperInterface(dtoSuperType);

			dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.*"));


			dtoInterface.addJavaDocLine("");


			String tableName = introspectedTable.getFullyQualifiedTableNameAtRuntime();

			if (StringUtility.stringContainsSpace(tableName)) {
				tableName = context.getBeginningDelimiter()
						+ tableName
						+ context.getEndingDelimiter();
			}

			if (caseSensitive && !dtoInterface.getType().getShortName().equals(tableName)) {
				dtoInterface.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
			} else if (!dtoInterface.getType().getShortName().equalsIgnoreCase(tableName)) {
				dtoInterface.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
			} else if (StringUtility.stringHasValue(schema)
					|| StringUtility.stringHasValue(beginningDelimiter)
					|| StringUtility.stringHasValue(endingDelimiter)) {
				dtoInterface.addAnnotation("@Table(name = \"" + getDelimiterName(tableName) + "\")");
			}

			generateMethods(dtoInterface, introspectedTable);



			dtoJavafile = new GeneratedJavaFile(dtoInterface, targetProject, javaFormatter);





					mapperJavaFiles.add(dtoJavafile);




		}
		return mapperJavaFiles;
	}

	
	private void generateMethods(Interface dtoInterface, IntrospectedTable introspectedTable){

		List<FullyQualifiedJavaType> buffer = new ArrayList<>();
		for(IntrospectedColumn column : introspectedTable.getAllColumns()){
			FullyQualifiedJavaType fullyQualifiedJavaType = column.getFullyQualifiedJavaType();

			if(fullyQualifiedJavaType.isExplicitlyImported() && !buffer.contains(fullyQualifiedJavaType)){
				buffer.add(fullyQualifiedJavaType);
			}
			String property = column.getJavaProperty();
			String upperFirstProperty = Character.toUpperCase(property.charAt(0)) + property.substring(1);
			Method getMethod = new Method("get"+upperFirstProperty);
			getMethod.setReturnType(fullyQualifiedJavaType);
			dtoInterface.addMethod(getMethod);

			Method setMethod = new Method("set"+upperFirstProperty);
			Parameter setParameter = new Parameter(fullyQualifiedJavaType, StringUtils.uncapitalize(property));
			setMethod.addParameter(0, setParameter);
			dtoInterface.addMethod(setMethod);
		}

		for(FullyQualifiedJavaType fullyQualifiedJavaType : buffer) {
			dtoInterface.addImportedType(fullyQualifiedJavaType);
		}

		processDTOGetMethodAnnotation(dtoInterface, introspectedTable);
	}

	
	private void processDTOGetMethodAnnotation(Interface dtoInterface, IntrospectedTable introspectedTable) {
		dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.Column"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.GeneratedValue"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.GenerationType"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("javax.persistence.Id"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("com.sa.metadata.FieldEditor"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("com.sa.metadata.annotation.FieldDef"));
		dtoInterface.addImportedType(new FullyQualifiedJavaType("com.sa.metadata.annotation.EditMode"));

		List<Method> methods = dtoInterface.getMethods();
		for (Method method : methods) {
			if (POJOUtils.isGetMethod(method.getName())) {
				IntrospectedColumn introspectedColumn = introspectedTable.getColumn(POJOUtils.humpToLineFast(POJOUtils.getBeanField(method.getName())));
				if(introspectedColumn == null){
					continue;
				}
				String fieldLabel = StringUtils.isBlank(introspectedColumn.getRemarks()) ? introspectedColumn.getJavaProperty() : BeetlTemplateUtil.getFieldName(introspectedColumn.getRemarks());

				if(introspectedColumn.isIdentity()) {
					method.addAnnotation("@Id");
					method.addAnnotation("@GeneratedValue(strategy = GenerationType.IDENTITY)");
				}

				method.addAnnotation("@Column(name = \""+beginningDelimiter+introspectedColumn.getActualColumnName()+beginningDelimiter+"\")");

				if ("VARCHAR".equals(introspectedColumn.getJdbcTypeName())) {
					method.addAnnotation("@FieldDef(label=\"" + fieldLabel + "\", maxLength = " + introspectedColumn.getLength() + ")");
				} else {
					method.addAnnotation("@FieldDef(label=\"" + fieldLabel + "\")");
				}

				JSONObject jsonObject = BeetlTemplateUtil.getJsonObject(introspectedColumn.getRemarks());

				if (jsonObject != null) {
					method.addAnnotation("@EditMode(editor = FieldEditor.Combo, required = " + !introspectedColumn.isNullable() + ", params=\"" + jsonObject.toJSONString().replaceAll("\"", "\\\\\"") + "\")");
				} else {
					if ("TIMESTAMP".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Datetime, required = " + !introspectedColumn.isNullable() + ")");
					} else if ("DATE".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Date, required = " + !introspectedColumn.isNullable() + ")");
					} else if ("INTEGER".equals(introspectedColumn.getJdbcTypeName()) || "BIGINT".equals(introspectedColumn.getJdbcTypeName()) || "BIT".equals(introspectedColumn.getJdbcTypeName())) {
						method.addAnnotation("@EditMode(editor = FieldEditor.Number, required = " + !introspectedColumn.isNullable() + ")");
					} else {
						method.addAnnotation("@EditMode(editor = FieldEditor.Text, required = " + !introspectedColumn.isNullable() + ")");
					}
				}
			}
		}
	}

	
	@Override
	public boolean modelBaseRecordClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

		if (isDTO) {
			String dtoSuperInterface = "com.sa.dto.IBaseDomain";
			topLevelClass.setVisibility(JavaVisibility.PUBLIC);

			FullyQualifiedJavaType dtoSuperType = new FullyQualifiedJavaType(dtoSuperInterface);

			topLevelClass.addSuperInterface(dtoSuperType);


			return true;
		}
		processEntityClass(topLevelClass, introspectedTable);
		return true;
	}

	
	@Override
	public boolean modelPrimaryKeyClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

		return true;
	}

	
	@Override
	public boolean modelRecordWithBLOBsClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {

		return false;
	}


	@Override
	public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientInsertMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientDeleteByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientInsertMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientInsertSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectAllMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectAllMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByPrimaryKeyMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeySelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByPrimaryKeyWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapDeleteByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapInsertElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapInsertSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectAllElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectByPrimaryKeyElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		updateAttributeId(element, "selectBy");
		return true;
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeySelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		updateAttributeId(element, "updateBy");
		return true;
	}


	private void updateAttributeId(XmlElement element, String updatedId) {
		List<Attribute> attrs = element.getAttributes();
		for (int i = 0; i < attrs.size(); i++) {
			Attribute attr = attrs.get(i);
			if ("id".equals(attr.getName())) {
				attrs.remove(i);
				break;
			}
		}
		element.addAttribute(new Attribute("id", updatedId));
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapUpdateByPrimaryKeyWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean providerGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean providerApplyWhereMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean providerInsertSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean providerUpdateByPrimaryKeySelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapBaseColumnListElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return true;
	}

	@Override
	public boolean sqlMapExampleWhereClauseElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapUpdateByExampleSelectiveElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapUpdateByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapUpdateByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapCountByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapDeleteByExampleElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectByExampleWithoutBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean sqlMapSelectByExampleWithBLOBsElementGenerated(XmlElement element, IntrospectedTable introspectedTable) {
		return false;
	}


	@Override
	public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleSelectiveMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientUpdateByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByExampleWithBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientSelectByExampleWithoutBLOBsMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientCountByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientCountByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientDeleteByExampleMethodGenerated(Method method, Interface interfaze, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean clientDeleteByExampleMethodGenerated(Method method, TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}



	@Override
	public boolean modelExampleClassGenerated(TopLevelClass topLevelClass, IntrospectedTable introspectedTable) {
		return false;
	}

	@Override
	public boolean modelFieldGenerated(Field field,
	                                   TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
	                                   IntrospectedTable introspectedTable,
	                                   ModelClassType modelClassType) {
		return isDTO ? false : true;
	}

	@Override
	public boolean modelGetterMethodGenerated(Method method,
	                                          TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
	                                          IntrospectedTable introspectedTable,
	                                          ModelClassType modelClassType) {
		return isDTO ? false : true;
	}

	@Override
	public boolean modelSetterMethodGenerated(Method method,
	                                          TopLevelClass topLevelClass, IntrospectedColumn introspectedColumn,
	                                          IntrospectedTable introspectedTable,
	                                          ModelClassType modelClassType) {
		return isDTO ? false : true;
	}
}
