package com.sa.idempotent.aop;

import com.sa.idempotent.service.IdempotentTokenService;
import com.sa.redis.service.RedisDistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;


public interface IdempotentAspectHandler {

    Object aroundIdempotent(ProceedingJoinPoint point, RedisDistributedLock redisDistributedLock) throws Throwable;

    Object aroundToken(ProceedingJoinPoint point, IdempotentTokenService idempotentTokenService) throws Throwable;
}
