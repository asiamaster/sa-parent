





























package com.sa.sid.dto;

import java.io.Serializable;

public class SnowflakeId implements Serializable {
    private long timeStamp;

    private long workerId;


    private long datacenterId;

    private long sequence;

    public SnowflakeId() {
    }

    public SnowflakeId(long timeStamp, long datacenterId, long workerId, long sequence) {
        this.timeStamp = timeStamp;
        this.datacenterId = datacenterId;
        this.workerId = workerId;
        this.sequence = sequence;
    }

    public long getTimeStamp() {
        return timeStamp;
    }

    public void setTimeStamp(long timeStamp) {
        this.timeStamp = timeStamp;
    }

    public long getWorkerId() {
        return workerId;
    }

    public void setWorkerId(long workerId) {
        this.workerId = workerId;
    }

    public long getDatacenterId() {
        return datacenterId;
    }

    public void setDatacenterId(long datacenterId) {
        this.datacenterId = datacenterId;
    }

    public long getSequence() {
        return sequence;
    }

    public void setSequence(long sequence) {
        this.sequence = sequence;
    }
}
