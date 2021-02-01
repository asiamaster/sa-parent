package com.sa.dto;

import javassist.ClassPath;
import javassist.CtClass;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;

public interface IDTOFactory {


    void registerDTOInstance(AnnotationMetadata importingClassMetadata);


    void registerDTOInstanceFromPackages(Set<String> packages, String file);


    <T extends IDTO> CtClass createCtClass(Class<T> clazz) throws Exception;


    void insertClassPath(ClassPath classPath);


    void importPackage(String packageName);
}
