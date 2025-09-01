package com.kintai.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_code", columnList = "employee_code", unique = true),
    @Index(name = "idx_employee_email", columnList = "email", unique = true)
})
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;
    
    @Column(name = "employee_code", nullable = false, unique = true, length = 10)
    private String employeeCode;
    
    @Column(name = "employee_name", nullable = false, length = 50)
    private String employeeName;
    
    @Column(name = "email", nullable = false, unique = true, length = 100)
    private String email;
    
    @Column(name = "employee_password_hash", nullable = false, length = 255)
    private String employeePasswordHash;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role", nullable = false)
    private EmployeeRole employeeRole = EmployeeRole.employee;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false)
    private EmploymentStatus employmentStatus = EmploymentStatus.active;
    
    @Column(name = "hired_at", nullable = false)
    private LocalDate hiredAt;
    
    @Column(name = "retired_at")
    private LocalDate retiredAt;
    
    @Column(name = "paid_leave_remaining_days", nullable = false)
    private Integer paidLeaveRemainingDays = 10;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public Employee() {}
    
    public Employee(String employeeCode, String employeeName, String email, 
                   String employeePasswordHash, EmployeeRole employeeRole) {
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.email = email;
        this.employeePasswordHash = employeePasswordHash;
        this.employeeRole = employeeRole;
        this.hiredAt = LocalDate.now();
    }
    
    // Getters and Setters
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    
    public String getEmployeeName() { return employeeName; }
    public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
    
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    
    public String getEmployeePasswordHash() { return employeePasswordHash; }
    public void setEmployeePasswordHash(String employeePasswordHash) { 
        this.employeePasswordHash = employeePasswordHash; 
    }
    
    public EmployeeRole getEmployeeRole() { return employeeRole; }
    public void setEmployeeRole(EmployeeRole employeeRole) { this.employeeRole = employeeRole; }
    
    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(EmploymentStatus employmentStatus) { 
        this.employmentStatus = employmentStatus; 
    }
    
    public LocalDate getHiredAt() { return hiredAt; }
    public void setHiredAt(LocalDate hiredAt) { this.hiredAt = hiredAt; }
    
    public LocalDate getRetiredAt() { return retiredAt; }
    public void setRetiredAt(LocalDate retiredAt) { this.retiredAt = retiredAt; }
    
    public Integer getPaidLeaveRemainingDays() { return paidLeaveRemainingDays; }
    public void setPaidLeaveRemainingDays(Integer paidLeaveRemainingDays) { 
        this.paidLeaveRemainingDays = paidLeaveRemainingDays; 
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Enums
    public enum EmployeeRole {
        employee, admin
    }
    
    public enum EmploymentStatus {
        active, retired
    }
}
