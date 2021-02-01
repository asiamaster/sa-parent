package com.sa.activiti.listener;

import org.activiti.engine.RuntimeService;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.delegate.ExecutionListener;
import org.activiti.engine.impl.persistence.entity.ExecutionEntity;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;


@Component
public class BusinessKeyExecutionListener implements ExecutionListener {
    @Autowired
    RuntimeService runtimeService;
    @Override
    public void notify(DelegateExecution execution){
        if(!StringUtils.isBlank(execution.getProcessBusinessKey())){
            return;
        }

        Object businessKeyObj = execution.getVariable("businessKey");
        if(businessKeyObj != null){
            runtimeService.updateBusinessKey(execution.getProcessInstanceId(), businessKeyObj.toString());
        }
        if(!(execution instanceof ExecutionEntity)){
            return;
        }
        ExecutionEntity processInstance = ((ExecutionEntity) execution).getSuperExecution().getProcessInstance();
        if(processInstance == null || processInstance.getBusinessKey() == null){
            return;
        }
        runtimeService.updateBusinessKey(execution.getProcessInstanceId(), processInstance.getBusinessKey());
    }
}
