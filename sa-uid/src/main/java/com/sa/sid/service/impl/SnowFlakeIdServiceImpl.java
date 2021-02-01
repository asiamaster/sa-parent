





























package com.sa.sid.service.impl;


import com.sa.sid.consts.SnowflakeIdMeta;
import com.sa.sid.dto.SnowflakeId;
import com.sa.sid.service.SnowFlakeIdService;
import com.sa.sid.service.SnowflakeIdConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Date;


public class SnowFlakeIdServiceImpl implements SnowFlakeIdService {

    private static long lastTimestamp = -1L;

    private static long sequence = 0L;

    private final long workerId;

    private final long datacenterId;

    protected static final Logger log = LoggerFactory.getLogger(SnowFlakeIdServiceImpl.class);

    private SnowflakeIdConverter snowflakeIdConverter;

    public SnowFlakeIdServiceImpl(long datacenterId, long workerId, SnowflakeIdConverter snowflakeIdConverter) {
        if (workerId > SnowflakeIdMeta.MAX_ID || workerId < 0) {
            throw new IllegalArgumentException(String.format("worker Id can't be greater than %d or less than 0", SnowflakeIdMeta.MAX_ID));
        }
        this.workerId = workerId;
        this.datacenterId = datacenterId;
        this.snowflakeIdConverter = snowflakeIdConverter;
        log.info("worker starting. timestamp left shift {},  datacenterId id bits {}, worker id bits {}, sequence bits {}, datacenterId {}, workerid {}", SnowflakeIdMeta.TIMESTAMP_LEFT_SHIFT_BITS, SnowflakeIdMeta.DATACENTER_ID_BITS, SnowflakeIdMeta.WORKER_ID_BITS, SnowflakeIdMeta.SEQUENCE_BITS, datacenterId, workerId);
    }


    @Override
    public synchronized long nextId() {
        long timestamp = timeGen();


        validateTimestamp(timestamp, lastTimestamp);


        if (lastTimestamp == timestamp) {
            sequence = (sequence + 1) & SnowflakeIdMeta.SEQUENCE_MASK;

            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }

        lastTimestamp = timestamp;


        return ((timestamp - SnowflakeIdMeta.START_TIME) << SnowflakeIdMeta.TIMESTAMP_LEFT_SHIFT_BITS) | (datacenterId << SnowflakeIdMeta.DATACENTER_ID_SHIFT_BITS) | (workerId << SnowflakeIdMeta.ID_SHIFT_BITS) | sequence;
    }


    @Override
    public SnowflakeId expId(long id) {
        return snowflakeIdConverter.convert(id);
    }


    @Override
    public Date transTime(long time) {
        return new Date(time + SnowflakeIdMeta.START_TIME);
    }


    @Override
    public long makeId(long timeStamp, long sequence) {
        return makeId(timeStamp, datacenterId, workerId, sequence);
    }


    @Override
    public long makeId(long timeStamp, long datacenter, long worker, long sequence) {
        return snowflakeIdConverter.convert(new SnowflakeId(timeStamp, datacenter, worker, sequence));
    }



    private void validateTimestamp(long timestamp, long lastTimestamp) {
        if (timestamp < lastTimestamp) {
            log.error(String.format("clock is moving backwards.  Rejecting requests until %d.", lastTimestamp));
            throw new IllegalStateException(String.format("Clock moved backwards.  Refusing to generate id for %d milliseconds", lastTimestamp - timestamp));
        }
    }


    private long tilNextMillis(long lastTimestamp) {
        long timestamp = timeGen();
        while (timestamp <= lastTimestamp) {
            timestamp = timeGen();
        }
        return timestamp;
    }


    private long timeGen() {
        return System.currentTimeMillis();
    }

}
