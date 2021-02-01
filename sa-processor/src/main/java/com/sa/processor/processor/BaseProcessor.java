





























package com.sa.processor.processor;

import com.sun.tools.javac.processing.JavacProcessingEnvironment;
import com.sun.tools.javac.tree.TreeMaker;
import com.sun.tools.javac.util.Context;
import com.sun.tools.javac.util.Names;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.tools.Diagnostic;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;


public abstract class BaseProcessor extends AbstractProcessor {

    protected Messager messager;

    protected TreeMaker treeMaker;
    protected Names names;
    protected Filer filer;

    protected Elements elementUtils;

    Properties properties = new Properties();

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        this.messager = processingEnv.getMessager();

        Context context = ((JavacProcessingEnvironment) processingEnv).getContext();
        this.treeMaker = TreeMaker.instance(context);
        this.names = Names.instance(context);
        filer = processingEnv.getFiler();
        elementUtils = processingEnv.getElementUtils();
        properties = new Properties();

        InputStream in = BaseProcessor.class.getClassLoader().getResourceAsStream("conf/processor.properties");
        if(null == in){
            return;
        }

        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }






















    protected void error(Element e, String msg, Object... args) {
        printMessage(Diagnostic.Kind.ERROR, e, String.format(msg, args));
    }

    protected void warning(Element e, String msg, Object... args) {
        printMessage(Diagnostic.Kind.WARNING, e, String.format(msg, args));
    }

    protected void info(Element e, String msg, Object... args) {
        printMessage(Diagnostic.Kind.OTHER, e, String.format(msg, args));
    }

    protected void printMessage(Diagnostic.Kind kind, Element e, String msg, Object... args) {
        messager.printMessage(kind, String.format(msg, args), e);
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }
}