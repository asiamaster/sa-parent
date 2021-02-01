package com.sa.util;

import java.util.concurrent.ExecutorService;

public interface ICustomThreadPoolExecutor {

	
	ExecutorService getCustomThreadPoolExecutor();

	
	ExecutorService getCustomThreadPoolExecutor(int corePoolSize,
												int maximumPoolSize,
												long keepAliveTime,
												int workQueueSize);
}