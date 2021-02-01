package com.sa.quartz.base;

import com.github.rholder.retry.Attempt;
import com.sa.quartz.domain.ScheduleJob;


public interface RecoveryCallback {

    void recover(Attempt attempt, ScheduleJob scheduleJob);
}
