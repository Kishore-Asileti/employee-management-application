package com.company.empmanagement.delegate;

import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.stereotype.Component;

/**
 * Stub delegate for BPMN service task:
 *   Task_Mgr_GenerateReport
 *
 * Referenced as: camunda:class="com.company.ReportService"
 *
 * Actual report generation is handled by the REST API (/api/manager/report).
 * This delegate keeps the Camunda process executable without errors.
 */
@Slf4j
@Component("com.company.ReportService")
public class ReportServiceDelegate implements JavaDelegate {

    @Override
    public void execute(DelegateExecution execution) {
        log.info("ReportService delegate executed for process instance: {}",
                execution.getProcessInstanceId());
        // Report generation is handled via REST API — no Camunda-side action needed.
    }
}
