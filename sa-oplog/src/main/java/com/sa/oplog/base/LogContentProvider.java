package com.sa.oplog.base;

import com.sa.oplog.dto.LogContext;

import java.lang.reflect.Method;


public interface LogContentProvider {
    
    String content(Method method, Object[] args, String params, LogContext logContext);
}
