package com.sa.seata.retrofitful;

import com.sa.retrofitful.annotation.ReqHeader;
import com.sa.retrofitful.aop.annotation.Order;
import com.sa.retrofitful.aop.filter.AbstractFilter;
import com.sa.retrofitful.aop.invocation.Invocation;
import com.sa.retrofitful.cache.RestfulCache;
import com.sa.seata.annotation.GlobalTx;
import com.sa.seata.consts.SeataConsts;
import io.seata.core.context.RootContext;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;

import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;


@Component
@Order(100)
public class RestfulSeataXidFilter extends AbstractFilter {

    @Override
    public Object invoke(Invocation invocation) throws Exception {

        Method method = invocation.getMethod();
        GlobalTx globalTx = method.getAnnotation(GlobalTx.class);

        if(globalTx == null){
            return super.invoke(invocation);
        }
        Annotation[][] ass = method.getParameterAnnotations();

        if(ass == null || ass.length == 0){
            return super.invoke(invocation);
        }
        String xid = RootContext.getXID();
        if(StringUtils.isEmpty(xid)){
            return super.invoke(invocation);
        }

        boolean hasHeader = false;
        retry:
        for(int i=0; i<ass.length; i++) {
            for (int j = 0; j < ass[i].length; j++) {
                Annotation annotation = ass[i][j];
                if (ReqHeader.class.equals(annotation.annotationType())) {
                    Map<String, String> headerMap = (Map)invocation.getArgs()[i];
                    if(headerMap == null) {
                        headerMap = new HashMap<>(2);
                        invocation.getArgs()[i] = headerMap;
                    }
                    headerMap.put(RootContext.KEY_XID, xid);
                    hasHeader = true;
                    break retry;
                }
            }
        }

        if(!hasHeader){
            HashMap<String, String> headerMap = new HashMap<>(2);
            headerMap.put(SeataConsts.XID, xid);
            RestfulCache.RESTFUL_HEADER_THREAD_LOCAL.set(headerMap);
        }
        return super.invoke(invocation);
    }
}
