package com.sa.retrofitful.aop.filter;

import com.sa.retrofitful.aop.invocation.Invocation;


public interface Filter {

    void setRestfulFilter(Filter restfulFilter);

    Filter getRestfulFilter();

    Object invoke(Invocation invocation) throws Exception;
}
