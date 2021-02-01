





























package com.sa.processor.processor;

import com.google.auto.service.AutoService;
import com.sun.tools.javac.code.Symbol;
import com.sun.tools.javac.code.Type;
import com.sun.tools.javac.model.JavacElements;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;


@AutoService(Processor.class)
public class ServiceCommentProcessor extends CommentProcessor {


    private List<String> serviceScan = null;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        String serviceScanStr = properties.getProperty("service.scan");
        if(StringUtils.isNotBlank(serviceScanStr)){
            serviceScan = Arrays.asList(serviceScanStr.trim().split(","));
        }
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {
        for (TypeElement typeElement : annotations) {
            Set<? extends Element> set = env.getElementsAnnotatedWith(typeElement);
            for(Element classElement : set){
                if(serviceScan != null) {

                    if(!matchPackage(classElement)){
                        return false;
                    }
                }
                com.sun.tools.javac.util.List<Type> interfaces = ((Symbol.ClassSymbol) classElement).getInterfaces();

                if(interfaces.isEmpty()){
                    return validClassComment(classElement);
                }
                TypeElement interfaceTypeElement = elementUtils.getTypeElement(interfaces.get(0).toString());

                if(((JavacElements) elementUtils).getTreeAndTopLevel(interfaceTypeElement, null, null) == null) {
                    continue;
                }

                if (StringUtils.isBlank(elementUtils.getDocComment(interfaceTypeElement))) {
                    error(interfaceTypeElement, "%s需要添加JavaDoc注释！", ((Symbol.ClassSymbol) interfaceTypeElement).fullname);
                    return true;
                }
                List<? extends Element> members = elementUtils.getAllMembers(interfaceTypeElement);
                for(Element member : members){

                    if(((Symbol.MethodSymbol) member).owner.getQualifiedName().toString().equals(Object.class.getName())){
                        continue;
                    }

                    if(!((Symbol.MethodSymbol) member).owner.equals(interfaceTypeElement)){
                        continue;
                    }
                    if(StringUtils.isBlank(elementUtils.getDocComment(member))){
                        error(member, "%s %s方法需要添加JavaDoc注释！",
                                ((Symbol.MethodSymbol) member).owner.getQualifiedName().toString(),
                                member.getSimpleName());
                        return true;
                    }
                }

            }
        }
        return true;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataionSet = new LinkedHashSet<String>();
        annotataionSet.add(Service.class.getCanonicalName());
        annotataionSet.add(Component.class.getCanonicalName());
        return annotataionSet;
    }



    private boolean matchPackage(Element classElement){
        String pkgFullname = ((Symbol.ClassSymbol) classElement).packge().fullname.toString();
        for(String path : serviceScan){

            if(pkgFullname.startsWith(path.trim())){
                return true;
            }
        }
        return false;
    }

}