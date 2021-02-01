package com.sa.datasource.aop;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;


public class DynamicRoutingDataSourceContextHolder {


	private static final ThreadLocal<Stack<String>> contextHolder = new ThreadLocal<Stack<String>>();


	public static List<String> dataSourceIds = new ArrayList<>();

	public static void setDataSourceType(Stack<String> dataSourceType) {
		contextHolder.set(dataSourceType);
	}

	public static Stack<String> getDataSourceType() {
		if(contextHolder.get() == null){
			contextHolder.set(new Stack<String>());
		}
		return contextHolder.get();
	}


	public static String peek() {
		if(contextHolder.get() == null || contextHolder.get().isEmpty()){
			return null;
		}
		return contextHolder.get().peek();
	}


	public static String push(String value) {
		if(contextHolder.get() == null){
			contextHolder.set(new Stack<String>());
		}
		return contextHolder.get().push(value);
	}


	public static String pop() {
		if(contextHolder.get() == null || contextHolder.get().isEmpty()){
			return null;
		}
		return contextHolder.get().pop();
	}

	public static void clear() {
		contextHolder.remove();
	}


	public static boolean containsDataSource(String dataSourceId){
		return dataSourceIds.contains(dataSourceId);
	}

}
