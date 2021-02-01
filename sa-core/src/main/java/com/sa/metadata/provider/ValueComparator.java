package com.sa.metadata.provider;

import java.util.Comparator;
import java.util.Map;


public class ValueComparator implements Comparator<String> {

	Map<String, Integer> base;

	public ValueComparator(Map<String, Integer> map){
		this.base = map;
	}

	@Override
	public int compare(String key1, String key2) {
		Integer value1 = base.get(key1);
		Integer value2 = base.get(key2);
		if ((value2 - value1) > 0) {
			return 1;
		}else if (value2 - value1 ==0) {
			return key1.toString().compareTo(key2.toString());
		}else {
			return -1;
		}
	}
}
