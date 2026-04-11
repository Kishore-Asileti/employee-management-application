package com.company.empmanagement.model;

import lombok.*;
import org.springframework.data.annotation.Id;
import org.springframework.data.couchbase.core.mapping.Document;
import org.springframework.data.couchbase.core.mapping.Field;
import java.time.LocalDateTime;

@Document
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Employee {

    @Id
    private String id;

    @Field
    private String firstName;

    @Field
    private String lastName;

    @Field
    private String email;

    @Field
    private String password;   // BCrypt hashed

    @Field
    private String phone;

    @Field
    private String department;

    @Field
    private String designation;

    @Field
    private Role role;  // HR, MANAGER, EMPLOYEE

    @Field
    private AccountStatus status;  // PENDING, ACTIVE, REJECTED

    @Field
    private String managerId;

    @Field
    private String camundaProcessInstanceId;

    @Field
    private LocalDateTime createdAt;

    @Field
    private LocalDateTime updatedAt;
}
