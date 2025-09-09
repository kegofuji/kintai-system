package com.kintai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "employees",
       indexes = {
           @Index(name = "idx_employee_code", columnList = "employee_code", unique = true),
           @Index(name = "idx_employee_email", columnList = "email", unique = true)
       })
@EntityListeners(AuditingEntityListener.class)
public class Employee {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id")
    private Long employeeId;
    
    @NotBlank
    @Column(name = "employee_code", length = 10, unique = true, nullable = false)
    private String employeeCode;
    
    @NotBlank
    @Column(name = "employee_name", length = 50, nullable = false)
    private String employeeName;
    
    @NotBlank
    @Email
    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;
    
    @NotBlank
    @Column(name = "employee_password_hash", nullable = false)
    private String employeePasswordHash;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role", nullable = false)
    private EmployeeRole employeeRole = EmployeeRole.EMPLOYEE;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false)
    private EmploymentStatus employmentStatus = EmploymentStatus.ACTIVE;
    
    @NotNull
    @Column(name = "hired_at", nullable = false)
    private LocalDate hiredAt;
    
    @Column(name = "retired_at")
    private LocalDate retiredAt;
    
    @NotNull
    @Column(name = "paid_leave_remaining_days", nullable = false)
    private Integer paidLeaveRemainingDays = 10;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Enums - 設計書通りの値を使用
    public enum EmployeeRole {
        EMPLOYEE("employee"), ADMIN("admin");
        
        private final String value;
        EmployeeRole(String value) { this.value = value; }
        public String getValue() { return value; }
    }
    
    public enum EmploymentStatus {
        ACTIVE("active"), RETIRED("retired");
        
        private final String value;
        EmploymentStatus(String value) { this.value = value; }
        public String getValue() { return value; }
    }
    
    // Constructors
    public Employee() {}
    
    public Employee(String employeeCode, String employeeName, String email, 
                   String employeePasswordHash, EmployeeRole employeeRole, LocalDate hiredAt) {
        this.employeeCode = employeeCode;
        this.employeeName = employeeName;
        this.email = email;
        this.employeePasswordHash = employeePasswordHash;
        this.employeeRole = employeeRole;
        this.hiredAt = hiredAt;
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
    public void setEmployeePasswordHash(String employeePasswordHash) { this.employeePasswordHash = employeePasswordHash; }
    
    public EmployeeRole getEmployeeRole() { return employeeRole; }
    public void setEmployeeRole(EmployeeRole employeeRole) { this.employeeRole = employeeRole; }
    
    public EmploymentStatus getEmploymentStatus() { return employmentStatus; }
    public void setEmploymentStatus(EmploymentStatus employmentStatus) { this.employmentStatus = employmentStatus; }
    
    public LocalDate getHiredAt() { return hiredAt; }
    public void setHiredAt(LocalDate hiredAt) { this.hiredAt = hiredAt; }
    
    public LocalDate getRetiredAt() { return retiredAt; }
    public void setRetiredAt(LocalDate retiredAt) { this.retiredAt = retiredAt; }
    
    public Integer getPaidLeaveRemainingDays() { return paidLeaveRemainingDays; }
    public void setPaidLeaveRemainingDays(Integer paidLeaveRemainingDays) { this.paidLeaveRemainingDays = paidLeaveRemainingDays; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Helper methods
    public boolean isActive() {
        return employmentStatus == EmploymentStatus.ACTIVE;
    }
    
    public boolean isAdmin() {
        return employeeRole == EmployeeRole.ADMIN;
    }
    
    public String getRoleForSpringSecurity() {
        return "ROLE_" + employeeRole.name();
    }
}