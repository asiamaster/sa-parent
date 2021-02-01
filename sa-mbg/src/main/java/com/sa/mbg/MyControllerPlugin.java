package com.sa.mbg;

import com.sa.domain.BaseOutput;
import com.sa.dto.IBaseDomain;
import com.sa.dto.IDTO;
import org.apache.commons.lang3.StringUtils;
import org.mybatis.generator.api.GeneratedJavaFile;
import org.mybatis.generator.api.IntrospectedTable;
import org.mybatis.generator.api.JavaFormatter;
import org.mybatis.generator.api.PluginAdapter;
import org.mybatis.generator.api.dom.java.*;
import org.mybatis.generator.internal.util.StringUtility;
import org.mybatis.generator.internal.util.messages.Messages;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.ui.ModelMap;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Set;


public class MyControllerPlugin extends PluginAdapter {

    private final String LINE_SEPARATOR = System.getProperty("line.separator");

    public MyControllerPlugin() {
    }

    @Override
    public void setProperties(Properties properties) {
        super.setProperties(properties);
    }

    @Override
    public boolean validate(List<String> warnings) {
        boolean valid = true;
        if(!StringUtility.stringHasValue(this.properties.getProperty("targetProject"))) {
            warnings.add(Messages.getString("ValidationError.18", "MyControllerPlugin", "targetProject"));
            valid = false;
        }
        if(!StringUtility.stringHasValue(this.properties.getProperty("targetPackage"))) {
            warnings.add(Messages.getString("ValidationError.18", "MyControllerPlugin", "targetPackage"));
            valid = false;
        }
        return valid;
    }

    @Override
    public List<GeneratedJavaFile> contextGenerateAdditionalJavaFiles(IntrospectedTable introspectedTable) {
        String controllerSuperClass = properties.getProperty("controllerSuperClass");
        String controllerSuperInterface = properties.getProperty("controllerSuperInterface");
        String controllerTargetDir = properties.getProperty("targetProject");
        String controllerTargetPackage = properties.getProperty("targetPackage");

        JavaFormatter javaFormatter = context.getJavaFormatter();
        List<GeneratedJavaFile> mapperJavaFiles = new ArrayList<GeneratedJavaFile>();
        for (GeneratedJavaFile javaFile : introspectedTable.getGeneratedJavaFiles()) {
            CompilationUnit unit = javaFile.getCompilationUnit();

            FullyQualifiedJavaType baseModelJavaType = unit.getType();
            String shortName = baseModelJavaType.getShortName();
            if (shortName.endsWith("Mapper")) {
                continue;
            }
            GeneratedJavaFile controllerJavafile = null;
            if (!shortName.endsWith("Example")) {

                TopLevelClass clazz = new TopLevelClass(controllerTargetPackage + "." + shortName + "Controller");
                clazz.setVisibility(JavaVisibility.PUBLIC);
                addJavaDocLine(clazz);
                addSuperInterface(clazz, controllerSuperInterface);
                addSuperClass(clazz, controllerSuperClass);
                addAutowiredService(clazz, controllerTargetPackage, shortName);




                clazz.addImportedType("org.springframework.stereotype.Controller");
                clazz.addAnnotation("@Controller");
                clazz.addImportedType("org.springframework.web.bind.annotation.RequestMapping");
                clazz.addAnnotation("@RequestMapping(\"/"+StringUtils.uncapitalize(shortName)+"\")");


                clazz.addImportedType(unit.getType());

                addCRUDMethod(clazz, unit);


                controllerJavafile = new GeneratedJavaFile(clazz, controllerTargetDir, javaFormatter);
                mapperJavaFiles.add(controllerJavafile);
            }
        }
        return mapperJavaFiles;
    }

    private void addSuperInterface(TopLevelClass clazz, String controllerSuperInterface){

        if (StringUtility.stringHasValue(controllerSuperInterface)) {

            FullyQualifiedJavaType controllerSuperInterfaceType = new FullyQualifiedJavaType(controllerSuperInterface);

            clazz.addImportedType(controllerSuperInterfaceType);

            clazz.addSuperInterface(controllerSuperInterfaceType);
        }
    }

