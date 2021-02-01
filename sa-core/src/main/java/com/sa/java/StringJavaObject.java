package com.sa.java;

import javax.tools.SimpleJavaFileObject;
import java.io.IOException;
import java.net.URI;

public class StringJavaObject extends SimpleJavaFileObject {

    private String content = "";

    public StringJavaObject(String _javaFileName,String _content){
        super(_createStringJavaObjectUri(_javaFileName),Kind.SOURCE);
        content = _content;
    }

    private static URI _createStringJavaObjectUri(String name){
        //注意此处没有设置包名
        return URI.create("String:///" + name + Kind.SOURCE.extension);
    }

    @Override
    public CharSequence getCharContent(boolean ignoreEncodingErrors)
            throws IOException {
        return content;
    }
}

