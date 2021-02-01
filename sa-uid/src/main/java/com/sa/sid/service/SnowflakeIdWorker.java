





























package com.sa.sid.service;

import java.util.Date;


public class SnowflakeIdWorker {



















    private final long START_TIME = 1546272000000L;


    private final long WORKER_ID_BITS = 5L;


    private final long DATACENTER_ID_BITS = 5L;


    private final long MAX_WORKER_ID = -1L ^ (-1L << WORKER_ID_BITS);


    private final long MAX_DATACENTER_ID = -1L ^ (-1L << DATACENTER_ID_BITS);


    private final long SEQUENCE_BITS = 12L;


    private final long WORKER_ID_SHIFT_BITS = SEQUENCE_BITS;


    private final long DATACENTER_ID_SHIFT_BITS = SEQUENCE_BITS + WORKER_ID_BITS;


    private final long TIMESTAMP_LEFT_SHIFT_BITS = SEQUENCE_BITS + WORKER_ID_BITS + DATACENTER_ID_BITS;


    private final long SEQUENCE_MASK = -1L ^ (-1L << SEQUENCE_BITS);


    private long workerId;


    private long datacenterId;


    private long sequence = 0L;


    private long lastTimestamp = -1L;




    public SnowflakeIdWorker(long workerId, long datacenterId) {
        if (workerId > MAX_WORKER_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", MAX_WORKER_ID));
        }
        if (datacenterId > MAX_DATACENTER_ID || datacenterId < 0) {
            throw new IllegalArgumentException(String.format("datacenter Id can't be greater than %d or less than 0", MAX_DATACENTER_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
    }




    public synchronized long nextId() {
        long timestamp = timeGen();


        if (timestamp < lastTimestamp) {
            throw new RuntimeException(
                    String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }


        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SEQUENCE_MASK;

            if (sequence == 0) {

                timestamp = tilNextMillis(lastTimestamp);
            }
        }

        else {
            sequence = 0L;
        }


        lastTimestamp = timestamp;


        return ((timestamp - START_TIME) << TIMESTAMP_LEFT_SHIFT_BITS)
                | (datacenterId << DATACENTER_ID_SHIFT_BITS)
                | (workerId << WORKER_ID_SHIFT_BITS)
                | sequence;
    }


    public Date transTime(long time) {
        return new Date(time + START_TIME);
    }




    protected long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }


    protected long timeGen() {
        return System.currentTimeMillis();
    }

}