package com.sa.gid.generator;

import org.springframework.stereotype.Component;




@Component
public class GIDShort extends GeneratorBase {

	private final double MS_TO_SEC = 0.001;

	
	private final long sequenceBits = 12L;

	
	private final long workerIdLeftShift = sequenceBits;
	
	private final long dcIdLeftShift = sequenceBits + workerIdBits;
	
	private final long timestampLeftShift = sequenceBits + workerIdBits + dcIdBits;

	
	private final long sequenceMask = -1L ^ (-1L << sequenceBits);

	
	private final long twepoch = 1451577600L;

	private long sequence = 0L;
	private long lastTimestamp = -1L;

	public synchronized long next() {

		long curTimestamp = (long) (System.currentTimeMillis() * MS_TO_SEC);


		if (lastTimestamp > curTimestamp) {
			throw new RuntimeException(
					String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
							lastTimestamp - curTimestamp));
		}


		if (lastTimestamp == curTimestamp) {

			sequence = (sequence + 1) & sequenceMask;


			if (sequence == 0) {

				curTimestamp = tilNextMillis((long) (lastTimestamp * MS_TO_SEC));
			}
		} else {


			sequence = 0L;
		}

		lastTimestamp = curTimestamp;


		return ((curTimestamp - twepoch) << timestampLeftShift) | (dcId << dcIdLeftShift)
				| (workerId << workerIdLeftShift) | sequence;
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = (long) (System.currentTimeMillis() * MS_TO_SEC);
		while (timestamp <= lastTimestamp) {
			timestamp = (long) (System.currentTimeMillis() * MS_TO_SEC);
		}

		return timestamp;
	}

}
