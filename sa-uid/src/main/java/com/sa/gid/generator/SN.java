package com.sa.gid.generator;

import org.apache.commons.lang3.RandomStringUtils;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;


@Component
public class SN {


	private final Map<Integer, Long> map = new HashMap<>();

	private long sequence = 0L;

    public static void main(String[] args) {
        SN sn = new SN();
        for (int i=0; i<10; i++) {
            System.out.println(sn.nextRsn(2));
        }
    }
	public SN() {
		long maxVal = 1L;
		for (int len = 1; len < 19; ++len) {
			maxVal *= 10;
			map.put(len, maxVal);
		}
	}


	public synchronized String nextSn(int len) {

		if (++sequence >= map.get(len)) {
			sequence = 1L;
		}

		return String.format("%0" + len + "d", sequence);
	}


	public synchronized String nextRsn(int len) {
		return String.format("%0" + len + "d", RandomUtils.nextLong(1, map.get(len)));
	}


	public synchronized String nextRc(int len) {
		return RandomStringUtils.randomAlphanumeric(len).replaceAll("O", "0").replaceAll("l", "L");
	}

}
