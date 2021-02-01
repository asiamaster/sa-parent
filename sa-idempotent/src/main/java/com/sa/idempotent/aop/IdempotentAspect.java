package com.sa.idempotent.aop;

import com.sa.idempotent.service.IdempotentTokenService;
import com.sa.redis.service.RedisDistributedLock;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
@Aspect
@ConditionalOnExpression("'${idempotent.enable}'=='true'")
public class IdempotentAspect {
    @Autowired
    IdempotentTokenService idempotentTokenService;

    @Autowired
    RedisDistributedLock redisDistributedLock;

    IdempotentAspectHandler idempotentAspectHandler;

    
    @PostConstruct
    public void init() {
        idempotentAspectHandler = new IdempotentAspectHandlerImpl();
    }

    
    @Around("@annotation(com.sa.idempotent.annotation.Token)")
    public Object token(ProceedingJoinPoint point) throws Throwable {
        return idempotentAspectHandler.aroundToken(point, idempotentTokenService);
    }

    
    @Around("@annotation(com.sa.idempotent.annotation.Idempotent)")
    public Object idempotent(ProceedingJoinPoint point) throws Throwable {
        return idempotentAspectHandler.aroundIdempotent(point, redisDistributedLock);
    }

}
