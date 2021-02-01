





























package com.sa.sid.util;

import com.sa.sid.dto.SnowflakeId;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public final class IdUtils {

    private static final Logger logger = LoggerFactory.getLogger(IdUtils.class);

    private static final Pattern PATTERN_LONG_ID = Pattern.compile("^([0-9]{15})([0-9a-f]{32})([0-9a-f]{3})$");

    private static final Pattern PATTERN_HOSTNAME = Pattern.compile("^.*\\D+([0-9]+)$");

    private static final long OFFSET = LocalDate.of(2000, 1, 1).atStartOfDay(ZoneId.of("Z")).toEpochSecond();

    private static final long MAX_NEXT = 0b11111_11111111_111L;

    private static final long SHARD_ID = getServerIdAsLong();

    private static long offset = 0;

    private static long lastEpoch = 0;


    private static final long SHARD_ID_MASK = ~(-1L << 5);
    private static final long SEQUENCE_MASK = ~(-1L << 16);
    private static final long TIMESTAMP_MASK = ~(-1L << 32);



    public static long stringIdToLongId(String stringId) {
        Matcher matcher = PATTERN_LONG_ID.matcher(stringId);
        if (matcher.matches()) {
            long epoch = Long.parseLong(matcher.group(1)) / 1000;
            String uuid = matcher.group(2);
            byte[] sha1 = HashUtil.sha1AsBytes(uuid);
            long next = ((sha1[0] << 24) | (sha1[1] << 16) | (sha1[2] << 8) | sha1[3]) & MAX_NEXT;
            long serverId = Long.parseLong(matcher.group(3), 16);
            return generateId(epoch, next, serverId);
        }
        throw new IllegalArgumentException("Invalid id: " + stringId);
    }













    public static SnowflakeId expId(long id) {
        SnowflakeId ret = new SnowflakeId();
        ret.setWorkerId(id & SHARD_ID_MASK);
        ret.setSequence((id >>> 5) & SEQUENCE_MASK);
        ret.setTimeStamp((id >>> 5+16) & TIMESTAMP_MASK);
        return ret;
    }


    public static Date transTime(long time) {
        return new Date(OFFSET*1000+time*1000);
    }


    public static long nextId() {
        return nextId(System.currentTimeMillis() / 1000);
    }

    private static synchronized long nextId(long epochSecond) {
        if (epochSecond < lastEpoch) {

            logger.warn("clock is back: " + epochSecond + " from previous:" + lastEpoch);
            epochSecond = lastEpoch;
        }
        if (lastEpoch != epochSecond) {
            lastEpoch = epochSecond;
            reset();
        }
        offset++;
        long next = offset & MAX_NEXT;
        if (next == 0) {
            logger.warn("maximum id reached in 1 second in epoch: " + epochSecond);
            return nextId(epochSecond + 1);
        }
        return generateId(epochSecond, next, SHARD_ID);
    }

    private static void reset() {
        offset = 0;
    }

    private static long generateId(long epochSecond, long next, long shardId) {
        return ((epochSecond - OFFSET) << 21) | (next << 5) | shardId;
    }

    private static long getServerIdAsLong() {
        try {
            String hostname = InetAddress.getLocalHost().getHostName();
            Matcher matcher = PATTERN_HOSTNAME.matcher(hostname);
            if (matcher.matches()) {
                long n = Long.parseLong(matcher.group(1));
                if (n >= 0 && n < 32) {
                    logger.info("detect server id from host name {}: {}.", hostname, n);
                    return n;
                }
            }else{
                try {
                    String hostAddress = Inet4Address.getLocalHost().getHostAddress();
                    int[] ints = StringUtils.toCodePoints(hostAddress);
                    int sums = 0;
                    for(int b : ints){
                        sums += b;
                    }
                    return (long)(sums % 32);
                } catch (UnknownHostException e) {

                    return RandomUtils.nextLong(0,31);
                }
            }
        } catch (UnknownHostException e) {
            logger.warn("unable to get host name. set server id = 0.");
        }
        return 0;
    }

}