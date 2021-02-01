package com.sa.beetl.boot;

import com.sa.beetl.CommonTagFactory;
import org.beetl.core.Format;
import org.beetl.core.Function;
import org.beetl.core.GroupTemplate;
import org.beetl.core.VirtualAttributeEval;
import org.beetl.core.resource.ClasspathResourceLoader;
import org.beetl.core.resource.StringTemplateResourceLoader;
import org.beetl.core.tag.Tag;
import org.beetl.core.tag.TagFactory;
import org.beetl.ext.spring.BeetlGroupUtilConfiguration;
import org.beetl.ext.spring.BeetlSpringViewResolver;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternUtils;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;


@Configuration
@ConditionalOnExpression("'${beetl.enable}'=='true'")
@SuppressWarnings("all")
public class BeetlConfig {

    @Autowired
    private List<VirtualAttributeEval> virtualAttributeEval;
    @Autowired
    private Map<String, Function> functions;
    @Autowired
    private Map<String, Tag> tags;
    @Autowired
    Map<String, Format> formats;

    @Value("${server.servlet.context-path:}")
    private String contextPath;
    @Value("${beetl.templatesPath:templates}")
    String templatesPath;


    @Bean("StringGroupTemplate")
    public GroupTemplate getStringGroupTemplate() throws IOException {
        BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = buildBeetlGroupUtilConfiguration();
        beetlGroupUtilConfiguration.setResourceLoader(new StringTemplateResourceLoader());
        beetlGroupUtilConfiguration.init();
        return beetlGroupUtilConfiguration.getGroupTemplate();
    }

    @Bean(initMethod = "init", name = "beetlGroupUtilConfiguration")
    public BeetlGroupUtilConfiguration getBeetlGroupUtilConfiguration() {
        BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = buildBeetlGroupUtilConfiguration();


        ClassLoader loader = Thread.currentThread().getContextClassLoader();
        if(loader==null){
            loader = BeetlConfig.class.getClassLoader();
        }


        ClasspathResourceLoader classpathResourceLoader = new ClasspathResourceLoader(loader, templatesPath);
        beetlGroupUtilConfiguration.setResourceLoader(classpathResourceLoader);
        beetlGroupUtilConfiguration.init();

        beetlGroupUtilConfiguration.getGroupTemplate().setClassLoader(loader);
        return beetlGroupUtilConfiguration;
    }

    @Bean(name = "beetlViewResolver")
    public BeetlSpringViewResolver getBeetlSpringViewResolver(@Qualifier("beetlGroupUtilConfiguration") BeetlGroupUtilConfiguration beetlGroupUtilConfiguration) {
        BeetlSpringViewResolver beetlSpringViewResolver = new BeetlSpringViewResolver();
        beetlSpringViewResolver.setContentType("text/html;charset=UTF-8");
        beetlSpringViewResolver.setOrder(0);
        beetlSpringViewResolver.setPrefix("/");
        beetlSpringViewResolver.setSuffix(".html");
        beetlSpringViewResolver.setConfig(beetlGroupUtilConfiguration);
        beetlSpringViewResolver.setRedirectHttp10Compatible(false);
        return beetlSpringViewResolver;
    }


    private BeetlGroupUtilConfiguration buildBeetlGroupUtilConfiguration() {
        BeetlGroupUtilConfiguration beetlGroupUtilConfiguration = new BeetlGroupUtilConfiguration();













        ResourcePatternResolver patternResolver = ResourcePatternUtils.getResourcePatternResolver(new DefaultResourceLoader());
        beetlGroupUtilConfiguration.setConfigFileResource(patternResolver.getResource("classpath:conf/beetl.properties"));
        InputStream inputStream = BeetlConfig.class.getResourceAsStream("/conf/beetlSharedVars.properties");
        Properties p = new Properties();
        try {
            if(inputStream != null) {


                p.load(new InputStreamReader(inputStream, "UTF-8"));
                inputStream.close();
            }
        } catch (IOException e1) {
            e1.printStackTrace();
        }
        p.put("contextPath", "${server.servlet.context-path}".equals(contextPath) ?"":contextPath);
        beetlGroupUtilConfiguration.setSharedVars((Map)p);
        beetlGroupUtilConfiguration.setVirtualAttributeEvals(virtualAttributeEval);
        beetlGroupUtilConfiguration.setFunctions(functions);
        beetlGroupUtilConfiguration.setFormats(formats);
        beetlGroupUtilConfiguration.setTagFactorys(getTagFactoryMaps());
        return beetlGroupUtilConfiguration;
    }

    private Map<String, TagFactory> getTagFactoryMaps(){
        Map<String, TagFactory> tagFactoryMap = new HashMap<>(tags.size());
        for(Map.Entry<String, Tag> entry : tags.entrySet()) {
            tagFactoryMap.put(entry.getKey(), new CommonTagFactory(entry.getValue()));
        }
        return tagFactoryMap;
    }



}
