package com.company.empmanagement.delegate;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.camunda.bpm.engine.delegate.DelegateExecution;
import org.camunda.bpm.engine.delegate.JavaDelegate;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

/**
 * Camunda JavaDelegate referenced in the BPMN as:
 *   camunda:class="com.company.EmailService"
 *
 * Sends a welcome email after successful registration validation.
 */
@Slf4j
@Component("com.company.EmailService")
@RequiredArgsConstructor
public class EmailServiceDelegate implements JavaDelegate {

    private final JavaMailSender mailSender;

    @Override
    public void execute(DelegateExecution execution) {
        String email     = (String) execution.getVariable("email");
        String firstName = (String) execution.getVariable("firstName");
        String lastName  = (String) execution.getVariable("lastName");

        log.info("Sending welcome email to {} {} <{}>", firstName, lastName, email);

        try {
            SimpleMailMessage msg = new SimpleMailMessage();
            msg.setTo(email);
            msg.setSubject("Welcome to Employee Management System");
            msg.setText(
                "Dear " + firstName + " " + lastName + ",\n\n" +
                "Your registration has been received and is under HR review.\n" +
                "You will receive another notification once your account is approved.\n\n" +
                "Best regards,\nEmployee Management Team"
            );
            mailSender.send(msg);
            log.info("Welcome email sent successfully to {}", email);
        } catch (Exception e) {
            // Non-fatal: log and continue process
            log.warn("Failed to send welcome email to {}: {}", email, e.getMessage());
        }

        execution.setVariable("welcomeEmailSent", true);
    }
}
