package com.company.empmanagement.controller;

import com.company.empmanagement.dto.ApiResponse;
import com.company.empmanagement.model.Employee;
import com.company.empmanagement.security.JwtUtils;
import com.company.empmanagement.service.EmployeeService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/employee")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;
    private final JwtUtils jwtUtils;

    /** GET /api/employee/me - get own profile */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse> getMyProfile(HttpServletRequest request) {
        String id = extractEmployeeId(request);
        try {
            Employee emp = employeeService.getEmployeeById(id);
            return ResponseEntity.ok(ApiResponse.ok("Profile.", emp));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /api/employee/list - all active employees (for employee view) */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse> viewEmployeeList() {
        List<Employee> list = employeeService.getAllEmployees();
        return ResponseEntity.ok(ApiResponse.ok("Employee list.", list));
    }

    private String extractEmployeeId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtils.getEmployeeIdFromToken(token);
    }
}
