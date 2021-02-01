





























package com.sa.sid.boot;

import com.sa.sid.service.SnowFlakeIdService;
import com.sa.sid.service.SnowflakeIdConverter;
import com.sa.sid.service.impl.SnowFlakeIdServiceImpl;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.net.Inet4Address;
import java.net.UnknownHostException;


@Configuration
public class IdWorkerConfiguration {
    @Value("${id.worker:noWorker}")
    private String worker;
    @Value("${id.dataCenter:noDataCenter}")
    private String dataCenter;
    protected static final Logger log = LoggerFactory.getLogger(IdWorkerConfiguration.class);
    @Bean
    @Primary
    public SnowFlakeIdService idWorker(SnowflakeIdConverter snowflakeIdConverter){
        Long datacenterId = getDataCenterFromConfig();
        Long workerId = getWorkFromConfig();
        log.info("detect server datacenterId:" + datacenterId + ", workerId:" + workerId);
        return new SnowFlakeIdServiceImpl(datacenterId, workerId, snowflakeIdConverter);
    }

    private Long getWorkFromConfig() {
        if ("noWorker".equals(worker)) {
            return getWorker();
        }

        return Long.parseLong(worker);
    }

    private Long getDataCenterFromConfig() {
        if ("noDataCenter".equals(dataCenter)) {
            return getDataCenterId();
        }

        return Long.parseLong(dataCenter);
    }

    private Long getWorker(){
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

    private Long getDataCenterId(){
        int[] ints = StringUtils.toCodePoints(SystemUtils.getHostName());
        if(ints == null){
            System.out.println("未取到HostName!datacenterId为1");
            return 1L;
        }
        int sums = 0;
        for (int i: ints) {
            sums += i;
        }
        return (long)(sums % 32);
    }

}
