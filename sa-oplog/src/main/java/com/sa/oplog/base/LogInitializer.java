package com.sa.oplog.base;

import com.sa.oplog.dto.LogContext;

import java.lang.reflect.Method;


public interface LogInitializer {

    LogContext init(Method method, Object[] args);

}
