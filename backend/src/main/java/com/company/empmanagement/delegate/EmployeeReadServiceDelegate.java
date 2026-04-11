package com.company.empmanagement.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Stub delegate for BPMN service tasks:
 *   Task_HR_ViewAll, Task_ViewEmployeeList, Task_Mgr_ViewAll
 *
 * Referenced as: camunda:class="com.company.EmployeeReadService"
 *
 * Actual employee reads are handled by the REST API controllers.
 * This delegate keeps the Camunda process executable without errors.
 */
@Slf4j
@Component("com.company.EmployeeReadService")
public class EmployeeReadServiceDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("EmployeeReadService delegate executed for process instance: {}",
                execution.getProcessInstanceId());
        // Reads are handled via REST API — no Camunda-side action needed.
    }
}
