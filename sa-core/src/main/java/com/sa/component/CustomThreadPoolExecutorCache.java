package com.sa.component;

import com.sa.java.B;
import com.sa.util.ICustomThreadPoolExecutor;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;


@Component
public class CustomThreadPoolExecutorCache {
    public static final String DEFAULT_KEY = "default";

    private Map<String, ExecutorService> executorServiceMap = new HashMap<>();

    
    @PostConstruct
    public void init() {
        try {
            executorServiceMap.put(DEFAULT_KEY, ((Class<ICustomThreadPoolExecutor>) B.b.g("threadPoolExecutor")).newInstance().getCustomThreadPoolExecutor());
        } catch (Exception e) {
        }
    }

    
    public ExecutorService getExecutor() {
        return executorServiceMap.get(DEFAULT_KEY);
    }

    
    public ExecutorService getExecutor(String key) {
        if(executorServiceMap.containsKey(key)) {
            return executorServiceMap.get(key);
        }
        synchronized(this){
            if(executorServiceMap.containsKey(key)) {
                return executorServiceMap.get(key);
            }
            try {
                executorServiceMap.put(key, ((Class<ICustomThreadPoolExecutor>) B.b.g("threadPoolExecutor")).newInstance().getCustomThreadPoolExecutor());
            } catch (Exception e) {
            }
        }
        return executorServiceMap.get(key);
    }

    
    public Map<String, ExecutorService> getExecutorServiceMap() {
        return executorServiceMap;
    }
}
