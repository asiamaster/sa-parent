package com.sa.idempotent.aop;

import com.sa.constant.ResultCode;
import com.sa.domain.BaseOutput;
import com.sa.idempotent.annotation.Idempotent;
import com.sa.idempotent.annotation.Token;
import com.sa.idempotent.service.IdempotentTokenService;
import com.sa.redis.service.RedisDistributedLock;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;


public class IdempotentAspectHandlerImpl implements IdempotentAspectHandler{

    public static final String TOKEN_VALUE = "token_value";

    @Override
    public Object aroundIdempotent(ProceedingJoinPoint point, RedisDistributedLock redisDistributedLock) throws Throwable{
        Signature signature = point.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        MethodSignature methodSignature = (MethodSignature) signature;
        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        Idempotent idempotent = currentMethod.getAnnotation(Idempotent.class);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        String type = StringUtils.isBlank(idempotent.value()) ? idempotent.type() : idempotent.value();
        String tokenValue = type.equals(Idempotent.HEADER) ? request.getHeader(TOKEN_VALUE) : request.getParameter(TOKEN_VALUE);

        if (redisDistributedLock.tryGetLock(tokenValue,tokenValue,180L)) {
            if (redisDistributedLock.exists(request.getRequestURI() + tokenValue)) {

                if (redisDistributedLock.get(request.getRequestURI() + tokenValue).equals(tokenValue)) {

                    redisDistributedLock.remove(request.getRequestURI() + tokenValue);

                    try {
                        return point.proceed();
                    }finally {

                        if (redisDistributedLock.exists(tokenValue)) {
                            redisDistributedLock.releaseLock(tokenValue, tokenValue);
                        }
                    }
                }
            }

            redisDistributedLock.releaseLock(tokenValue, tokenValue);
        }

        return BaseOutput.class.isAssignableFrom(currentMethod.getReturnType()) ? BaseOutput.failure(ResultCode.IDEMPOTENT_ERROR, "幂等接口调用失败") : null;
    }

    @Override
    public Object aroundToken(ProceedingJoinPoint point, IdempotentTokenService idempotentTokenService) throws Throwable{
        Signature signature = point.getSignature();
        if (!(signature instanceof MethodSignature)) {
            throw new IllegalArgumentException("该注解只能用于方法");
        }
        MethodSignature methodSignature = (MethodSignature) signature;
        Object target = point.getTarget();
        Method currentMethod = target.getClass().getMethod(methodSignature.getName(), methodSignature.getParameterTypes());
        Token token = currentMethod.getAnnotation(Token.class);
        HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
        if(StringUtils.isNotBlank(token.value())){
            request.setAttribute(TOKEN_VALUE, idempotentTokenService.getToken(token.value()).getValue());
        }else if(StringUtils.isNotBlank(token.url())){
            request.setAttribute(TOKEN_VALUE, idempotentTokenService.getToken(token.url()).getValue());
        }else{

            return false;
        }
        return point.proceed();
    }
}
