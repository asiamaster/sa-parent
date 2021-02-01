package com.sa.quartz.domain;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;


public class QuartzConstants {

    public static final Map<String, Integer> sheduelTimes = new ConcurrentHashMap<>();
    public static final String jobDataMapScheduleJobKey = "JOB_DATA_MAPSCHEDULE_JOB_KEY";


    public enum Concurrent {
        Sync(0,"同步"),
        Async(1,"异步");

        private Integer code ;

        private String desc;

        Concurrent(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static Concurrent getConcurrent(Integer code) {
            for (Concurrent concurrent : Concurrent.values()) {
                if (concurrent.getCode().equals(code)) {
                    return concurrent;
                }
            }
            return null;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }
    }

    public static enum JobStatus {
        NONE(0,"无"),
        NORMAL(1,"正常"),
        PAUSED(2,"暂停"),
        COMPLETE(3,"完成"),
        ERROR(4,"错误"),
        BLOCKED(5,"阻塞");

        private Integer code ;

        private String desc;

        JobStatus(int code, String desc) {
            this.code = code;
            this.desc = desc;
        }

        public static JobStatus getJobStatus(Integer code) {
            for (JobStatus jobStatus : JobStatus.values()) {
                if (jobStatus.getCode().equals(code)) {
                    return jobStatus;
                }
            }
            return null;
        }

        public Integer getCode() {
            return code;
        }

        public String getDesc() {
            return desc;
        }

    }

}
