package com.sa.retrofitful.cache;

import java.util.Map;


public class RestfulCache {

    public static final ThreadLocal<Map<String, String>> RESTFUL_HEADER_THREAD_LOCAL = new ThreadLocal<>();
}
