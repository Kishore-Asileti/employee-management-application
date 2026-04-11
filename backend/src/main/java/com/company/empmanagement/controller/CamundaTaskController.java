package com.company.empmanagement.controller;

import com.company.empmanagement.dto.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.camunda.bpm.engine.HistoryService;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.history.HistoricProcessInstance;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/camunda")
@RequiredArgsConstructor
public class CamundaTaskController {

    private final RuntimeService runtimeService;
    private final TaskService taskService;
    private final HistoryService historyService;

    /** GET /api/camunda/instances - all running process instances */
    @GetMapping("/instances")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse> getRunningInstances() {
        List<ProcessInstance> instances = runtimeService
                .createProcessInstanceQuery()
                .processDefinitionKey("EmployeeManager")
                .list();

        List<Map<String, Object>> result = instances.stream().map(pi -> Map.<String, Object>of(
                "instanceId",   pi.getId(),
                "processKey",   pi.getProcessDefinitionId(),
                "businessKey",  pi.getBusinessKey() != null ? pi.getBusinessKey() : "",
                "suspended",    pi.isSuspended()
        )).toList();

        return ResponseEntity.ok(ApiResponse.ok("Running instances.", result));
    }

    /** GET /api/camunda/instances/history - completed & running history */
    @GetMapping("/instances/history")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse> getHistory() {
        List<HistoricProcessInstance> history = historyService
                .createHistoricProcessInstanceQuery()
                .processDefinitionKey("EmployeeManager")
                .orderByProcessInstanceStartTime().desc()
                .list();

        List<Map<String, Object>> result = history.stream().map(h -> Map.<String, Object>of(
                "instanceId",  h.getId(),
                "startTime",   h.getStartTime() != null ? h.getStartTime().toString() : "",
                "endTime",     h.getEndTime() != null ? h.getEndTime().toString() : "In Progress",
                "state",       h.getState(),
                "durationMs",  h.getDurationInMillis() != null ? h.getDurationInMillis() : 0
        )).toList();

        return ResponseEntity.ok(ApiResponse.ok("Process history.", result));
    }

    /** GET /api/camunda/tasks/all - all open tasks */
    @GetMapping("/tasks/all")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse> getAllOpenTasks() {
        List<Task> tasks = taskService.createTaskQuery()
                .processDefinitionKey("EmployeeManager")
                .list();

        List<Map<String, Object>> result = tasks.stream().map(t -> Map.<String, Object>of(
                "taskId",       t.getId(),
                "taskName",     t.getName(),
                "assignee",     t.getAssignee() != null ? t.getAssignee() : "unassigned",
                "candidateGroup", "hr",
                "processId",    t.getProcessInstanceId(),
                "created",      t.getCreateTime() != null ? t.getCreateTime().toString() : ""
        )).toList();

        return ResponseEntity.ok(ApiResponse.ok("All open tasks.", result));
    }

    /** GET /api/camunda/tasks/my - tasks assigned to or claimable by current user */
    @GetMapping("/tasks/my")
    public ResponseEntity<ApiResponse> getMyTasks(@RequestParam String userId) {
        List<Task> tasks = taskService.createTaskQuery()
                .taskAssignee(userId)
                .list();

        List<Map<String, Object>> result = tasks.stream().map(t -> Map.<String, Object>of(
                "taskId",    t.getId(),
                "taskName",  t.getName(),
                "processId", t.getProcessInstanceId(),
                "created",   t.getCreateTime() != null ? t.getCreateTime().toString() : ""
        )).toList();

        return ResponseEntity.ok(ApiResponse.ok("My tasks.", result));
    }

    /** POST /api/camunda/tasks/{taskId}/complete - complete any task with variables */
    @PostMapping("/tasks/{taskId}/complete")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse> completeTask(
            @PathVariable String taskId,
            @RequestBody(required = false) Map<String, Object> variables) {
        try {
            taskService.complete(taskId, variables != null ? variables : Map.of());
            return ResponseEntity.ok(ApiResponse.ok("Task completed."));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /** GET /api/camunda/process/{instanceId}/variables */
    @GetMapping("/process/{instanceId}/variables")
    @PreAuthorize("hasRole('HR')")
    public ResponseEntity<ApiResponse> getProcessVariables(@PathVariable String instanceId) {
        try {
            var vars = runtimeService.getVariables(instanceId);
            // Remove sensitive fields
            vars.remove("rawPassword");
            return ResponseEntity.ok(ApiResponse.ok("Process variables.", vars));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }
}
