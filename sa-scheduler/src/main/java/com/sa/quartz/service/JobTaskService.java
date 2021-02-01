package com.sa.quartz.service;

import com.sa.dto.DTOUtils;
import com.sa.quartz.domain.QuartzConstants;
import com.sa.quartz.domain.ScheduleJob;
import com.sa.quartz.job.QuartzJobDisallowConcurrentExecutionFactory;
import com.sa.quartz.job.QuartzJobFactory;
import com.sa.util.SpringUtil;
import org.apache.commons.lang3.StringUtils;
import org.quartz.*;
import org.quartz.impl.matchers.GroupMatcher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.scheduling.quartz.SchedulerFactoryBean;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;


@Service
@ConditionalOnProperty(name = "quartz.enabled")

public class JobTaskService implements ApplicationListener<ContextRefreshedEvent> {

    protected Logger log = LoggerFactory.getLogger(this.getClass());

    @Value("${quartz.enabled:}")
    private String quartzEnabled;


    private SchedulerFactoryBean schedulerFactoryBean;

    @Override
    public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {
        if(SpringUtil.getApplicationContext().containsBean("schedulerFactoryBean")
                && "true".equals(quartzEnabled)) {
            schedulerFactoryBean = (SchedulerFactoryBean) SpringUtil.getBean(SchedulerFactoryBean.class);
        }













































    }


