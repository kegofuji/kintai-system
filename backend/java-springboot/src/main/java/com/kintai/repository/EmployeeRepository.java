package com.kintai.repository;

import com.kintai.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    Optional<Employee> findByEmployeeCode(String employeeCode);
    
    Optional<Employee> findByEmail(String email);
    
    boolean existsByEmployeeCode(String employeeCode);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT e FROM Employee e WHERE e.employmentStatus = :status")
    List<Employee> findByEmploymentStatus(@Param("status") Employee.EmploymentStatus status);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "(e.employeeName LIKE %:keyword% OR e.employeeCode LIKE %:keyword%) " +
           "AND (:status IS NULL OR e.employmentStatus = :status)")
    List<Employee> searchEmployees(@Param("keyword") String keyword, 
                                  @Param("status") Employee.EmploymentStatus status);
    
    @Query("SELECT e FROM Employee e WHERE e.employeeRole = :role")
    List<Employee> findByEmployeeRole(@Param("role") Employee.EmployeeRole role);
}