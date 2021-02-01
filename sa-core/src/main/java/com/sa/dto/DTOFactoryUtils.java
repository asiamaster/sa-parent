package com.sa.dto;

import com.sa.java.B;
import javassist.CtClass;
import org.springframework.core.type.AnnotationMetadata;

import java.util.Set;


public class DTOFactoryUtils {


    @SuppressWarnings(value={"unchecked", "deprecation"})
    public static void registerDTOInstance(AnnotationMetadata importingClassMetadata){
        getInstance().registerDTOInstance(importingClassMetadata);
    }

    @SuppressWarnings(value={"unchecked", "deprecation"})
    public static void registerDTOInstanceFromPackages(Set<String> packages, String file){
        getInstance().registerDTOInstanceFromPackages(packages, file);
    }



    @SuppressWarnings(value={"unchecked", "deprecation"})
    public static <T extends IDTO> CtClass createCtClass(Class<T> clazz) throws Exception {
        return getInstance().createCtClass(clazz);
    }

    private static IDTOFactory getInstance(){
        try {
            return (IDTOFactory)((Class<?>) B.b.g("DTOFactory")).getMethod("getInstance").invoke(null);
        } catch (Exception e) {
            return null;
        }
    }

}