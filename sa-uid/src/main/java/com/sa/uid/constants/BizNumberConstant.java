package com.sa.uid.constants;

import com.sa.uid.domain.BizNumberRule;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class BizNumberConstant {

    public static Map<String, BizNumberRule> bizNumberCache = new ConcurrentHashMap<>();

}
