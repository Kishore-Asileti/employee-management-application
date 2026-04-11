package com.company.empmanagement.service;

import com.company.empmanagement.dto.EmployeeUpdateRequest;
import com.company.empmanagement.dto.LoginRequest;
import com.company.empmanagement.dto.LoginResponse;
import com.company.empmanagement.dto.RegisterRequest;
import com.company.empmanagement.model.AccountStatus;
import com.company.empmanagement.model.Employee;
import com.company.empmanagement.model.Role;
import com.company.empmanagement.repository.EmployeeRepository;
import com.company.empmanagement.security.JwtUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.RuntimeService;
import org.camunda.bpm.engine.TaskService;
import org.camunda.bpm.engine.runtime.ProcessInstance;
import org.camunda.bpm.engine.task.Task;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtils jwtUtils;
    private final RuntimeService runtimeService;
    private final TaskService taskService;

    // ── PROCESS KEY from the BPMN id ─────────────────────────────────────────
    private static final String PROCESS_KEY = "EmployeeManager";

    // ─────────────────────────────────────────────────────────────────────────
    // REGISTRATION: starts the Camunda process
    // ─────────────────────────────────────────────────────────────────────────
    public Map<String, Object> startRegistrationProcess(RegisterRequest req) {
        if (employeeRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email already registered: " + req.getEmail());
        }

        Map<String, Object> variables = new HashMap<>();
        variables.put("email",       req.getEmail());
        variables.put("firstName",   req.getFirstName());
        variables.put("lastName",    req.getLastName());
        variables.put("rawPassword", req.getPassword());
        variables.put("phone",       req.getPhone());
        variables.put("department",  req.getDepartment());
        variables.put("designation", req.getDesignation());
        variables.put("role",        req.getRole() != null ? req.getRole().name() : "EMPLOYEE");
        variables.put("isNewEmployee", true);

        ProcessInstance pi = runtimeService.startProcessInstanceByKey(PROCESS_KEY, variables);
        log.info("Started process instance: {} for email: {}", pi.getId(), req.getEmail());

        // The BPMN starts with user tasks Task_loginpage → Task_Register which model the
        // portal visit and form fill. Both are already handled by this REST API call, so
        // we auto-complete them to advance the process to Task_ValidateRegistration
        // (which saves the employee) and then to Task_HRApproval (where HR waits).
        advanceThroughUserTasks(pi.getId());

        return Map.of(
            "processInstanceId", pi.getId(),
            "message", "Registration started. Awaiting HR approval."
        );
    }

    private void advanceThroughUserTasks(String processInstanceId) {
        // Complete Task_loginpage
        Task loginTask = taskService.createTaskQuery()
            .processInstanceId(processInstanceId)
            .taskDefinitionKey("Task_loginpage")
            .singleResult();
        if (loginTask != null) {
            taskService.complete(loginTask.getId());
            log.info("Auto-completed Task_loginpage for process {}", processInstanceId);
        }

        // Complete Task_Register (process now waits here after loginpage completes)
        Task registerTask = taskService.createTaskQuery()
            .processInstanceId(processInstanceId)
            .taskDefinitionKey("Task_Register")
            .singleResult();
        if (registerTask != null) {
            taskService.complete(registerTask.getId());
            log.info("Auto-completed Task_Register for process {}", processInstanceId);
        }
        // After Task_Register completes: Task_ValidateRegistration (service task) runs
        // automatically, then Task_SendWelcomeEmail, then the process waits at Task_HRApproval.
    }

    // ─────────────────────────────────────────────────────────────────────────
    // LOGIN
    // ─────────────────────────────────────────────────────────────────────────
    public LoginResponse login(LoginRequest req) {
        Employee emp = employeeRepository.findByEmail(req.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!passwordEncoder.matches(req.getPassword(), emp.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (emp.getStatus() != AccountStatus.ACTIVE) {
            throw new IllegalStateException("Account is " + emp.getStatus().name().toLowerCase() +
                ". Please contact HR.");
        }

        String token = jwtUtils.generateToken(emp.getEmail(), emp.getRole().name(), emp.getId());

        return LoginResponse.builder()
            .token(token)
            .employeeId(emp.getId())
            .email(emp.getEmail())
            .fullName(emp.getFirstName() + " " + emp.getLastName())
            .role(emp.getRole())
            .processInstanceId(emp.getCamundaProcessInstanceId())
            .build();
    }

    // ─────────────────────────────────────────────────────────────────────────
    // HR APPROVAL via Camunda task completion
    // ─────────────────────────────────────────────────────────────────────────
    public void approveEmployee(String taskId, boolean approved, String hrUserId) {
        Task task = taskService.createTaskQuery().taskId(taskId).singleResult();
        if (task == null) throw new IllegalArgumentException("Task not found: " + taskId);

        // Read employeeId BEFORE completing the task — after completion the process
        // advances to End_Rejected (when rejected) which ends the instance, making
        // runtimeService.getVariable() throw because the instance no longer exists.
        String employeeId = (String) runtimeService.getVariable(
            task.getProcessInstanceId(), "employeeId");

        Map<String, Object> vars = new HashMap<>();
        vars.put("hrApproved", approved);
        vars.put("hrReviewedBy", hrUserId);

        taskService.claim(taskId, hrUserId);
        taskService.complete(taskId, vars);

        if (!approved) {
            // Mark employee as rejected in Couchbase
            if (employeeId != null) {
                employeeRepository.findById(employeeId).ifPresent(emp -> {
                    emp.setStatus(AccountStatus.REJECTED);
                    emp.setUpdatedAt(LocalDateTime.now());
                    employeeRepository.save(emp);
                });
            }
        }
        log.info("HR task {} completed. Approved={} by {}", taskId, approved, hrUserId);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // CRUD
    // ─────────────────────────────────────────────────────────────────────────
    public List<Employee> getAllEmployees() {
        return (List<Employee>) employeeRepository.findAll();
    }

    public Employee getEmployeeById(String id) {
        return employeeRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Employee not found: " + id));
    }

    public Employee updateEmployee(String id, EmployeeUpdateRequest req) {
        Employee emp = getEmployeeById(id);

        if (req.getFirstName()   != null) emp.setFirstName(req.getFirstName());
        if (req.getLastName()    != null) emp.setLastName(req.getLastName());
        if (req.getPhone()       != null) emp.setPhone(req.getPhone());
        if (req.getDepartment()  != null) emp.setDepartment(req.getDepartment());
        if (req.getDesignation() != null) emp.setDesignation(req.getDesignation());
        if (req.getRole()        != null) emp.setRole(req.getRole());
        if (req.getStatus()      != null) emp.setStatus(req.getStatus());
        if (req.getManagerId()   != null) emp.setManagerId(req.getManagerId());

        emp.setUpdatedAt(LocalDateTime.now());
        return employeeRepository.save(emp);
    }

    public void deleteEmployee(String id) {
        Employee emp = getEmployeeById(id);
        emp.setStatus(AccountStatus.INACTIVE);
        emp.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(emp);   // soft delete
    }

    public List<Employee> getEmployeesByRole(Role role) {
        return employeeRepository.findByRole(role);
    }

    public List<Employee> getEmployeesByDepartment(String dept) {
        return employeeRepository.findByDepartment(dept);
    }

    public List<Employee> getEmployeesByManager(String managerId) {
        return employeeRepository.findByManagerId(managerId);
    }

    public List<Employee> getPendingEmployees() {
        return employeeRepository.findByStatus(AccountStatus.PENDING);
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Manager Report
    // ─────────────────────────────────────────────────────────────────────────
    public Map<String, Object> generateReport(String managerId) {
        List<Employee> team = employeeRepository.findByManagerId(managerId);
        long active   = team.stream().filter(e -> e.getStatus() == AccountStatus.ACTIVE).count();
        long inactive = team.stream().filter(e -> e.getStatus() == AccountStatus.INACTIVE).count();
        long pending  = team.stream().filter(e -> e.getStatus() == AccountStatus.PENDING).count();

        Map<String, Object> report = new LinkedHashMap<>();
        report.put("managerId",    managerId);
        report.put("totalTeam",    team.size());
        report.put("active",       active);
        report.put("inactive",     inactive);
        report.put("pending",      pending);
        report.put("generatedAt",  LocalDateTime.now().toString());
        report.put("employees",    team);
        return report;
    }
}
