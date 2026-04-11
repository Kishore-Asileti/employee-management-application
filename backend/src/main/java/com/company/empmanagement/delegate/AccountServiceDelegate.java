package com.company.empmanagement.delegate;

import com.company.empmanagement.model.AccountStatus;
import com.company.empmanagement.model.Employee;
import com.company.empmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Camunda JavaDelegate referenced as:
 *   camunda:class="com.company.AccountService"
 *
 * Activates employee account after HR approval.
 */
@Slf4j
@Component("com.company.AccountService")
@RequiredArgsConstructor
public class AccountServiceDelegate implements JavaDelegate {

    private final EmployeeRepository employeeRepository;
    private final JavaMailSender mailSender;

    @Override
    public void execute(DelegateExecution execution) {
        String employeeId = (String) execution.getVariable("employeeId");

        log.info("Activating account for employeeId: {}", employeeId);

        Employee emp = employeeRepository.findById(employeeId).orElseThrow(
            () -> new RuntimeException("Employee not found: " + employeeId)
        );

        emp.setStatus(AccountStatus.ACTIVE);
        emp.setUpdatedAt(LocalDateTime.now());
        employeeRepository.save(emp);

        log.info("Account activated for: {}", emp.getEmail());

        // Send activation notification
        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(emp.getEmail());
            msg.setSubject("Your account has been approved!");
            msg.setText(
                "Dear " + emp.getFirstName() + ",\n\n" +
                "Your account has been approved by HR. You can now log in.\n\n" +
                "Login at: http://localhost:4200/login\n\n" +
                "Best regards,\nEmployee Management Team"
            );
            mailSender.send(msg);
        } catch (Exception e) {
            log.warn("Could not send activation email: {}", e.getMessage());
        }

        execution.setVariable("accountActivated", true);
    }
}
