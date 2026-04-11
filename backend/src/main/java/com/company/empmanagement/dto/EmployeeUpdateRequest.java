package com.company.empmanagement.dto;

import com.company.empmanagement.model.AccountStatus;
import com.company.empmanagement.model.Role;
import lombok.Data;

@Data
public class EmployeeUpdateRequest {
    private String firstName;
    private String lastName;
    private String phone;
    private String department;
    private String designation;
    private Role role;
    private AccountStatus status;
    private String managerId;
}
