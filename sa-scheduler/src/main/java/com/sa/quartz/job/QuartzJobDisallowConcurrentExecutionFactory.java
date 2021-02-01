package com.sa.quartz.job;

import com.github.rholder.retry.*;
import com.google.common.base.Predicates;
import com.sa.quartz.TaskUtils;
import com.sa.quartz.domain.QuartzConstants;
import com.sa.quartz.domain.ScheduleJob;
import com.sa.quartz.domain.ScheduleMessage;
import com.sa.quartz.listener.SchedulerRetryListener;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;



@DisallowConcurrentExecution
public class QuartzJobDisallowConcurrentExecutionFactory implements Job {

    protected Logger log = LoggerFactory.getLogger(this.getClass());
    @Override
    public void execute(JobExecutionContext jobExecutionContext) {
        ScheduleJob scheduleJob = (ScheduleJob) jobExecutionContext.getMergedJobDataMap().get(QuartzConstants.jobDataMapScheduleJobKey);
        ScheduleMessage scheduleMessage = new ScheduleMessage();
        scheduleMessage.setJobData(scheduleJob.getJobData());

        invoke(scheduleJob, scheduleMessage);
    }


    private void invoke(ScheduleJob scheduleJob, ScheduleMessage scheduleMessage) {

        if(scheduleJob.getRetryCount() == null || scheduleJob.getRetryCount() < 0){
            scheduleJob.setRetryCount(0);
        }

        if(scheduleJob.getRetryInterval() == null){
            scheduleJob.setRetryInterval(1000L);
        }

        Retryer<Boolean> retryer = RetryerBuilder.<Boolean> newBuilder()

                .retryIfException()

                .retryIfResult(Predicates.equalTo(false))

                .withStopStrategy(StopStrategies.stopAfterAttempt(scheduleJob.getRetryCount() + 1))

                .withWaitStrategy(WaitStrategies.fixedWait(scheduleJob.getRetryInterval(), TimeUnit.MILLISECONDS))
                .withRetryListener(new SchedulerRetryListener(scheduleJob))
                .build();
        try {
            retryer.call(()-> {
                return TaskUtils.invokeMethod(scheduleJob, scheduleMessage);
            });
        } catch (RetryException | ExecutionException e) {
            log.error(ExceptionUtils.getStackTrace(e));
        }
    }






























}
