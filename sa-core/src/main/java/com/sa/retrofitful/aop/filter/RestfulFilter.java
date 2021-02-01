package com.sa.retrofitful.aop.filter;

import com.sa.java.B;
import com.sa.retrofitful.aop.annotation.Order;
import com.sa.retrofitful.aop.invocation.Invocation;
import com.sa.retrofitful.aop.service.RestfulService;
import org.springframework.context.annotation.DependsOn;
import org.springframework.core.Ordered;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;


@Component
@Order(Ordered.LOWEST_PRECEDENCE)
@DependsOn("initConfig")
public class RestfulFilter extends AbstractFilter {
    private RestfulService restfulService;

    @PostConstruct
    public void init(){
        Class<?> clazz = (Class<?>) B.b.g("restfulService");
        if(clazz != null) {
            try {
                restfulService = (RestfulService) clazz.newInstance();
            } catch (Exception e) {
            }
        }
    }

    @Override
    public Object invoke(Invocation invocation) throws Exception {
        return restfulService.invoke(invocation);
    }


}
