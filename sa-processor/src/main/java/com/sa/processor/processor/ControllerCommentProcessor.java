





























package com.sa.processor.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@AutoService(Processor.class)
public class ControllerCommentProcessor extends CommentProcessor {


    private List<String> controllerScan = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        String serviceScanStr = properties.getProperty("controller.scan");
        if(StringUtils.isNotBlank(serviceScanStr)){
            controllerScan = Arrays.asList(serviceScanStr.trim().split(","));
        }
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {

        for (TypeElement typeElement : annotations) {
            Set<? extends Element> set = env.getElementsAnnotatedWith(typeElement);

            for(Element classElement : set){
                if(controllerScan != null) {

                    if(!matchPackage(classElement)){
                        return false;
                    }
                }
                if(StringUtils.isBlank(elementUtils.getDocComment(classElement))){
                    error(classElement, "%s需要添加JavaDoc注释！", ((Symbol.ClassSymbol) classElement).fullname);
                    return true;
                }
                List<? extends Element> enclosedElements = classElement.getEnclosedElements();
                for(Element enclosedElement : enclosedElements){

                    if(enclosedElement.getKind().equals(ElementKind.CONSTRUCTOR)
                            || enclosedElement.getKind().equals(ElementKind.FIELD)
                            || enclosedElement.getKind().equals(ElementKind.CLASS)){
                        continue;
                    }
                    String comment = elementUtils.getDocComment(enclosedElement);
                    if(StringUtils.isBlank(comment)){
                        error(enclosedElement, "%s %s方法需要添加JavaDoc注释！",
                                ((Symbol.MethodSymbol) enclosedElement).owner.getQualifiedName().toString(),
                                enclosedElement.getSimpleName());
                        return true;
                    }



                }
            }
        }
        return true;
    }


    private boolean matchPackage(Element classElement){
        String pkgFullname = ((Symbol.ClassSymbol) classElement).packge().fullname.toString();
        for(String path : controllerScan){

            if(pkgFullname.startsWith(path.trim())){
                return true;
            }
        }
        return false;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(Controller.class.getCanonicalName());
        annotataions.add(RestController.class.getCanonicalName());
        return annotataions;
    }

}