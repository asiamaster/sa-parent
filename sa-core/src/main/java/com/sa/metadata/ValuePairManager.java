package com.sa.metadata;


import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class ValuePairManager {

    private static final Map<String, List<ValuePair<?>>> cache = new ConcurrentHashMap<String, List<ValuePair<?>>>();


    public static void register(String key, List<ValuePair<?>> value) {
        cache.put(key, value);
    }


    public static List<ValuePair<?>> get(String key) {
        return cache.get(key);
    }


    public static void clearValuePairs() {
        cache.clear();
    }
}
