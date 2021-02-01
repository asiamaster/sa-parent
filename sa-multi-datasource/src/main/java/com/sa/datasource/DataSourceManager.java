package com.sa.datasource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class DataSourceManager {
	private static final Logger LOGGER = LoggerFactory.getLogger(DataSourceManager.class);


	public static SwitchMode switchMode;


	public static SelectorMode selectorMode;
	
	public static String master = SwitchDataSource.DEFAULT_DATASOURCE;

	
	public static List<String> slaves = new ArrayList<>(4);

	
	public static Map<String, Integer> weights = new HashMap<>();

	
	public static String getDefault() {
		return slaves.get(0);
	}

	public static String fetchSlave(int index) {
		return slaves.get(index);
	}

}
