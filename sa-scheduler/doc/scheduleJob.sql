CREATE TABLE `schedule_job` (
	`id` BIGINT(20) NOT NULL AUTO_INCREMENT,
	`created` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
	`modified` TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
	`job_name` VARCHAR(40) NULL DEFAULT NULL COMMENT '任务名',
	`job_group` VARCHAR(40) NULL DEFAULT NULL COMMENT '任务分组',
	`job_status` INT(11) NULL DEFAULT NULL COMMENT '是否启动任务##{provider:"jobStatusProvider", data:[{value:0, text:"无"},{value:1, text:"正常"},{value:2, text:"暂停"},{value:3, text:"完成"},{value:4, text:"错误"},{value:5, text:"阻塞"}]}',
	`job_data` VARCHAR(1000) NULL DEFAULT NULL COMMENT 'json',
	`cron_expression` VARCHAR(40) NULL DEFAULT NULL COMMENT 'cron表达式',
	`repeat_interval` INT(11) NULL DEFAULT NULL COMMENT '重复间隔(s)##简单调度，默认以秒为单位',
	`start_delay` INT(11) NULL DEFAULT NULL COMMENT '启动间隔(s)##启动调度器后，多少秒开始执行调度',
	`description` VARCHAR(200) NULL DEFAULT NULL COMMENT '描述',
	`bean_class` VARCHAR(100) NULL DEFAULT NULL COMMENT '任务调用类##任务执行时调用类的全名，用于反射',
	`spring_id` VARCHAR(40) NULL DEFAULT NULL COMMENT 'SpringBeanId##spring的beanId，直接从spring中获取',
	`url` VARCHAR(100) NULL DEFAULT NULL COMMENT 'url##支持远程调用restful url',
	`is_concurrent` INT(11) NULL DEFAULT NULL COMMENT '任务是否有状态##1：并发; 0:同步##{provider:"isConcurrentProvider", data:[{value:0, text:"同步"},{value:1, text:"并发"}]}',
	`method_name` VARCHAR(40) NULL DEFAULT NULL COMMENT '任务调用的方法名##bean_class和spring_id需要配置方法名',
	`retry_count` INT(11) NULL DEFAULT NULL COMMENT '重试次数',
	`retry_interval` BIGINT(20) NULL DEFAULT NULL COMMENT '重试间隔/毫秒',
	`recovery_callback` VARCHAR(40) NULL DEFAULT NULL COMMENT '兜底回调',
	PRIMARY KEY (`id`)
)
COMMENT='任务调度'
COLLATE='utf8_general_ci'
ENGINE=InnoDB
AUTO_INCREMENT=9
;
