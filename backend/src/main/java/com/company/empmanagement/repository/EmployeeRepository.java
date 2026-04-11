package com.company.empmanagement.repository;

import com.company.empmanagement.model.AccountStatus;
import com.company.empmanagement.model.Employee;
import com.company.empmanagement.model.Role;
import org.springframework.data.couchbase.repository.CouchbaseRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends CouchbaseRepository<Employee, String> {

    Optional<Employee> findByEmail(String email);

    boolean existsByEmail(String email);

    List<Employee> findByRole(Role role);

    List<Employee> findByStatus(AccountStatus status);

    List<Employee> findByDepartment(String department);

    List<Employee> findByManagerId(String managerId);

    List<Employee> findByRoleAndStatus(Role role, AccountStatus status);
}
