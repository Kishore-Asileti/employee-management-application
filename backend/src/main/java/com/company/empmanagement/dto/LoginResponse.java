package com.company.empmanagement.dto;

import com.company.empmanagement.model.Role;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class LoginResponse {
    private String token;
    private String employeeId;
    private String email;
    private String fullName;
    private Role role;
    private String processInstanceId;
}
