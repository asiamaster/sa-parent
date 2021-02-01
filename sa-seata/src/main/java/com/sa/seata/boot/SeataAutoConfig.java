package com.sa.seata.boot;

import org.springframework.context.annotation.Bean;
import org.springframework.web.filter.OncePerRequestFilter;




public class SeataAutoConfig {


    @Bean(name="seataXidFilter")
    public OncePerRequestFilter seataXidFilter(){
        return new SeataXidFilter();
    }








}
