package com.sa.oplog.aop;

import com.sa.dto.IDTO;
import com.sa.exception.ParamErrorException;
import com.sa.java.B;
import com.sa.oplog.annotation.LogParam;
import com.sa.oplog.annotation.OpLog;
import com.sa.oplog.base.LogContentProvider;
import com.sa.oplog.base.LogHandler;
import com.sa.oplog.base.LogInitializer;
import com.sa.oplog.dto.LogContext;
import com.sa.util.BeanConver;
import com.sa.util.ICustomThreadPoolExecutor;
import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.reflect.MethodSignature;
import org.beetl.core.GroupTemplate;
import org.beetl.core.Template;
import org.beetl.core.exception.BeetlException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.DependsOn;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.lang.annotation.Annotation;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;


@Component
@Aspect
@DependsOn("initConfig")
@ConditionalOnExpression("'${oplog.enable}'=='true'")
public class LogAspect {

    @Resource(name="StringGroupTemplate")
    GroupTemplate groupTemplate;

    @Value("${oplog.contentProvider:}")
    private String contentProvider;

    @Value("${oplog.handler:}")
    private String handler;

    @Value("${oplog.initializer:}")
    private String initializer;

    private ExecutorService executor;

    private static final Logger log = LoggerFactory.getLogger(LogAspect.class);

    Map<String, LogHandler> loghandlerCache = new HashMap<>();

    Map<String, LogInitializer> logInitializerCache = new HashMap<>();
    @PostConstruct
    public void init() throws IllegalAccessException, InstantiationException {
        System.out.println("操作日志启动");
        executor = ((Class<ICustomThreadPoolExecutor>) B.b.g("threadPoolExecutor")).newInstance().getCustomThreadPoolExecutor();
    }


    @Around( "@annotation(com.sa.oplog.annotation.OpLog)")
    public Object logAround(ProceedingJoinPoint point) throws Throwable {
        LogContext logContext = null;

        Method method = ((MethodSignature) point.getSignature()).getMethod();
        OpLog opLog = method.getAnnotation(OpLog.class);

        String initializer = StringUtils.isBlank(opLog.initializer()) ? this.initializer : opLog.initializer();
        if(StringUtils.isNotBlank(initializer)){
            if(!logInitializerCache.containsKey(initializer)){
                LogInitializer logInitializer = getObj(initializer, LogInitializer.class);
                logInitializerCache.put(initializer, logInitializer);
            }
            LogInitializer logInitializer = logInitializerCache.get(initializer);
            if(logInitializer != null){
                logContext = logInitializer.init(((MethodSignature) point.getSignature()).getMethod(), point.getArgs());
            }
        }
        LogContext finalLogContext = logContext;
        String content = getContent(point, finalLogContext);

        Object retValue = point.proceed();

        executor.execute(() -> {
            try {
                log(point, finalLogContext, content);
            } catch (Exception e) {
                log.error("操作日志异常:"+e.getMessage());
            }
        });
        return retValue;
    }


    private String getContent(ProceedingJoinPoint point, LogContext logContext){
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        OpLog opLog = method.getAnnotation(OpLog.class);
        String content = null;

        String cp = StringUtils.isBlank(opLog.contentProvider()) ? contentProvider : opLog.contentProvider();

        if(StringUtils.isNotBlank(cp)){
            LogContentProvider logContentProvider = null;
            try {
                logContentProvider = getObj(cp, LogContentProvider.class);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | BeansException e) {
                log.error(e.getMessage());
                return null;
            }
            content = logContentProvider.content(method, point.getArgs(), opLog.params(), logContext);
        }else{
            content = getBeetlContent(method, point.getArgs());
        }
        return content;
    }


    private void log(ProceedingJoinPoint point, LogContext logContext, String content) throws Exception {
        Method method = ((MethodSignature) point.getSignature()).getMethod();
        OpLog opLog = method.getAnnotation(OpLog.class);
        String handler = StringUtils.isBlank(opLog.handler()) ? this.handler : opLog.handler();

        if(StringUtils.isNotBlank(handler)) {
            LogHandler logHandler = getLogHandler(handler);
            if(logHandler != null){
                logHandler.log(content, method, point.getArgs(), opLog.params(), logContext);
            }
        }
    }


