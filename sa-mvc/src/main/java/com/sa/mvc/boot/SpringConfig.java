package com.sa.mvc.boot;

import com.sa.mvc.spring.RequestJsonParamMethodArgumentResolver;
import com.sa.util.SpringUtil;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerAdapter;

import java.util.List;



public class SpringConfig {














    public CommandLineRunner customRequestMappingHandlerAdapter(){
        return new CommandLineRunner() {
            @Override
            public void run(String... strings) throws Exception {

                DefaultListableBeanFactory defaultListableBeanFactory = (DefaultListableBeanFactory) SpringUtil.getApplicationContext().getAutowireCapableBeanFactory();
                BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(RequestMappingHandlerAdapter.class);
                builder.addPropertyValue("synchronizeOnSession", true);
                List<HandlerMethodArgumentResolver> customArgumentResolvers = SpringUtil.getBean(RequestMappingHandlerAdapter.class).getCustomArgumentResolvers();
                customArgumentResolvers.add(new RequestJsonParamMethodArgumentResolver());

                builder.addPropertyValue("customArgumentResolvers", customArgumentResolvers);
                defaultListableBeanFactory.registerBeanDefinition("requestMappingHandlerAdapter", builder.getBeanDefinition());



            }
        };
    }


}
