





























package com.sa.sid.consts;

public class SnowflakeIdMeta {

    public static final long START_TIME = 1546272000000L;

    public static final long ID_BITS = 10L;

    public static final long MAX_ID = ~(-1L << ID_BITS);

    public static final long SEQUENCE_BITS = 12L;

    public static final long ID_SHIFT_BITS = SEQUENCE_BITS;

    public static final long WORKER_ID_SHIFT_BITS = SEQUENCE_BITS;

    public static final long WORKER_ID_BITS = 5L;

    public static final long DATACENTER_ID_BITS = 5L;

    public static final long DATACENTER_ID_SHIFT_BITS = SEQUENCE_BITS + WORKER_ID_BITS;


    public static final long TIMESTAMP_LEFT_SHIFT_BITS = SEQUENCE_BITS + ID_BITS;


    public static final long WORKER_ID_MASK = ~(-1L << WORKER_ID_BITS);


    public static final long SEQUENCE_MASK = ~(-1L << SEQUENCE_BITS);

    public static final long ID_MASK = ~(-1L << ID_BITS);

    public static final long TIMESTAMP_MASK = ~(-1L << 41L);


    private SnowflakeIdMeta() {
    }
}
