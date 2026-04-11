package com.company.empmanagement.controller;

import com.company.empmanagement.dto.ApiResponse;
import com.company.empmanagement.dto.EmployeeUpdateRequest;
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
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/manager")
@PreAuthorize("hasAnyRole('MANAGER','HR')")
@RequiredArgsConstructor
public class ManagerController {

    private final EmployeeService employeeService;
    private final JwtUtils jwtUtils;

    /** GET /api/manager/team - team members under this manager */
    @GetMapping("/team")
    public ResponseEntity<ApiResponse> getMyTeam(HttpServletRequest request) {
        String managerId = extractEmployeeId(request);
        List<Employee> team = employeeService.getEmployeesByManager(managerId);
        return ResponseEntity.ok(ApiResponse.ok("Team members.", team));
    }

    /** GET /api/manager/employees - view all employees (read-only) */
    @GetMapping("/employees")
    public ResponseEntity<ApiResponse> getAllEmployees() {
        return ResponseEntity.ok(ApiResponse.ok("All employees.", employeeService.getAllEmployees()));
    }

    /** GET /api/manager/employees/{id} */
    @GetMapping("/employees/{id}")
    public ResponseEntity<ApiResponse> getEmployee(@PathVariable String id) {
        try {
            return ResponseEntity.ok(ApiResponse.ok("Employee.", employeeService.getEmployeeById(id)));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** PUT /api/manager/employees/{id} - edit (limited fields) */
    @PutMapping("/employees/{id}")
    public ResponseEntity<ApiResponse> editEmployee(@PathVariable String id,
                                                     @RequestBody EmployeeUpdateRequest req) {
        // Managers cannot change role or status
        req.setRole(null);
        req.setStatus(null);
        try {
            Employee updated = employeeService.updateEmployee(id, req);
            return ResponseEntity.ok(ApiResponse.ok("Employee updated.", updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** DELETE /api/manager/employees/{id} - soft delete */
    @DeleteMapping("/employees/{id}")
    public ResponseEntity<ApiResponse> deleteEmployee(@PathVariable String id) {
        try {
            employeeService.deleteEmployee(id);
            return ResponseEntity.ok(ApiResponse.ok("Employee deactivated."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.notFound().build();
        }
    }

    /** GET /api/manager/report - generate team report */
    @GetMapping("/report")
    public ResponseEntity<ApiResponse> getReport(HttpServletRequest request) {
        String managerId = extractEmployeeId(request);
        Map<String, Object> report = employeeService.generateReport(managerId);
        return ResponseEntity.ok(ApiResponse.ok("Report generated.", report));
    }

    /** GET /api/manager/employees/department/{dept} */
    @GetMapping("/employees/department/{dept}")
    public ResponseEntity<ApiResponse> getByDepartment(@PathVariable String dept) {
        return ResponseEntity.ok(ApiResponse.ok("Employees by department.",
            employeeService.getEmployeesByDepartment(dept)));
    }

    private String extractEmployeeId(HttpServletRequest request) {
        String token = request.getHeader("Authorization").substring(7);
        return jwtUtils.getEmployeeIdFromToken(token);
    }
}
