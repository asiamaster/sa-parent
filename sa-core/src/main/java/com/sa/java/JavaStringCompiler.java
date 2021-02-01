package com.sa.java;

import com.sa.util.SpringUtil;
import com.sa.util.ZipUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.system.ApplicationHome;
import org.springframework.util.ResourceUtils;

import javax.tools.JavaCompiler;
import javax.tools.JavaCompiler.CompilationTask;
import javax.tools.JavaFileObject;
import javax.tools.StandardJavaFileManager;
import javax.tools.ToolProvider;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Map;


public class JavaStringCompiler {
	protected static final Logger logger = LoggerFactory.getLogger(JavaStringCompiler.class);
	JavaCompiler compiler;
	StandardJavaFileManager stdManager;

	private static boolean isRun = false;

	public JavaStringCompiler() {
		this.compiler = ToolProvider.getSystemJavaCompiler();
		this.stdManager = compiler.getStandardFileManager(null, null, null);
	}


	@SuppressWarnings("all")
	public Map<String, byte[]> compile(String fileName, String source) throws IOException {
		try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
			JavaFileObject javaFileObject = manager.makeStringSource(fileName, source);
            boolean isJar = isJarRun();
			if( !isRun && isJar ){
				unzip();
				isRun = true;
			}else if(isJar){
				File libDir = getLibDir();
				if(libDir != null) {
					libDir = new File(libDir.getParentFile().getPath() + "/BOOT-INF/lib/");
					if (!libDir.exists()) {
						unzip();
					}
				}
			}
			if(!isRun){
				B.i();
			}
			String classpath = isJar ? buildClassPath("/BOOT-INF/lib/") : null;
            Iterable options = isJar ? Arrays.asList("-classpath", classpath) : null;
			CompilationTask task = compiler.getTask(null, manager, null, options, null, Arrays.asList(javaFileObject));
			Boolean result = task.call();
			if (result == null || !result.booleanValue()) {
				throw new RuntimeException("Compilation failed.");
			}

			return manager.getClassBytes();
		}
    }


	@SuppressWarnings("all")
	public Map<String, byte[]> compileLocal(String fileName, String source) throws IOException {
		try (MemoryJavaFileManager manager = new MemoryJavaFileManager(stdManager)) {
			JavaFileObject javaFileObject = manager.makeStringSource(fileName, source);
			CompilationTask task = compiler.getTask(null, manager, null, null, null, Arrays.asList(javaFileObject));
			Boolean result = task.call();
			if (result == null || !result.booleanValue()) {
				throw new RuntimeException("Compilation failed.");
			}

			return manager.getClassBytes();
		}
	}


	public Class<?> loadClass(String name, Map<String, byte[]> classBytes) throws ClassNotFoundException, IOException {
		Object obj = SpringUtil.getBean("theOne");
		if(obj == null){
			try (MemoryClassLoader classLoader = new MemoryClassLoader(classBytes)) {
				return classLoader.loadClass(name);
			}
		}else {
			try (MemoryClassLoader classLoader = new MemoryClassLoader(classBytes, obj.getClass().getClassLoader())) {
				return classLoader.loadClass(name);
			}
		}
	}

    public static boolean isJarRun() {






		try {
			String classpath = ResourceUtils.getURL("classpath:").getPath();
			return classpath.contains("\\BOOT-INF\\classes!") || classpath.contains("!/BOOT-INF/classes!") ? true : false;
		} catch (FileNotFoundException e) {
			e.printStackTrace();

			return true;
		}
	}

	private static boolean isLinux() {
		return System.getProperty("os.name").toLowerCase().indexOf("linux") >= 0;
	}

    private static String buildClassPath(String libRelativePath){
		try {
			File libDir = getLibDir();
			libDir = new File(libDir.getParentFile().getPath() + libRelativePath);
			File[] jars = libDir.listFiles();
			StringBuilder classpath = new StringBuilder();
			libRelativePath = "."+libRelativePath;
			String separator = isLinux() ? ":" : ";";
			for (File jar : jars) {
				classpath.append(libRelativePath).append(jar.getName()).append(separator);
			}
			return classpath.toString();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	private static File getLibDir() throws FileNotFoundException {
		String path = ResourceUtils.getURL("classpath:").getPath();
		if(path.startsWith("file:/")){
			path = path.substring(6, path.length());
		}
		if(isLinux() && !path.startsWith("/")){
			path = "/"+path;
		}
		int index = path.lastIndexOf("!/BOOT-INF/classes");
		if(index < 0){
			return null;
		}
		return new File(path.substring(0, index));
	}


	private void unzip() throws IOException {
		String unzipDirPath = new ApplicationHome(getClass()).getDir().getAbsolutePath();
		String jarFile = new ApplicationHome(getClass()).getSource().getAbsolutePath();
		ZipUtils.unzip(new File(unzipDirPath), new File(jarFile));
	}

}
