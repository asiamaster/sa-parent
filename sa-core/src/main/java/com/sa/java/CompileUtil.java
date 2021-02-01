package com.sa.java;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


public class CompileUtil {
	private final static JavaStringCompiler compiler;
	public final static Map<String, Class<?>> classes = new HashMap<>();
	static {
		compiler = new JavaStringCompiler();
	}


	public static Map<String, byte[]> compileFile(String fileName, String source) throws IOException {
		return compiler.compile(fileName, source);
	}


	public static Map<String, byte[]> compileLocalFile(String fileName, String source) throws IOException {
		return compiler.compileLocal(fileName, source);
	}


	@SuppressWarnings("all")
	public static Class<?> compile(String classContent, String classFullname)  {
		try {
			String cn = classFullname.substring(classFullname.lastIndexOf(".")+1);
			Map<String, byte[]> results = compileFile(cn+".java", classContent);
			Class<?> clazz = compiler.loadClass(classFullname, results);
			classes.put(clazz.getName(), clazz);
			return clazz;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}


	@SuppressWarnings("all")
	public static Class<?> compileLocal(String classContent, String classFullname)  {
		try {
			String cn = classFullname.substring(classFullname.lastIndexOf(".")+1);
			Map<String, byte[]> results = compileLocalFile(cn+".java", classContent);
			Class<?> clazz = compiler.loadClass(classFullname, results);
			classes.put(clazz.getName(), clazz);
			return clazz;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}


	@SuppressWarnings("all")
	public static Class<?> compile(Map<String, byte[]> results, String classFullname)  {
		try {
			Class<?> clazz = compiler.loadClass(classFullname, results);
			classes.put(clazz.getName(), clazz);
			return clazz;
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}

	public static void clean(){
		try {
			((Class) B.b.g("cleaner")).getMethod("execute").invoke(null);
		} catch (Exception e) {
		}
	}
}
