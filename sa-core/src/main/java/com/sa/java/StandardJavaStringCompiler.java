package com.sa.java;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class StandardJavaStringCompiler {
	protected static final Logger logger = LoggerFactory.getLogger(StandardJavaStringCompiler.class);


	public static Class<?> compile(String fileName, String source) throws IOException, ClassNotFoundException {

		JavaCompiler cmp = ToolProvider.getSystemJavaCompiler();

		StandardJavaFileManager fm = cmp.getStandardFileManager(null, null, null);

		JavaFileObject jfo = new StringJavaObject(fileName, source);

		List<String> optionsList = new ArrayList<String>();

		optionsList.addAll(Arrays.asList("-d", "./target/classes"));

		List<JavaFileObject> jfos = Arrays.asList(jfo);

		JavaCompiler.CompilationTask task = cmp.getTask(null, fm, null, optionsList, null, jfos);

		Boolean result = task.call();
		if (result == null || !result.booleanValue()) {
			throw new RuntimeException("Compilation failed.");
		}
		return Class.forName(fileName);
	}








}
