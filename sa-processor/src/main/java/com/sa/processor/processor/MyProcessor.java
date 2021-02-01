package com.sa.processor.processor;

import com.sa.processor.annotation.GenDTOMethod;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;
import javax.tools.FileObject;
import javax.tools.StandardLocation;
import java.io.File;
import java.io.IOException;
import java.util.LinkedHashSet;
import java.util.Set;




public class MyProcessor extends AbstractProcessor {


    public static final String SUFFIX = "AutoGenerate";
    public static final String PREFIX = "My_";

    private Types typeUtils;


    private Elements elementUtils;
    private Filer filer;
    private Messager messager;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        typeUtils = processingEnv.getTypeUtils();
        elementUtils = processingEnv.getElementUtils();
        filer = processingEnv.getFiler();
        messager = processingEnv.getMessager();
    }


    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment env) {












        messager.printMessage(Diagnostic.Kind.WARNING, "env.processingOver():" + env.processingOver());
        messager.printMessage(Diagnostic.Kind.WARNING, "annotations:" + annotations.size());
        for (TypeElement typeElement : annotations) {
            for (Element e : env.getElementsAnnotatedWith(typeElement)) {

                messager.printMessage(Diagnostic.Kind.WARNING, "Printing:" + e.toString());
                messager.printMessage(Diagnostic.Kind.WARNING, "Printing:" + e.getSimpleName());
                messager.printMessage(Diagnostic.Kind.WARNING, "Printing:" + e.getEnclosedElements().toString());


                GenDTOMethod annotation = e.getAnnotation(GenDTOMethod.class);

                String name = e.getSimpleName().toString();
                char c = Character.toUpperCase(name.charAt(0));
                name = String.valueOf(c + name.substring(1));

                Element enclosingElement = e.getEnclosingElement();

                String enclosingQualifiedname;
                if (enclosingElement instanceof PackageElement) {
                    enclosingQualifiedname = ((PackageElement) enclosingElement).getQualifiedName().toString();
                } else {
                    enclosingQualifiedname = ((TypeElement) enclosingElement).getQualifiedName().toString();
                }

                processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "Found " + e);
                processingEnv.getMessager().printMessage(Diagnostic.Kind.MANDATORY_WARNING, "SOURCE VERSION:" + processingEnv.getSourceVersion().name());

                try {
                    FileObject fileObject = processingEnv.getFiler().createResource(StandardLocation.CLASS_OUTPUT, processingEnv.getElementUtils().getPackageOf(e).toString(), e.getSimpleName() + ".found", e);
                    messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "fileObject.uri: " + fileObject.toUri());
                    String rawPath = fileObject.toUri().getRawPath();
                    String dirPath = rawPath.substring(0, rawPath.lastIndexOf("/target/classes/")+1);
                    messager.printMessage(Diagnostic.Kind.MANDATORY_WARNING, "dirPath: " + dirPath);
                    gen(new File(dirPath+"src/main/java/"));
                    fileObject.openOutputStream().close();




                    return true;
                } catch (Exception x) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, x.toString(), e);
                }


































            }
        }
        return true;
    }

    private void gen(File file) throws IOException {
        MethodSpec main = MethodSpec.methodBuilder("main")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .returns(void.class)
                .addParameter(String[].class, "args")
                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
                .build();

        TypeSpec helloWorld = TypeSpec.classBuilder("HelloWorld")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(main)
                .build();

        JavaFile javaFile = JavaFile.builder("com.sa.dto", helloWorld)
                .build();

        javaFile.writeTo(file);
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> annotataions = new LinkedHashSet<String>();
        annotataions.add(GenDTOMethod.class.getCanonicalName());
        return annotataions;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    private void error(Element e, String msg, Object... args) {
        messager.printMessage(
                Diagnostic.Kind.ERROR,
                String.format(msg, args),
                e);
    }
}