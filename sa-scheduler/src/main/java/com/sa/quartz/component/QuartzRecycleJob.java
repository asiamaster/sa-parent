package com.sa.quartz.component;

import com.sa.quartz.domain.QuartzConstants;
import com.sa.quartz.domain.ScheduleJob;
import com.sa.quartz.domain.ScheduleMessage;
import com.sa.quartz.service.ScheduleJobService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.ApplicationListener;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.stereotype.Component;

import java.util.List;


@Component
@ConditionalOnProperty(name = "quartz.enabled")
public class QuartzRecycleJob implements ApplicationListener<ContextRefreshedEvent> {

	private static final Logger log = LoggerFactory.getLogger(QuartzRecycleJob.class);

	@Autowired
	ScheduleJobService scheduleJobService;

	@Override
	public void onApplicationEvent(ContextRefreshedEvent contextRefreshedEvent) {


			List<ScheduleJob> scheduleJobs = scheduleJobService.list(null);
			for (ScheduleJob job : scheduleJobs) {

				if(!job.getJobStatus().equals(QuartzConstants.JobStatus.NORMAL.getCode())) {
					job.setJobStatus(QuartzConstants.JobStatus.NORMAL.getCode());
				}
				scheduleJobService.updateSelective(job);
				scheduleJobService.addJob(job, true);
			}

	}

	public void scan(ScheduleMessage scheduleMessage) {



























	}

	private boolean containsScheduleJob(List<ScheduleJob> scheduleJobs, ScheduleJob scheduleJob){
		for(ScheduleJob job : scheduleJobs){
			if(job.getId().equals(scheduleJob.getId())){
				return true;
			}
		}
		return false;
	}
}
