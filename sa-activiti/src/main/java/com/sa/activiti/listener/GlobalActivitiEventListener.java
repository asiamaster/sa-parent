package com.sa.activiti.listener;

import org.activiti.engine.delegate.event.ActivitiEvent;
import org.activiti.engine.delegate.event.ActivitiEventListener;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.stereotype.Component;


@Component
@ConditionalOnExpression("'${activiti.enable}'=='true'")
public class GlobalActivitiEventListener implements ActivitiEventListener {
    @Override
    public void onEvent(ActivitiEvent event) {
        switch (event.getType()) {
            case ACTIVITY_COMPENSATE:

                break;
            case ACTIVITY_COMPLETED:

                break;
            case ACTIVITY_ERROR_RECEIVED:

                break;
            case ACTIVITY_MESSAGE_RECEIVED:

                break;
            case ACTIVITY_SIGNALED:

                break;
            case ACTIVITY_STARTED:

                break;
            case CUSTOM:
                break;
            case ENGINE_CLOSED:

                break;
            case ENGINE_CREATED:

                break;
            case ENTITY_ACTIVATED:

                break;
            case ENTITY_CREATED:

                break;
            case ENTITY_DELETED:

                break;
            case ENTITY_INITIALIZED:

                break;
            case ENTITY_SUSPENDED:

                break;
            case ENTITY_UPDATED:

                break;
            case JOB_EXECUTION_FAILURE:

                break;
            case JOB_EXECUTION_SUCCESS:

                break;
            case JOB_RETRIES_DECREMENTED:

                break;
            case MEMBERSHIPS_DELETED:

                break;
            case MEMBERSHIP_CREATED:

                break;
            case MEMBERSHIP_DELETED:

                break;
            case TASK_ASSIGNED:

                break;
            case TASK_COMPLETED:

                break;
            case TIMER_FIRED:

                break;
            case UNCAUGHT_BPMN_ERROR:
                break;
            case VARIABLE_CREATED:
                break;
            case VARIABLE_DELETED:
                break;
            case VARIABLE_UPDATED:
                break;
            default:
                break;
        }
    }

    @Override
    public boolean isFailOnException() {
        System.out.println("isFailOnException");

        return false;
    }
}
