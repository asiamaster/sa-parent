





























package com.sa.processor.processor;

import com.google.auto.service.AutoService;
import com.sa.processor.annotation.GenDTOMethod;
import com.sa.processor.util.ClassUtils;
import com.sa.util.POJOUtils;
import com.squareup.javapoet.*;
import com.sun.tools.javac.code.Attribute;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.*;
import javax.persistence.*;
import javax.tools.Diagnostic;
import java.io.File;
import java.io.IOException;
import java.util.*;


@AutoService(Processor.class)
public class DtoProcessor extends BaseProcessor {


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        Set<? extends Element> set = env.getElementsAnnotatedWith(GenDTOMethod.class);
        set.forEach(element -> {







            String sourceFilePath = ((Symbol.ClassSymbol) element).sourcefile.getName();

            String filePath = sourceFilePath.substring(0, sourceFilePath.indexOf("/src/main/java/")+15);

            String packageFullName = ((Symbol.ClassSymbol) element).packge().fullname.toString();

            TypeSpec typeSpec = null;
            try {
                if (element.getKind() == ElementKind.INTERFACE) {
                    typeSpec = buildIntfSource(element);
                }else{
                    typeSpec = buildClassSource(element);
                }

            } catch (ClassNotFoundException e) {
                messager.printMessage(Diagnostic.Kind.WARNING, "构建失败:" + e.getMessage());
                return;
            }



            JavaFile javaFile = JavaFile.builder(packageFullName, typeSpec).skipJavaLangImports(true).build();
            try {
                javaFile.writeTo(new File(filePath));
            } catch (IOException e) {
                e.printStackTrace();
            }





        });
        return true;
    }


    private TypeSpec buildClassSource(Element element) throws ClassNotFoundException {
        String simpleName = element.getSimpleName().toString();

        TypeSpec.Builder interfaceBuilder = TypeSpec.interfaceBuilder(simpleName)
                .addModifiers(Modifier.PUBLIC);
        String doc = elementUtils.getDocComment(element);
        if(StringUtils.isNotBlank(doc)) {
            interfaceBuilder.addJavadoc(doc);
        }
        com.sun.tools.javac.util.List<Attribute.Compound> classAnnotationMirrors = ((Symbol.ClassSymbol) element).getAnnotationMirrors();

        List<AnnotationSpec> annotationSpecs = new ArrayList<>(classAnnotationMirrors.size());

        boolean containsTableAnnotation = false;
        for(int i=0; i<classAnnotationMirrors.size(); i++){

            Attribute.Compound compound = classAnnotationMirrors.get(i);
            Map<Symbol.MethodSymbol, Attribute> symbolAttributeMap = compound.getElementValues();

            Boolean reuse = null;
            if (Table.class.getName().equals(compound.type.toString())) {
                containsTableAnnotation = true;
            }
            if (GenDTOMethod.class.getName().equals(compound.type.toString())) {

                if (symbolAttributeMap.isEmpty()) {












                    List<Symbol.MethodSymbol> methodSymbols = (List) compound.getAnnotationType().asElement().getEnclosedElements();
                    for (Symbol.MethodSymbol methodSymbol : methodSymbols) {
                        if (methodSymbol.getSimpleName().toString().equals("reuse")) {
                            reuse = (boolean) methodSymbol.defaultValue.getValue();
                            break;
                        }
                    }
                } else {
                    for (Map.Entry<Symbol.MethodSymbol, Attribute> entry : symbolAttributeMap.entrySet()) {
                        Object reuseValue = entry.getValue().getValue();

                        if (entry.getKey().getSimpleName().toString().equals("reuse")) {
                            reuse = (boolean) reuseValue;
                            break;
                        }
                    }
                }
                if(null != reuse && !reuse){
                    continue;
                }
            }


            AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Class.forName(compound.getAnnotationType().toString()));
            for(Map.Entry<Symbol.MethodSymbol, Attribute> entry : symbolAttributeMap.entrySet()){
                Object value = entry.getValue().getValue();
                if(value instanceof String){
                    annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$S", value).build());
                }else if(entry.getValue() instanceof Attribute.Enum){
                    annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$T.$L", ((Attribute.Enum) entry.getValue()).type, value.toString()).build());
                }else{
                    annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$L", value).build());
                }
            }
            annotationSpecs.add(annotationSpecBuilder.build());
        }
        if(!containsTableAnnotation){

            AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Table.class);
            annotationSpecBuilder.addMember("name", CodeBlock.builder().add("$S", "`"+POJOUtils.humpToLineFast(simpleName)+"`").build());
            annotationSpecs.add(annotationSpecBuilder.build());
        }


        com.sun.tools.javac.util.List<Type> interfacesType = ((Symbol.ClassSymbol) element).getInterfaces();
        List<TypeName> typeNames = new ArrayList<>(interfacesType.size());
        for (Type type : interfacesType) {
            typeNames.add(ParameterizedTypeName.get(type));
        }



        interfaceBuilder.addAnnotations(annotationSpecs).addSuperinterfaces(typeNames);

        List<? extends Element> elements = element.getEnclosedElements();
        for(Element ele : elements) {

            if (ele.getKind().equals(ElementKind.FIELD)) {
                messager.printMessage(Diagnostic.Kind.OTHER, "根据属性" + ele.getSimpleName() + "构建getter/setter.");
                buildMethodByField(interfaceBuilder, (Symbol.VarSymbol) ele);
            } else if (ele.getKind().equals(ElementKind.METHOD)) {


            }
        }
        return interfaceBuilder.build();
    }


    private TypeSpec buildIntfSource(Element element) throws ClassNotFoundException {
        String simpleName = element.getSimpleName().toString();

        TypeSpec.Builder builder = TypeSpec.interfaceBuilder(simpleName)
                .addModifiers(Modifier.PUBLIC);
        String doc = elementUtils.getDocComment(element);
        if(StringUtils.isNotBlank(doc)) {
            builder.addJavadoc(doc);
        }
        com.sun.tools.javac.util.List<Attribute.Compound> classAnnotationMirrors = ((Symbol.ClassSymbol) element).getAnnotationMirrors();

        List<AnnotationSpec> annotationSpecs = new ArrayList<>(classAnnotationMirrors.size());
        for(int i=0; i<classAnnotationMirrors.size(); i++){

            Attribute.Compound compound = classAnnotationMirrors.get(i);
            Map<Symbol.MethodSymbol, Attribute> symbolAttributeMap = compound.getElementValues();

            Boolean reuse = null;

            if(symbolAttributeMap.isEmpty()) {












                List<Symbol.MethodSymbol> methodSymbols = (List)compound.getAnnotationType().asElement().getEnclosedElements();
                for(Symbol.MethodSymbol methodSymbol :  methodSymbols){
                    if(methodSymbol.getSimpleName().toString().equals("reuse")){
                        reuse = (boolean) methodSymbol.defaultValue.getValue();
                        break;
                    }
                }
            }else {
                if (GenDTOMethod.class.getName().equals(compound.type.toString())) {
                    for (Map.Entry<Symbol.MethodSymbol, Attribute> entry : symbolAttributeMap.entrySet()) {
                        Object reuseValue = entry.getValue().getValue();

                        if (entry.getKey().getSimpleName().toString().equals("reuse")) {
                            reuse = (boolean) reuseValue;
                            break;
                        }
                    }
                }
            }
            if(null != reuse && !reuse){
                continue;
            }


            AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Class.forName(compound.getAnnotationType().toString()));
            for(Map.Entry<Symbol.MethodSymbol, Attribute> entry : symbolAttributeMap.entrySet()){
                Object value = entry.getValue().getValue();
                if(value instanceof String){
                    annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$S", value).build());
                }else if(entry.getValue() instanceof Attribute.Enum){
                    annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$T.$L", ((Attribute.Enum) entry.getValue()).type, value.toString()).build());
                }else{
                    annotationSpecBuilder.addMember(entry.getKey().getSimpleName().toString(), CodeBlock.builder().add("$L", value).build());
                }
            }
            annotationSpecs.add(annotationSpecBuilder.build());
        }

        com.sun.tools.javac.util.List<Type> interfacesType = ((Symbol.ClassSymbol) element).getInterfaces();
        List<TypeName> typeNames = new ArrayList<>(interfacesType.size());
        for (Type type : interfacesType) {
            typeNames.add(ParameterizedTypeName.get(type));
        }
        builder.addAnnotations(annotationSpecs).addSuperinterfaces(typeNames);

        List<? extends Element> elements = element.getEnclosedElements();
        for(Element ele : elements) {

            if (ele.getKind().equals(ElementKind.FIELD)) {
                messager.printMessage(Diagnostic.Kind.OTHER, "根据属性" + ele.getSimpleName() + "构建getter/setter.");
                buildMethodByField(builder, (Symbol.VarSymbol) ele);
            } else if (ele.getKind().equals(ElementKind.METHOD)) {



                buildMethod(builder, (Symbol.MethodSymbol) ele);
            }
        }
        return builder.build();
    }


    private void buildMethod(TypeSpec.Builder builder, Symbol.MethodSymbol methodSymbol) throws ClassNotFoundException {
        String methodSimpleName = methodSymbol.getSimpleName().toString();
        MethodSpec.Builder methodBuilder = MethodSpec.methodBuilder(methodSimpleName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);
        com.sun.tools.javac.util.List<Type> types = methodSymbol.getReturnType().getTypeArguments();

        if(types.isEmpty()) {
            Class<?> retType = ClassUtils.forName(methodSymbol.getReturnType().toString());
            methodBuilder.returns(retType);

        }else{

            TypeName typeName = buildTypeName(methodSymbol);
            methodBuilder.returns(typeName);


        }


        com.sun.tools.javac.util.List<Symbol.VarSymbol> parameters = methodSymbol.getParameters();
        if(null != parameters && !parameters.isEmpty()){
            for(Symbol.VarSymbol symbol : parameters) {
                methodBuilder.addParameter(buildTypeName(symbol), POJOUtils.getBeanField(methodSimpleName));
            }
        }


        List<? extends AnnotationMirror> methodAnnotationMirrors = methodSymbol.getAnnotationMirrors();
        for(int i=0; i<methodAnnotationMirrors.size(); i++){
            AnnotationMirror annotationMirror = methodAnnotationMirrors.get(i);
            AnnotationSpec.Builder annotationSpecBuilder = AnnotationSpec.builder(Class.forName(annotationMirror.getAnnotationType().toString()));
            annotationMirror.getElementValues().entrySet().stream().forEach((t)->{
                Object value = t.getValue().getValue();
                if(value instanceof String){
                    annotationSpecBuilder.addMember(t.getKey().getSimpleName().toString(), CodeBlock.builder().add("$S", t.getValue().getValue()).build());
                }else if(t.getValue() instanceof Attribute.Enum){
                    annotationSpecBuilder.addMember(t.getKey().getSimpleName().toString(), CodeBlock.builder().add("$T.$L", ((Attribute.Enum) t.getValue()).type, t.getValue().getValue().toString()).build());
                }else{
                    annotationSpecBuilder.addMember(t.getKey().getSimpleName().toString(), CodeBlock.builder().add("$L", t.getValue().getValue()).build());
                }
            });
            methodBuilder.addAnnotation(annotationSpecBuilder.build());
        }
        String doc = elementUtils.getDocComment(methodSymbol);
        if(StringUtils.isNotBlank(doc)) {
            methodBuilder.addJavadoc(doc);
        }
        builder.addMethod(methodBuilder.build());
    }


    private void buildMethodByField(TypeSpec.Builder builder, Symbol.VarSymbol varSymbol) throws ClassNotFoundException {
        String fieldSimpleName = varSymbol.getSimpleName().toString();

        String getterName = "get" + fieldSimpleName.substring(0, 1).toUpperCase() + fieldSimpleName.substring(1);
        MethodSpec.Builder getMethodBuilder = MethodSpec.methodBuilder(getterName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT);

        AnnotationSpec.Builder columnAnnotationSpecBuilder = AnnotationSpec.builder(Column.class);
        StringBuilder fieldSimpleNameStringBuilder = new StringBuilder();
        columnAnnotationSpecBuilder.addMember("name", CodeBlock.builder().add("$S", fieldSimpleNameStringBuilder.append("`").append(POJOUtils.humpToLineFast(fieldSimpleName)).append("`").toString()).build());
        getMethodBuilder.addAnnotation(columnAnnotationSpecBuilder.build());
        if("id".equals(fieldSimpleName)){

            getMethodBuilder.addAnnotation(Id.class);

            AnnotationSpec.Builder generatedValueAnnotationSpecBuilder = AnnotationSpec.builder(GeneratedValue.class);
            generatedValueAnnotationSpecBuilder.addMember("strategy", CodeBlock.builder().add("$T.$L", GenerationType.class, "IDENTITY").build());
            getMethodBuilder.addAnnotation(generatedValueAnnotationSpecBuilder.build());
        }

        String setterName = "set" + fieldSimpleName.substring(0, 1).toUpperCase() + fieldSimpleName.substring(1);
        MethodSpec.Builder setMethodBuilder = MethodSpec.methodBuilder(setterName).addModifiers(Modifier.PUBLIC, Modifier.ABSTRACT).returns(void.class);
        com.sun.tools.javac.util.List<Type> types = varSymbol.asType().getTypeArguments();

        if(types.isEmpty()){

            getMethodBuilder.returns(ClassUtils.forName(varSymbol.asType().tsym.toString()));

            setMethodBuilder.addParameter(ClassUtils.forName(varSymbol.asType().tsym.toString()), fieldSimpleName);
        }else{

            TypeName typeName = buildTypeName(varSymbol);
            getMethodBuilder.returns(typeName);
            setMethodBuilder.addParameter(typeName, fieldSimpleName);
        }
        String doc = elementUtils.getDocComment(varSymbol);
        if(StringUtils.isNotBlank(doc)) {
            getMethodBuilder.addJavadoc(doc);
        }

        MethodSpec getterSpec = getMethodBuilder.build();
        builder.addMethod(getterSpec);
        MethodSpec setterSpec = setMethodBuilder.build();
        builder.addMethod(setterSpec);

    }


    private TypeName buildTypeName(Symbol symbol) throws ClassNotFoundException {
        boolean isVarSymbol = symbol instanceof Symbol.VarSymbol;

        String classNameStr = isVarSymbol ? symbol.asType().tsym.toString() : ((Symbol.MethodSymbol) symbol).getReturnType().getOriginalType().tsym.toString();
        ClassName className = ClassName.get(ClassUtils.forName(classNameStr));
        com.sun.tools.javac.util.List<Type> types = isVarSymbol ? symbol.asType().getTypeArguments() : ((Symbol.MethodSymbol) symbol).getReturnType().getTypeArguments();
        if(types.isEmpty()){
            return className;
        }
        List<TypeName> typeNames = new ArrayList<>(types.size());
        for(Type type : types){
            com.sun.tools.javac.util.List<Type> typeList = type.getTypeArguments();
            if(typeList.isEmpty()) {
                typeNames.add(ClassName.get(ClassUtils.forName(type.toString())));
            }else{
                typeNames.add(buildTypeName(type));
            }
        }
        return ParameterizedTypeName.get(className, typeNames.toArray(new TypeName[]{}));
    }

    private TypeName buildTypeName(Type type) throws ClassNotFoundException {
        com.sun.tools.javac.util.List<Type> typeList = type.getTypeArguments();
        if(typeList.isEmpty()) {
            return ClassName.get(ClassUtils.forName(type.toString()));
        }else{
            List<TypeName> typeNames = new ArrayList<>(typeList.size());
            ClassName className = ClassName.get(ClassUtils.forName(type.tsym.toString()));
            for(Type type1 : typeList){
                typeNames.add(buildTypeName(type1));
            }
            return ParameterizedTypeName.get(className, typeNames.toArray(new TypeName[]{}));
        }
    }


    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(GenDTOMethod.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return super.getSupportedSourceVersion();
    }
}