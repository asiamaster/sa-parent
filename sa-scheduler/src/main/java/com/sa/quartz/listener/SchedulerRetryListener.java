package com.sa.quartz.listener;

import com.github.rholder.retry.Attempt;
import com.github.rholder.retry.RetryListener;
import com.sa.exception.ParamErrorException;
import com.sa.quartz.base.RecoveryCallback;
import com.sa.quartz.domain.ScheduleJob;
import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.BeansException;


public class SchedulerRetryListener implements RetryListener {
    protected Logger log = LoggerFactory.getLogger(this.getClass());
    private ScheduleJob scheduleJob;
    public SchedulerRetryListener(ScheduleJob scheduleJob){
        this.scheduleJob = scheduleJob;
    }
    @Override
    public <T> void onRetry(Attempt<T> attempt) {

       if(attempt.getAttemptNumber() == 1){
           return;
       }

        if (attempt.hasException()) {
            log.warn("第"+attempt.getAttemptNumber()+"次调度异常:" + attempt.getExceptionCause().toString());
        }

       if(attempt.getAttemptNumber() >= scheduleJob.getRetryCount() + 1 && StringUtils.isNotBlank(scheduleJob.getRecoveryCallback())){
           try {
               RecoveryCallback recoveryCallback = getObj(scheduleJob.getRecoveryCallback(), RecoveryCallback.class);
               recoveryCallback.recover(attempt, scheduleJob);
           } catch (Exception e) {
               log.error(ExceptionUtils.getStackTrace(e));
           }

       }









    }

    
    private <T> T getObj(String objName, Class<T> clazz) throws ClassNotFoundException, IllegalAccessException, InstantiationException, BeansException, ParamErrorException {
        if(objName.contains(".")){
            Class objClass = Class.forName(objName);
            if(clazz.isAssignableFrom(objClass)){
                return (T) objClass.newInstance();
            }
            throw new ParamErrorException(objName + "不是" + clazz.getName() +"的实例");
        }else{

            T bean = SpringUtil.getBean(objName, clazz);
            if(clazz.isAssignableFrom(bean.getClass())){
                return bean;
            }
            throw new ParamErrorException(objName + "不是" + clazz.getName() +"的实例");
        }
    }

}
