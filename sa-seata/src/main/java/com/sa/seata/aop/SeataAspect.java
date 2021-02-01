package com.sa.seata.aop;

import com.sa.retrofitful.annotation.ReqHeader;
import com.sa.seata.consts.SeataConsts;
import io.seata.core.context.RootContext;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.reflect.MethodSignature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;






public class SeataAspect {

    private ExecutorService executor;

    private static final Logger log = LoggerFactory.getLogger(SeataAspect.class);
    @PostConstruct
    public void init() {
        System.out.println("SeataAspect.init");
    }


    @Around( "@annotation(com.sa.seata.annotation.GlobalTx)")
    public Object around(ProceedingJoinPoint point) throws Throwable {

        Method method = ((MethodSignature) point.getSignature()).getMethod();
        Annotation[][] ass = method.getParameterAnnotations();
        if(ass == null || ass.length == 0){
            return point.proceed();
        }
        retry:
        for(int i=0; i<ass.length; i++) {
            for (int j = 0; j < ass[i].length; j++) {
                Annotation annotation = ass[i][j];
                if (ReqHeader.class.equals(annotation.annotationType())) {
                    Map<String, String> headerMap = (Map)point.getArgs()[i];
                    if(headerMap == null) {
                        headerMap = new HashMap<>(1);
                    }
                    String xid = RootContext.getXID();
                    if (!StringUtils.isEmpty(xid)) {
                        headerMap.put(SeataConsts.XID, xid);
                    }
                    break retry;
                }
            }
        }
        return point.proceed();
    }
}
