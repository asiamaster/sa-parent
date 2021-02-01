package com.sa.mvc.boot;

import com.sa.mvc.servlet.CSRFInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;

import javax.annotation.Resource;


@Configuration
@ConditionalOnExpression("'${CSRFInterceptor.enable}'=='true'")
public class CSRFInterceptorConfig extends WebMvcConfigurationSupport {

    @Autowired
    private CSRFInterceptorProperties csrfInterceptorProperties;
    @Resource
    private CSRFInterceptor csrfInterceptor;


    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        if (csrfInterceptorProperties.getEnable()) {
            registry.addInterceptor(csrfInterceptor)
                    .addPathPatterns(csrfInterceptorProperties.getPaths().toArray(new String[csrfInterceptorProperties.getPaths().size()]))
                    .excludePathPatterns(csrfInterceptorProperties.getExcludePaths().toArray(new String[csrfInterceptorProperties.getExcludePaths().size()]));
        }
    }
}