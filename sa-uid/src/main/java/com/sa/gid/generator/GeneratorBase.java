package com.sa.gid.generator;


public abstract class GeneratorBase {


    final static long dcIdBits = 2L;

    final static long workerIdBits = 10L - dcIdBits;


    final static long dcId = 0L;

    static long workerId;

    public static long getWorkerIdBits() {
        return workerIdBits;
    }

    public static void setWorkerId(long workerId) {
        GeneratorBase.workerId = workerId;
    }
}
