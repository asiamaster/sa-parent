package com.sa.quartz.domain;

import java.io.Serializable;


public class ScheduleMessage implements Serializable {

    private Integer sheduelTimes;

    private String jobGroup;

    private String jobName;

    private String jobData;

    public String getJobGroup() {
        return jobGroup;
    }

    public void setJobGroup(String jobGroup) {
        this.jobGroup = jobGroup;
    }

    public String getJobName() {
        return jobName;
    }

    public void setJobName(String jobName) {
        this.jobName = jobName;
    }

    public Integer getSheduelTimes() {
        return sheduelTimes;
    }

    public void setSheduelTimes(Integer sheduelTimes) {
        this.sheduelTimes = sheduelTimes;
    }

    public String getJobData() {
        return jobData;
    }

    public void setJobData(String jobData) {
        this.jobData = jobData;
    }

}
