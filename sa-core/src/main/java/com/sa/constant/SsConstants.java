package com.sa.constant;

import com.sa.util.SpringUtil;

import java.util.HashMap;
import java.util.Map;


public class SsConstants {

	public static final String COLON_ENCODE = "#@#@";

	public static final String ENCRYPT_PROPERTY_PASSWORD = "security";


	public static final int LIMIT = Integer.parseInt(SpringUtil.getProperty("export.limit", "2"));

	public static final Map<String, Long> EXPORT_FLAG = new HashMap<>(LIMIT);

}
