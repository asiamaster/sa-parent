package com.sa.processor.processor;

import com.sun.tools.javac.code.Symbol;
import org.apache.commons.lang3.StringUtils;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.List;
import java.util.Set;


public abstract class CommentProcessor extends BaseProcessor {


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

        for (TypeElement typeElement : annotations) {
            Set<? extends Element> set = env.getElementsAnnotatedWith(typeElement);

            for(Element classElement : set){
                if(!validClassComment(classElement)){
                    return true;
                }



            }
        }
        return true;
    }


    protected boolean validClassComment(Element classElement){

        if(StringUtils.isBlank(elementUtils.getDocComment(classElement))){
            error(classElement, "%s需要添加JavaDoc注释！", ((Symbol.ClassSymbol) classElement).fullname);
            return false;
        }
        List<? extends Element> enclosedElements = classElement.getEnclosedElements();
        for(Element enclosedElement : enclosedElements) {

            if (enclosedElement.getKind().equals(ElementKind.CONSTRUCTOR) || enclosedElement.getKind().equals(ElementKind.FIELD) || enclosedElement.getKind().equals(ElementKind.CLASS)) {
                continue;
            }

            if(enclosedElement.getAnnotation(Override.class) != null){
                continue;
            }
            String comment = elementUtils.getDocComment(enclosedElement);
            if (StringUtils.isBlank(comment)) {
                error(enclosedElement, "%s %s方法需要添加JavaDoc注释！", ((Symbol.MethodSymbol) enclosedElement).owner.getQualifiedName().toString(), enclosedElement.getSimpleName());
                return false;
            }
        }
        return true;
    }

}
