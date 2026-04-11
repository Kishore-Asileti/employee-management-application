package com.company.empmanagement.config;

import com.company.empmanagement.model.AccountStatus;
import com.company.empmanagement.model.Employee;
import com.company.empmanagement.model.Role;
import com.company.empmanagement.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Seeds default users on first startup so the system is usable out of the box.
 *
 * Default accounts:
 *   HR Admin  — hr@company.com      / Admin@123
 *   Manager   — manager@company.com / Admin@123
 *   Employee  — emp@company.com     / Admin@123
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        seedUser("hr@company.com",      "Admin", "HR",      Role.HR,       "HR",          "HR Manager");
        seedUser("manager@company.com", "John",  "Manager", Role.MANAGER,  "Engineering", "Engineering Manager");
        seedUser("emp@company.com",     "Jane",  "Doe",     Role.EMPLOYEE, "Engineering", "Software Engineer");
    }

    private void seedUser(String email, String first, String last, Role role,
                          String dept, String desig) {
        if (!employeeRepository.existsByEmail(email)) {
            Employee emp = Employee.builder()
                    .id(UUID.randomUUID().toString())
                    .firstName(first)
                    .lastName(last)
                    .email(email)
                    .password(passwordEncoder.encode("Admin@123"))
                    .department(dept)
                    .designation(desig)
                    .role(role)
                    .status(AccountStatus.ACTIVE)
                    .phone("+1-555-0100")
                    .createdAt(LocalDateTime.now())
                    .updatedAt(LocalDateTime.now())
                    .build();
            employeeRepository.save(emp);
            log.info("Seeded default user: {} ({})", email, role);
        }
    }
}
