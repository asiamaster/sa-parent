package com.sa.retrofitful;

import com.sa.domain.BaseDomain;
import com.sa.java.B;
import com.sa.retrofitful.annotation.Restful;
import com.sa.retrofitful.annotation.RestfulScan;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.util.ClassUtils;
import org.springframework.util.ResourceUtils;
import org.springframework.util.StringUtils;

import java.beans.Introspector;
import java.io.IOException;
import java.net.JarURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;


public class RetrofitfulRegistrar implements ImportBeanDefinitionRegistrar {

    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Override
    public void registerBeanDefinitions(AnnotationMetadata annotationMetadata, BeanDefinitionRegistry beanDefinitionRegistry) {
        B.daeif("script/ri", null, null);
        Set<String> basePackages = getBasePackages(annotationMetadata);
        for (String basePackage : basePackages) {
            Resource[] rootResources = getRootResources(basePackage);
            if(rootResources == null){
                break;
            }
            for(Resource rootResource : rootResources) {
                Resource[] resources = getResources(basePackage);
                for (Resource resource : resources) {
                    String classFullName = null;

                    try {

                        classFullName = getClassNameByResource(resource, rootResource.getURL(), basePackage);
                        if(StringUtils.isEmpty(classFullName)){
                            continue;
                        }
                        Class intfClass = Class.forName(classFullName);
                        if (!intfClass.isInterface() || intfClass.getAnnotation(Restful.class) == null) {
                            continue;
                        }





                        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
                        beanDefinition.setBeanClass(RestfulFactoryBean.class);
                        beanDefinition.getPropertyValues().add("intfClass", intfClass);
                        beanDefinition.setSynthetic(true);

                        beanDefinitionRegistry.registerBeanDefinition(buildDefaultBeanName(classFullName), beanDefinition);
                    } catch (IOException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    } catch (ClassNotFoundException e) {
                        logger.error(e.getMessage());
                        e.printStackTrace();
                    }
                }
            }
        }
    }

    public void initialize(ConfigurableApplicationContext configurableApplicationContext) {
        DefaultListableBeanFactory dbf = (DefaultListableBeanFactory) configurableApplicationContext.getBeanFactory();

        String basePackage = "";


        BeanDefinitionBuilder dataSourceBuider = BeanDefinitionBuilder.genericBeanDefinition(BaseDomain.class);
        dbf.registerBeanDefinition("baseDomain", dataSourceBuider.getBeanDefinition());
    }

    protected Set<String> getBasePackages(AnnotationMetadata importingClassMetadata) {
        Map attributes = importingClassMetadata.getAnnotationAttributes(RestfulScan.class.getCanonicalName());
        HashSet basePackages = new HashSet();
        String[] var4 = (String[]) ((String[]) attributes.get("value"));
        int var5 = var4.length;

        int var6;
        String clazz;
        for (var6 = 0; var6 < var5; ++var6) {
            clazz = var4[var6];
            if (StringUtils.hasText(clazz)) {
                basePackages.add(clazz);
            }
        }

        var4 = (String[]) ((String[]) attributes.get("basePackages"));
        var5 = var4.length;

        for (var6 = 0; var6 < var5; ++var6) {
            clazz = var4[var6];
            if (StringUtils.hasText(clazz)) {
                basePackages.add(clazz);
            }
        }

        Class[] var8 = (Class[]) ((Class[]) attributes.get("basePackageClasses"));
        var5 = var8.length;

        for (var6 = 0; var6 < var5; ++var6) {
            Class var9 = var8[var6];
            basePackages.add(ClassUtils.getPackageName(var9));
        }

        if (basePackages.isEmpty()) {
            basePackages.add(ClassUtils.getPackageName(importingClassMetadata.getClassName()));
        }

        return basePackages;
    }


    private Resource getRootResource(String basePackage) {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        String basePackagePath = ClassUtils.convertClassNameToResourcePath(basePackage);
        try {
            Resource[] resources = resourcePatternResolver.getResources("classpath*:" + basePackagePath + "/");
            return resources.length > 0 ? resources[0] : null;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }


    private Resource[] getRootResources(String basePackage) {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        String basePackagePath = ClassUtils.convertClassNameToResourcePath(basePackage);
        try {
            return resourcePatternResolver.getResources("classpath*:" + basePackagePath + "/");
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    private Resource[] getResources(String basePackage) {
        PathMatchingResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        String basePackagePath = ClassUtils.convertClassNameToResourcePath(basePackage);
        String resourcePattern = "**/*.class";
        String ex = "classpath*:" + basePackagePath + '/' + resourcePattern;
        try {
            return resourcePatternResolver.getResources(ex);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 根据资源、根URL和扫描包获取类全名
     */
    private String getClassNameByResource(Resource resource, URL rootDirURL, String basePackage) {
        JarFile jarFile = null;
        boolean closeJarFile = false;
        JarEntry entry;
        try {
            URLConnection con = resource.getURL().openConnection();
            if (con instanceof JarURLConnection) {
                JarURLConnection entries = (JarURLConnection) con;
                ResourceUtils.useCachesIfNecessary(entries);
                jarFile = entries.getJarFile();

                entry = entries.getJarEntry();
                String rootEntryPath = entry != null ? entry.getName() : "";
                String classFullPath = rootEntryPath.substring(0, rootEntryPath.length() - ".class".length());
                String classFullName = ClassUtils.convertResourcePathToClassName(classFullPath);

                closeJarFile = !entries.getUseCaches();
                return classFullName;
            } else {
                String resourcePath = resource.getURL().getPath();
                String rootDirPath = rootDirURL.getPath();
                int index = resourcePath.lastIndexOf(rootDirPath);

                if(index == -1){
                    return "";
                }
                String path = resourcePath.substring(index + rootDirPath.length() - basePackage.length() - 1);
                String classFullPath = path.substring(0, path.length() - ".class".length());
                return ClassUtils.convertResourcePathToClassName(classFullPath);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (closeJarFile) {
                try {
                    if(jarFile != null) {
                        jarFile.close();
                    }
                } catch (IOException e) {
                }
            }
        }
        return "";
    }



    private String buildDefaultBeanName(String className) {
        String shortClassName = ClassUtils.getShortName(className);
        return Introspector.decapitalize(shortClassName);
    }

}
