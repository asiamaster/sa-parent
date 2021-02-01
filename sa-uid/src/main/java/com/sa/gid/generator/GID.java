package com.sa.gid.generator;

import org.springframework.stereotype.Component;


@Component
public class GID  extends GeneratorBase{

	
	private final long sequenceBits = 12L;

	
	private final long workerIdLeftShift = sequenceBits;
	
	private final long dcIdLeftShift = sequenceBits + workerIdBits;
	
	private final long timestampLeftShift = sequenceBits + workerIdBits + dcIdBits;

	
	private final long sequenceMask = -1L ^ (-1L << sequenceBits);

	private final long twepoch = 1288834974657L;

	private long sequence = 0L;
	private long lastTimestamp = -1L;

	public synchronized long next() {

		long curTimestamp = System.currentTimeMillis();


		if (lastTimestamp > curTimestamp) {
			throw new RuntimeException(
					String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
							lastTimestamp - curTimestamp));
		}


		if (lastTimestamp == curTimestamp) {

			sequence = (sequence + 1) & sequenceMask;


			if (sequence == 0) {

				curTimestamp = tilNextMillis(lastTimestamp);
			}
		} else {


			sequence = 0L;
		}

		lastTimestamp = curTimestamp;


		return ((curTimestamp - twepoch) << timestampLeftShift) | (dcId << dcIdLeftShift)
				| (workerId << workerIdLeftShift) | sequence;
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = System.currentTimeMillis();
		while (timestamp <= lastTimestamp) {
			timestamp = System.currentTimeMillis();
		}

		return timestamp;
	}

}