    private void addSuperClass(TopLevelClass clazz, String controllerSuperClass){

        if (StringUtility.stringHasValue(controllerSuperClass)) {
            FullyQualifiedJavaType controllerSuperType = new FullyQualifiedJavaType(controllerSuperClass);

            clazz.addImportedType(controllerSuperType);
            clazz.setSuperClass(controllerSuperType);
        }
    }

    private void addAutowiredService(TopLevelClass clazz, String controllerTargetPackage, String shortName){
        String servicePackage = controllerTargetPackage.substring(0, controllerTargetPackage.lastIndexOf('.'))+".service";
        String serviceClass = servicePackage+"."+shortName+"Service";
        clazz.addImportedType(serviceClass);
        FullyQualifiedJavaType serviceType = new FullyQualifiedJavaType(serviceClass);
        Field field = new Field(StringUtils.uncapitalize(shortName)+"Service", serviceType);
        field.addAnnotation("@Autowired");
        clazz.addField(field);
        clazz.addImportedType(Autowired.class.getName());
    }

    private void addJavaDocLine(TopLevelClass clazz){
        clazz.addJavaDocLine("");
    }


    private void addCRUDMethod(TopLevelClass clazz, CompilationUnit unit){
        if(!isDTO(unit)){
            clazz.addImportedType(ModelAttribute.class.getName());
        }
        clazz.addImportedType(ModelMap.class.getName());
        clazz.addImportedType(BaseOutput.class.getName());
        clazz.addImportedType(ResponseBody.class.getName());
        clazz.addImportedType(List.class.getName());
        clazz.addImportedType(RequestMethod.class.getName());




        addIndexMethod(clazz, unit);

        addListPageMethod(clazz, unit);
        addInsertMethod(clazz, unit);
        addUpdateMethod(clazz, unit);
        addDeleteMethod(clazz, unit);
    }


    private void addIndexMethod(TopLevelClass clazz, CompilationUnit unit){
        Method listMethod = new Method("index");
        FullyQualifiedJavaType modelMapType = new FullyQualifiedJavaType("org.springframework.ui.ModelMap");
        listMethod.addParameter(0, new Parameter(modelMapType, "modelMap"));
        listMethod.setReturnType(new FullyQualifiedJavaType("java.lang.String"));
        listMethod.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String returnLine = "return \""+StringUtils.uncapitalize(unit.getType().getShortName())+"/index\";";
        bodyLines.add(returnLine);
        listMethod.addBodyLines(bodyLines);


        listMethod.addAnnotation("@RequestMapping(value=\"/index.html\", method = RequestMethod.GET)");

        listMethod.addJavaDocLine("");
        clazz.addMethod(listMethod);
    }


