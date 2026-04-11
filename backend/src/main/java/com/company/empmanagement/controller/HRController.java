package com.company.empmanagement.controller;

import com.company.empmanagement.dto.ApiResponse;
import com.company.empmanagement.dto.EmployeeUpdateRequest;
import com.company.empmanagement.model.Employee;
import com.company.empmanagement.model.Role;
import com.company.empmanagement.security.JwtUtils;
import com.company.empmanagement.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.task.Task;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/hr")
@PreAuthorize("hasRole('HR')")
@RequiredArgsConstructor
public class HRController {

    private final EmployeeService employeeService;
    private final TaskService taskService;
    private final JwtUtils jwtUtils;

    /** GET /api/hr/employees - list all employees */
    @GetMapping("/employees")
    public ResponseEntity<ApiResponse> getAllEmployees() {
        List<Employee> list = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.ok("Employees fetched.", list));
    }

    /** GET /api/hr/employees/pending - PENDING approval */
    @GetMapping("/employees/pending")
    public ResponseEntity<ApiResponse> getPendingEmployees() {
        return ResponseEntity.ok(ApiResponse.ok("Pending employees.",
            employeeService.getPendingEmployees()));
    }

    /** GET /api/hr/employees/{id} */
    @GetMapping("/employees/{id}")
    public ResponseEntity<ApiResponse> getEmployee(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Employee found.", employeeService.getEmployeeById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** PUT /api/hr/employees/{id} */
    @PutMapping("/employees/{id}")
    public ResponseEntity<ApiResponse> updateEmployee(@PathVariable String id,
                                                       @RequestBody EmployeeUpdateRequest req) {
        try {
            Employee updated = employeeService.updateEmployee(id, req);
            return ResponseEntity.ok(ApiResponse.ok("Employee updated.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/hr/employees/{id} - soft delete */
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<ApiResponse> deleteEmployee(@PathVariable String id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(ApiResponse.ok("Employee deactivated."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /api/hr/tasks - Camunda HR approval tasks */
    @GetMapping("/tasks")
    public ResponseEntity<ApiResponse> getHrTasks() {
        List<Task> tasks = taskService.createTaskQuery()
            .taskCandidateGroup("hr")
            .list();

        List<Map<String, Object>> result = tasks.stream().map(t -> Map.<String, Object>of(
            "taskId",            t.getId(),
            "taskName",          t.getName(),
            "processInstanceId", t.getProcessInstanceId(),
            "created",           t.getCreateTime()
        )).toList();

        return ResponseEntity.ok(ApiResponse.ok("HR tasks.", result));
    }

    /** POST /api/hr/tasks/{taskId}/approve */
    @PostMapping("/tasks/{taskId}/approve")
    public ResponseEntity<ApiResponse> approveTask(@PathVariable String taskId,
                                                    @RequestParam(defaultValue = "true") boolean approved,
                                                    HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        String hrId  = jwtUtils.getEmployeeIdFromToken(token);
        try {
            employeeService.approveEmployee(taskId, approved, hrId);
            String msg = approved ? "Employee approved and account activated." : "Employee registration rejected.";
            return ResponseEntity.ok(ApiResponse.ok(msg));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /** GET /api/hr/employees/role/{role} */
    @GetMapping("/employees/role/{role}")
    public ResponseEntity<ApiResponse> getByRole(@PathVariable String role) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Employees by role.",
                employeeService.getEmployeesByRole(Role.valueOf(role.toUpperCase()))));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error("Invalid role: " + role));
        }
    }
}
