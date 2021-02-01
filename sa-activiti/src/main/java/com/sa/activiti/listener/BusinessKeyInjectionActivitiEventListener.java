package com.sa.activiti.listener;

import org.activiti.engine.delegate.event.ActivitiEntityEvent;
import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;


@Component("businessKeyInjectionActivitiEventListener")
@ConditionalOnExpression("'${activiti.enable}'=='true'")
public class BusinessKeyInjectionActivitiEventListener implements ActivitiEventListener {
    private Logger log = LoggerFactory.getLogger(getClass());
    @Override
    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
            case PROCESS_STARTED:
                if (event instanceof ActivitiEntityEvent) {
                    ActivitiEntityEvent activityEntityEvent = (ActivitiEntityEvent) event;
                    ExecutionEntity exEntity = (ExecutionEntity) activityEntityEvent.getEntity();
                    ExecutionEntity processInstance = exEntity.getProcessInstance();
                    String key = processInstance.getBusinessKey();
                    log.info("获取当前任务的流程实例的businessKey:{}",key);
                    if(StringUtils.isEmpty(key)){
                        ExecutionEntity superExecEntity = exEntity.getSuperExecution();
                        key = superExecEntity.getBusinessKey();
                        if(StringUtils.isEmpty(key)){
                            key = superExecEntity.getProcessInstance().getBusinessKey();
                        }
                        if(StringUtils.isBlank(key)){
                            break;
                        }
                        log.info("获取上一个流程实例的businessKey:{}",key);
                        processInstance.setBusinessKey(key);
                        processInstance.updateProcessBusinessKey(key);
                    }
                    break;
                }
            default:
                break;
        }
    }

    @Override
    public boolean isFailOnException() {

        return false;
    }
}
