package com.sa.retrofitful.aop.service;

import com.sa.retrofitful.aop.invocation.Invocation;


public interface RestfulService {


    Object invoke(Invocation invocation);

}
