package com.sa.oplog.base;

import com.sa.oplog.dto.LogContext;

import java.lang.reflect.Method;


public interface LogHandler {
    
    void log(String content, Method method, Object[] args, String params, LogContext logContext);
}
