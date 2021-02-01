package com.sa.retrofitful.aop.filter;

import com.sa.retrofitful.aop.invocation.Invocation;


public abstract class AbstractFilter implements Filter {
    protected Filter restfulFilter;

    @Override
    public Filter getRestfulFilter() {
        return restfulFilter;
    }

    @Override
    public void setRestfulFilter(Filter restfulFilter) {
        this.restfulFilter = restfulFilter;
    }

    @Override
    public Object invoke(Invocation invocation) throws Exception{
        return getRestfulFilter().invoke(invocation);
    }


}
