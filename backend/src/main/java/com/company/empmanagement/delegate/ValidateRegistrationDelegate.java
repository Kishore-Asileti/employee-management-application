package com.company.empmanagement.delegate;

import com.company.empmanagement.model.AccountStatus;
import com.company.empmanagement.model.Employee;
import com.company.empmanagement.model.Role;
import com.company.empmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Service task: "Validate & Save Employee to DB"
 * Sets validationResult = VALID | INVALID and validationErrors
 */
@Slf4j
@Component("validateRegistrationDelegate")
@RequiredArgsConstructor
public class ValidateRegistrationDelegate implements JavaDelegate {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void execute(DelegateExecution execution) {
        String email     = (String) execution.getVariable("email");
        String firstName = (String) execution.getVariable("firstName");
        String lastName  = (String) execution.getVariable("lastName");
        String rawPwd    = (String) execution.getVariable("rawPassword");
        String phone     = (String) execution.getVariable("phone");
        String dept      = (String) execution.getVariable("department");
        String desig     = (String) execution.getVariable("designation");
        String roleStr   = (String) execution.getVariable("role");

        // ── Validation ────────────────────────────────────────────────────────
        StringBuilder errors = new StringBuilder();

        if (email == null || !email.matches("^[\\w.+\\-]+@[\\w\\-]+\\.[a-z]{2,}$"))
            errors.append("Invalid email address. ");

        if (firstName == null || firstName.isBlank())
            errors.append("First name is required. ");

        if (lastName == null || lastName.isBlank())
            errors.append("Last name is required. ");

        if (rawPwd == null || rawPwd.length() < 6)
            errors.append("Password must be at least 6 characters. ");

        if (employeeRepository.existsByEmail(email))
            errors.append("Email already registered. ");

        if (errors.length() > 0) {
            log.warn("Registration validation failed for {}: {}", email, errors);
            execution.setVariable("validationResult", "INVALID");
            execution.setVariable("validationErrors", errors.toString().trim());
            return;
        }

        // ── Persist to Couchbase ──────────────────────────────────────────────
        Role role = Role.EMPLOYEE;
        try { role = Role.valueOf(roleStr); } catch (Exception ignored) {}

        Employee emp = Employee.builder()
            .id(UUID.randomUUID().toString())
            .firstName(firstName)
            .lastName(lastName)
            .email(email)
            .password(passwordEncoder.encode(rawPwd))
            .phone(phone)
            .department(dept)
            .designation(desig)
            .role(role)
            .status(AccountStatus.PENDING)
            .camundaProcessInstanceId(execution.getProcessInstanceId())
            .createdAt(LocalDateTime.now())
            .updatedAt(LocalDateTime.now())
            .build();

        employeeRepository.save(emp);
        log.info("Employee saved to Couchbase: {} (id={})", email, emp.getId());

        execution.setVariable("validationResult", "VALID");
        execution.setVariable("employeeId", emp.getId());
        execution.setVariable("validationErrors", "");
    }
}
