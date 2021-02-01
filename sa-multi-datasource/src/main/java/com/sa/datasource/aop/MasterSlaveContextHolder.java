package com.sa.datasource.aop;

import java.lang.reflect.Method;


public class MasterSlaveContextHolder {

	private static ThreadLocal<AccessType> accessType = new ThreadLocal<AccessType>();
	private static ThreadLocal<Method> stackTopMethod = new ThreadLocal<Method>();

	public static void read() {
		accessType.set(AccessType.READ);
	}

	public static void write() {
		accessType.set(AccessType.WRITE);
	}
	
	public static void clean() {
		accessType.remove();
		stackTopMethod.remove();
	}

	public static boolean writable() {
		return AccessType.WRITE.equals(accessType.get());
	}

	public static boolean isAvaiable() {
		return null != accessType.get();
	}


	public static void pushOuterMethod(Method method) {
		if (null == stackTopMethod.get())
			stackTopMethod.set(method);
	}
	

	public static void pushOuterMethodByForce(Method method) {
		stackTopMethod.set(method);
	}
	
	public static Method getStackTopMethod(){
		return stackTopMethod.get();
	}


	public static void pullOutMethod(Method method) {
		if (stackTopMethod.get() == method) {
			stackTopMethod.set(null);
			accessType.set(null);
		}
	}

	private enum AccessType {
		READ, WRITE
	}

}
