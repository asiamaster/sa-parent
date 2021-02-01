package com.sa.mvc.boot;

import freemarker.template.TemplateException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;


@Configuration
@ConditionalOnExpression("'${freemarker.enable}'=='true'")
public class FreeMarkerConfig {

    @Autowired
    protected freemarker.template.Configuration configuration;
    @Autowired
    protected org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver resolver;
    @Autowired
    protected org.springframework.web.servlet.view.InternalResourceViewResolver springResolver;


    @PostConstruct
    public void  setSharedVariable(){
        configuration.setDateFormat("yyyy/MM/dd");
        configuration.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");







        try {
            configuration.setSetting("template_update_delay", "1");
            configuration.setSetting("default_encoding", "UTF-8");
        } catch (TemplateException e) {
            e.printStackTrace();
        }


        springResolver.setPrefix("/templates/");
        springResolver.setSuffix(".jsp");
        springResolver.setOrder(2);


        resolver.setSuffix(".ftl");
        resolver.setCache(true);
        resolver.setRequestContextAttribute("request");
        resolver.setOrder(0);

    }



}
