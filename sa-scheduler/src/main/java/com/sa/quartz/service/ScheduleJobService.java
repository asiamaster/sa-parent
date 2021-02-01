package com.sa.quartz.service;


import com.sa.base.BaseService;
import com.sa.quartz.domain.ScheduleJob;
import org.quartz.SchedulerException;

import java.util.List;


public interface ScheduleJobService extends BaseService<ScheduleJob, Long> {


	void addJob(ScheduleJob scheduleJob, boolean overwrite);


	void resumeJob(ScheduleJob scheduleJob);


	void pauseJob(ScheduleJob scheduleJob);


	void refresh(ScheduleJob scheduleJob);


	void refresh(List<ScheduleJob> scheduleJobs);


	List<ScheduleJob> getAllJob() throws SchedulerException;


	List<ScheduleJob> getRunningJob() throws SchedulerException;


	void deleteJob(ScheduleJob scheduleJob) throws SchedulerException;


	int update(ScheduleJob job, boolean schedule);


	int updateSelective(ScheduleJob job, boolean schedule);


	int batchUpdateSelective(List<ScheduleJob> list, boolean schedule);


	int delete(List<Long> ids, boolean schedule);


	int delete(Long id, boolean schedule);

	int insert(ScheduleJob scheduleJob, boolean schedule);

	int insertSelective(ScheduleJob scheduleJob, boolean schedule);

	int batchInsert(List<ScheduleJob> list, boolean schedule);
}