    private void addListMethod(TopLevelClass clazz, CompilationUnit unit){
        Method listMethod = new Method("list");
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Parameter entityParameter = new Parameter(baseModelJavaType, StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        if(!isDTO(unit)){
            entityParameter.addAnnotation("@ModelAttribute");
        }
        listMethod.addParameter(0, entityParameter);
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("@ResponseBody List");
        returnType.addTypeArgument(baseModelJavaType);
        listMethod.setReturnType(returnType);
        listMethod.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();


        String returnLine = "return "+StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.list("+entityParameter.getName()+");";
        bodyLines.add(returnLine);
        listMethod.addBodyLines(bodyLines);







        listMethod.addAnnotation("@RequestMapping(value=\"/list.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        listMethod.addJavaDocLine("");
        clazz.addMethod(listMethod);
    }


    private void addListPageMethod(TopLevelClass clazz, CompilationUnit unit){
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Method listPageMethod = new Method("listPage");
        Parameter entityParameter = new Parameter(baseModelJavaType, StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        if(!isDTO(unit)){
            entityParameter.addAnnotation("@ModelAttribute");
        }
        listPageMethod.addParameter(0, entityParameter);
        FullyQualifiedJavaType exception = new FullyQualifiedJavaType("Exception");
        listPageMethod.addException(exception);
        FullyQualifiedJavaType returnType = new FullyQualifiedJavaType("@ResponseBody String");
        listPageMethod.setReturnType(returnType);
        listPageMethod.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String returnLine = "return "+StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.listEasyuiPageByExample("+entityParameter.getName()+", true).toString();";
        bodyLines.add(returnLine);
        listPageMethod.addBodyLines(bodyLines);







        listPageMethod.addAnnotation("@RequestMapping(value=\"/listPage.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        listPageMethod.addJavaDocLine("");
        clazz.addMethod(listPageMethod);
    }


    private void addInsertMethod(TopLevelClass clazz, CompilationUnit unit){
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Method method = new Method("insert");
        Parameter entityParameter = new Parameter(baseModelJavaType, StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        if(!isDTO(unit)){
            entityParameter.addAnnotation("@ModelAttribute");
        }
        method.addParameter(0, entityParameter);
        method.setReturnType(new FullyQualifiedJavaType("@ResponseBody BaseOutput"));
        method.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String contentLine1 = StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.insertSelective("+entityParameter.getName()+");";
        String returnLine = "return BaseOutput.success(\"新增成功\");";
        bodyLines.add(contentLine1);
        bodyLines.add(returnLine);
        method.addBodyLines(bodyLines);







        method.addAnnotation("@RequestMapping(value=\"/insert.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        method.addJavaDocLine("");
        clazz.addMethod(method);
    }


    private void addUpdateMethod(TopLevelClass clazz, CompilationUnit unit){
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Method method = new Method("update");
        Parameter entityParameter = new Parameter(baseModelJavaType, StringUtils.uncapitalize(baseModelJavaType.getShortName()));
        if(!isDTO(unit)){
            entityParameter.addAnnotation("@ModelAttribute");
        }
        method.addParameter(0, entityParameter);
        method.setReturnType(new FullyQualifiedJavaType("@ResponseBody BaseOutput"));
        method.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String contentLine1 = StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.updateSelective("+entityParameter.getName()+");";
        String returnLine = "return BaseOutput.success(\"修改成功\");";
        bodyLines.add(contentLine1);
        bodyLines.add(returnLine);
        method.addBodyLines(bodyLines);







        method.addAnnotation("@RequestMapping(value=\"/update.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        method.addJavaDocLine("");
        clazz.addMethod(method);
    }


    private void addDeleteMethod(TopLevelClass clazz, CompilationUnit unit){
        FullyQualifiedJavaType baseModelJavaType = unit.getType();
        Method method = new Method("delete");
        Parameter entityParameter = new Parameter(new FullyQualifiedJavaType("Long"), "id");
        method.addParameter(0, entityParameter);
        method.setReturnType(new FullyQualifiedJavaType("@ResponseBody BaseOutput"));
        method.setVisibility(JavaVisibility.PUBLIC);
        List<String> bodyLines = new ArrayList<>();
        String contentLine1 = StringUtils.uncapitalize(baseModelJavaType.getShortName())+"Service.delete("+entityParameter.getName()+");";
        String returnLine = "return BaseOutput.success(\"删除成功\");";
        bodyLines.add(contentLine1);
        bodyLines.add(returnLine);
        method.addBodyLines(bodyLines);







        method.addAnnotation("@RequestMapping(value=\"/delete.action\", method = {RequestMethod.GET, RequestMethod.POST})");

        method.addJavaDocLine("");
        clazz.addMethod(method);
    }


    private boolean isDTO(CompilationUnit unit){
        Set<FullyQualifiedJavaType> fullyQualifiedJavaTypes = unit.getSuperInterfaceTypes();
        if(fullyQualifiedJavaTypes.isEmpty()) {
            return false;
        }
        for(FullyQualifiedJavaType fullyQualifiedJavaType : fullyQualifiedJavaTypes) {
            if (fullyQualifiedJavaType.getFullyQualifiedName().equals(IBaseDomain.class.getName()) || fullyQualifiedJavaType.getFullyQualifiedName().equals(IDTO.class.getName())){
                return true;
            }
        }
        return false;
    }

}
