package com.company.empmanagement.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RepositoryService;
import org.camunda.bpm.engine.repository.ProcessDefinition;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class ProcessDeploymentConfig implements CommandLineRunner {

    private static final String PROCESS_KEY = "EmployeeManager";
    private static final String BPMN_CLASSPATH = "processes/employee_management_application.bpmn";

    private final RepositoryService repositoryService;

    @Override
    public void run(String... args) {
        ProcessDefinition existing = repositoryService
            .createProcessDefinitionQuery()
            .processDefinitionKey(PROCESS_KEY)
            .latestVersion()
            .singleResult();

        if (existing == null) {
            repositoryService.createDeployment()
                .name("employee-management-bpmn")
                .addClasspathResource(BPMN_CLASSPATH)
                .deploy();
            log.info("Deployed BPMN process from {}", BPMN_CLASSPATH);
        } else {
            log.info("BPMN process already deployed: key={}, version={}", PROCESS_KEY, existing.getVersion());
        }
    }
}
