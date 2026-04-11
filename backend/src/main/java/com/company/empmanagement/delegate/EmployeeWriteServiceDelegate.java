package com.company.empmanagement.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Stub delegate for BPMN service tasks:
 *   Task_HR_SaveChanges, Task_Mgr_SaveChanges
 *
 * Referenced as: camunda:class="com.company.EmployeeWriteService"
 *
 * Actual employee writes are handled by the REST API controllers.
 * This delegate keeps the Camunda process executable without errors.
 */
@Slf4j
@Component("com.company.EmployeeWriteService")
public class EmployeeWriteServiceDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("EmployeeWriteService delegate executed for process instance: {}",
                execution.getProcessInstanceId());
        // Writes are handled via REST API — no Camunda-side action needed.
    }
}
