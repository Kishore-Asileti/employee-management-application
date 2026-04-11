package com.company.empmanagement.controller;

import com.company.empmanagement.dto.ApiResponse;
import com.company.empmanagement.dto.LoginRequest;
import com.company.empmanagement.dto.LoginResponse;
import com.company.empmanagement.dto.RegisterRequest;
import com.company.empmanagement.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@Slf4j
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final EmployeeService employeeService;

    /**
     * POST /api/auth/register
     * Starts the Camunda EmployeeManager process for a new employee.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse> register(@Valid @RequestBody RegisterRequest req) {
        try {
            var result = employeeService.startRegistrationProcess(req);
            return ResponseEntity.ok(ApiResponse.ok("Registration submitted successfully.", result));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(ApiResponse.error(e.getMessage()));
        }
    }

    /**
     * POST /api/auth/login
     * Validates credentials and returns a JWT token.
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse> login(@Valid @RequestBody LoginRequest req) {
        try {
            LoginResponse resp = employeeService.login(req);
            return ResponseEntity.ok(ApiResponse.ok("Login successful.", resp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(401).body(ApiResponse.error(e.getMessage()));
        } catch (IllegalStateException e) {
            return ResponseEntity.status(403).body(ApiResponse.error(e.getMessage()));
        }
    }
}
