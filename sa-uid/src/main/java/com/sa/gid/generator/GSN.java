package com.sa.gid.generator;

import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;


@Component
public class GSN extends GeneratorBase {

	
	private final long maxSequence = 9999L;

	
	private final SimpleDateFormat df = new SimpleDateFormat("yyyyMMddHHmmssSSS");

	private long sequence = 0L;
	private long lastTimestamp = -1L;

	public synchronized String next() {

		long curTimestamp = System.currentTimeMillis();


		if (lastTimestamp > curTimestamp) {
			throw new RuntimeException(
					String.format("Clock moved backwards. Refusing to generate id for %d milliseconds",
							lastTimestamp - curTimestamp));
		}


		if (lastTimestamp == curTimestamp) {

			if (++sequence > maxSequence) {
				sequence = 0L;


				curTimestamp = tilNextMillis(lastTimestamp);
			}
		} else {


			sequence = 0L;
		}

		lastTimestamp = curTimestamp;


		return df.format(new Date(curTimestamp)) + String.format("%03d%04d", workerId, sequence);
	}

	private long tilNextMillis(long lastTimestamp) {
		long timestamp = System.currentTimeMillis();
		while (timestamp <= lastTimestamp) {
			timestamp = System.currentTimeMillis();
		}

		return timestamp;
	}

}
