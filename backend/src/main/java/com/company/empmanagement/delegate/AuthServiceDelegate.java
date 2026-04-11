package com.company.empmanagement.delegate;

import com.company.empmanagement.model.AccountStatus;
import com.company.empmanagement.model.Employee;
import com.company.empmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Camunda JavaDelegate referenced as:
 *   camunda:class="com.company.AuthService"
 *
 * Validates credentials and sets role/authResult process variables.
 */
@Slf4j
@Component("com.company.AuthService")
@RequiredArgsConstructor
public class AuthServiceDelegate implements JavaDelegate {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void execute(DelegateExecution execution) {
        String email    = (String) execution.getVariable("email");
        String password = (String) execution.getVariable("rawPassword");

        log.info("Authenticating user: {}", email);

        Employee emp = employeeRepository.findByEmail(email).orElse(null);

        if (emp == null || !passwordEncoder.matches(password, emp.getPassword())) {
            log.warn("Authentication failed for: {}", email);
            execution.setVariable("authResult", "FAILED");
            execution.setVariable("authError", "Invalid email or password");
            return;
        }

        if (emp.getStatus() != AccountStatus.ACTIVE) {
            log.warn("Account not active for: {}", email);
            execution.setVariable("authResult", "FAILED");
            execution.setVariable("authError", "Account is " + emp.getStatus().name().toLowerCase() + ". Please contact HR.");
            return;
        }

        log.info("Authentication successful for: {} with role: {}", email, emp.getRole());
        execution.setVariable("authResult", "SUCCESS");
        execution.setVariable("userRole",   emp.getRole().name());
        execution.setVariable("employeeId", emp.getId());
        execution.setVariable("fullName",   emp.getFirstName() + " " + emp.getLastName());
    }
}
