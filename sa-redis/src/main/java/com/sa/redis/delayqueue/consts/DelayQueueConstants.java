package com.sa.redis.delayqueue.consts;


public interface DelayQueueConstants {

    String META_TOPIC_WAIT = "delay:meta:topic:wait";

    String META_TOPIC_ACTIVE = "delay:meta:topic:active";

    String META_TOPIC = "delay:meta:topic";


    String DELAY_WAIT_KEY = "delay:wait:";

    String DELAY_ACTIVE_KEY = "delay:active:";

    String DELAY_FAIL_KEY = "delay:fail:";


    String DELAY_QUEUE_KEY = "delay:queue:";


    String DELAY_QUEUE_FAIL_KEY = "delay:queue:fail:";


    String DELAY_QUEUE_EXECUTOR_KEY = "delay_queue_executor";
}
