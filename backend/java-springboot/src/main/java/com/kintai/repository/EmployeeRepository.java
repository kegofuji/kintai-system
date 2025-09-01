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
    
    List<Employee> findByEmploymentStatus(Employee.EmploymentStatus employmentStatus);
    
    @Query("SELECT e FROM Employee e WHERE " +
           "(e.employeeName LIKE %:keyword% OR e.employeeCode LIKE %:keyword%) " +
           "AND (:status IS NULL OR e.employmentStatus = :status) " +
           "ORDER BY e.employeeCode")
    List<Employee> findByKeywordAndStatus(@Param("keyword") String keyword, 
                                        @Param("status") Employee.EmploymentStatus status);
    
    boolean existsByEmployeeCode(String employeeCode);
    
    boolean existsByEmail(String email);
    
    @Query("SELECT e FROM Employee e WHERE e.employmentStatus = 'active' ORDER BY e.employeeCode")
    List<Employee> findActiveEmployees();
    
    @Query("SELECT COUNT(e) FROM Employee e WHERE e.employmentStatus = 'active'")
    long countActiveEmployees();
}