    public void addJob(ScheduleJob job, boolean overwrite) throws SchedulerException {

        if (job == null) {
            return;
        }
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        log.debug(scheduler + ".......................................................................................add");
        TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
        Trigger trigger = scheduler.getTrigger(triggerKey);

        if (null == trigger) {
            Class clazz = QuartzConstants.Concurrent.Async.getCode().equals(job.getIsConcurrent()) ? QuartzJobFactory.class : QuartzJobDisallowConcurrentExecutionFactory.class;
            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).build();
            jobDetail.getJobDataMap().put(QuartzConstants.jobDataMapScheduleJobKey, job);
            ScheduleBuilder scheduleBuilder = null;

            if (StringUtils.isBlank(job.getCronExpression())) {
                scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(job.getRepeatInterval());
            } else {
                scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
            }
            if (job.getStartDelay() != null && job.getStartDelay() > 0) {
                Long startDelayTime = System.currentTimeMillis() + (job.getStartDelay() * 1000);
                trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup()).startAt(new Date(startDelayTime)).withSchedule(scheduleBuilder).build();
            } else {
                trigger = TriggerBuilder.newTrigger().withIdentity(job.getJobName(), job.getJobGroup()).withSchedule(scheduleBuilder).build();
            }
            scheduler.scheduleJob(jobDetail, trigger);
        } else {

            if (StringUtils.isBlank(job.getCronExpression())) {

                SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(job.getRepeatInterval());
                SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
                trigger = simpleTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            } else {

                CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
                CronTrigger cronTrigger = (CronTrigger) trigger;

                trigger = cronTrigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
            }
            Class clazz = QuartzConstants.Concurrent.Async.getCode().equals(job.getIsConcurrent()) ? QuartzJobFactory.class : QuartzJobDisallowConcurrentExecutionFactory.class;
            JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).storeDurably(true).build();


            jobDetail.getJobDataMap().put(QuartzConstants.jobDataMapScheduleJobKey, job);

            if(!overwrite) {
                ScheduleJob currentScheduleJob = (ScheduleJob)scheduler.getJobDetail(JobKey.jobKey(job.getJobName(),job.getJobGroup())).getJobDataMap().get(QuartzConstants.jobDataMapScheduleJobKey);
                job.setJobData(currentScheduleJob.getJobData());
            }

            scheduler.addJob(jobDetail, true);

            scheduler.rescheduleJob(triggerKey, trigger);
        }
    }


    public List<ScheduleJob> getRunningJob() throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        List<JobExecutionContext> executingJobs = scheduler.getCurrentlyExecutingJobs();
        List<ScheduleJob> jobList = new ArrayList<ScheduleJob>(executingJobs.size());
        for (JobExecutionContext executingJob : executingJobs) {
            ScheduleJob job = DTOUtils.newDTO(ScheduleJob.class);
            JobDetail jobDetail = executingJob.getJobDetail();
            JobKey jobKey = jobDetail.getKey();
            Trigger trigger = executingJob.getTrigger();
            job.setJobName(jobKey.getName());
            job.setJobGroup(jobKey.getGroup());
            job.setDescription("触发器:" + trigger.getKey());

            Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());

            job.setJobStatus(triggerState.ordinal());
            if (trigger instanceof CronTrigger) {
                CronTrigger cronTrigger = (CronTrigger) trigger;
                String cronExpression = cronTrigger.getCronExpression();
                job.setCronExpression(cronExpression);
            }else{
                SimpleTrigger simpleTrigger = (SimpleTrigger) trigger;
                job.setRepeatInterval(new Long(simpleTrigger.getRepeatInterval()).intValue()/1000);
            }
            jobList.add(job);
        }
        return jobList;
    }


    public Trigger getTrigger(String triggerName, String triggerGroup){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            return scheduler.getTrigger(TriggerKey.triggerKey(triggerName, triggerGroup));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }


    public JobDetail getJobDetail(String jobName, String jobGroup){
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        try {
            return scheduler.getJobDetail(JobKey.jobKey(jobName, jobGroup));
        } catch (SchedulerException e) {
            e.printStackTrace();
        }
        return null;
    }


    public void pauseJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.pauseJob(jobKey);
    }


    public void resumeJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.resumeJob(jobKey);
    }


    public void deleteJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.deleteJob(jobKey);

    }


    public List<ScheduleJob> getAllJob() throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        GroupMatcher<JobKey> matcher = GroupMatcher.anyJobGroup();
        Set<JobKey> jobKeys = scheduler.getJobKeys(matcher);
        List<ScheduleJob> jobList = new ArrayList<ScheduleJob>();
        for (JobKey jobKey : jobKeys) {
            JobDetail jobDetail = scheduler.getJobDetail(jobKey);
            ScheduleJob scheduleJob = (ScheduleJob) jobDetail.getJobDataMap().get(QuartzConstants.jobDataMapScheduleJobKey);
            List<? extends Trigger> triggers = scheduler.getTriggersOfJob(jobKey);
            for (Trigger trigger : triggers) {
                if(jobKey.equals(trigger.getJobKey())){
                    scheduleJob.setJobStatus(scheduler.getTriggerState(trigger.getKey()).ordinal());
                }
            }
            jobList.add(scheduleJob);
        }
        return jobList;























    }


    public void runAJobNow(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        JobKey jobKey = JobKey.jobKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        scheduler.triggerJob(jobKey);
    }


    public void updateJob(ScheduleJob scheduleJob) throws SchedulerException {
        Scheduler scheduler = schedulerFactoryBean.getScheduler();
        TriggerKey triggerKey = TriggerKey.triggerKey(scheduleJob.getJobName(), scheduleJob.getJobGroup());
        Trigger trigger = null;





        trigger = scheduler.getTrigger(triggerKey);

        if(trigger == null) {
            addJob(scheduleJob, true);
            return;
        }

        if (StringUtils.isBlank(scheduleJob.getCronExpression())) {

            SimpleScheduleBuilder scheduleBuilder = SimpleScheduleBuilder.repeatSecondlyForever(scheduleJob.getRepeatInterval());


            trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).forJob(scheduleJob.getJobName(), scheduleJob.getJobGroup()).build();
            trigger.getJobDataMap().put(QuartzConstants.jobDataMapScheduleJobKey, scheduleJob);
        } else {

            CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(scheduleJob.getCronExpression());



            trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).forJob(scheduleJob.getJobName(), scheduleJob.getJobGroup()).build();
            trigger.getJobDataMap().put(QuartzConstants.jobDataMapScheduleJobKey, scheduleJob);
        }
        scheduler.rescheduleJob(triggerKey, trigger);
    }

}