    private String getBeetlContent(Method method, Object[] args){
        OpLog opLog = method.getAnnotation(OpLog.class);
        Annotation[][] parameterAnnotations = method.getParameterAnnotations();
        Class<?>[] parameterTypes = method.getParameterTypes();

        String tpl = StringUtils.isBlank(opLog.value()) ? opLog.template() : opLog.value();
        if(StringUtils.isBlank(tpl)){
            log.warn("日志模板为空");
            return null;
        }

        if(StringUtils.isNotBlank(opLog.requiredExpr())){
            String head = "<% if(" + opLog.requiredExpr() + "){%>";
            String foot = "<%}%>";
            tpl = head + tpl + foot;
        }
        Template template = groupTemplate.getTemplate(tpl);
        Map<String, Object> params = null;
        try {

            params = getBindingMap(parameterAnnotations, parameterTypes, args);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }

        template.binding(params);

        BeetlException exception = template.validate();
        if(exception != null){
            log.error(exception.getMessage());
            return null;
        }
        return template.render();
    }


    private LogHandler getLogHandler(String logHandler) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
        if(!loghandlerCache.containsKey(logHandler)){
            LogHandler logHandlerInstance = null;
            try {
                logHandlerInstance = getObj(logHandler, LogHandler.class);
            } catch (ClassNotFoundException | IllegalAccessException | InstantiationException | BeansException e) {
                log.error(e.getMessage());
                return null;
            }
            loghandlerCache.put(logHandler, logHandlerInstance);
        }
        return loghandlerCache.get(logHandler);
    }


    private <T> T getObj(String objName, Class<T> clazz) throws ClassNotFoundException, IllegalAccessException, InstantiationException, BeansException {
        if(objName.contains(".")){
            Class objClass = Class.forName(objName);
            if(clazz.isAssignableFrom(objClass)){
                return (T) objClass.newInstance();
            }
            throw new ParamErrorException(objName + "不是" + clazz.getName() +"的实例");
        }else{
            T bean = null;
            try {

                bean = SpringUtil.getBean(objName, clazz);
            } catch (BeansException e) {
                throw e;
            }
            if(clazz.isAssignableFrom(bean.getClass())){
                return bean;
            }
            throw new ParamErrorException(objName + "不是" + clazz.getName() +"的实例");
        }
    }


    private Map<String, Object> getBindingMap(Annotation[][] parameterAnnotations, Class<?>[] parameterTypes, Object[] args) throws Exception {

        String inValidBindName = null;

        Map<String, Object> params = new HashMap<>();

        for(int i = 0; i < parameterAnnotations.length && StringUtils.isBlank(inValidBindName); i++){
            Annotation[] annotations = parameterAnnotations[i];
            for(Annotation annotation : annotations){
                if(annotation instanceof LogParam){
                    if(((LogParam) annotation).required() && args[i] == null){
                        String bindName = ((LogParam) annotation).value();
                        inValidBindName = StringUtils.isBlank(bindName) ? ((LogParam) annotation).bindName() : bindName;
                        break;
                    }
                    String bindName = ((LogParam) annotation).bindName();

                    if(List.class.isAssignableFrom(parameterTypes[i]) || parameterTypes[i].isArray()){
                        if(StringUtils.isBlank(bindName)){
                            bindName = "list";
                        }
                        params.put(bindName, args[i]);
                    }

                    else if(StringUtils.isBlank(bindName) && IDTO.class.isAssignableFrom(parameterTypes[i])){
                        params.putAll(BeanConver.transformObjectToMap(args[i]));
                    }else if(StringUtils.isBlank(bindName) && Map.class.isAssignableFrom(parameterTypes[i])){
                        params.putAll((Map)args[i]);
                    }else{
                        if(StringUtils.isBlank(bindName)){
                            bindName = "args["+i+"]";
                        }
                        if(null != args[i]) {
                            params.put(bindName, args[i]);
                        }
                    }
                }
            }
        }
        if(StringUtils.isNotBlank(inValidBindName)){
            throw new ParamErrorException("必填参数["+inValidBindName + "]为空");
        }
        return params;
    }


}
