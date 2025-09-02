package com.kintai.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests",
       indexes = {
           @Index(name = "idx_employee_leave_date", columnList = "employee_id, leave_request_date"),
           @Index(name = "idx_leave_date", columnList = "leave_request_date"),
           @Index(name = "idx_status", columnList = "leave_request_status")
       })
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_request_id")
    private Long leaveRequestId;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "leave_request_date", nullable = false)
    private LocalDate leaveRequestDate;
    
    @Column(name = "leave_request_reason", nullable = false, length = 200)
    private String leaveRequestReason;
    
    @Column(name = "leave_request_status", nullable = false, length = 10)
    private String leaveRequestStatus = "未処理";
    
    @Column(name = "approved_by_employee_id")
    private Long approvedByEmployeeId;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @CreationTimestamp
    @Column(name = "created_at")
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
    
    public LeaveRequest() {}
    
    public LeaveRequest(Long employeeId, LocalDate leaveRequestDate, String leaveRequestReason) {
        this.employeeId = employeeId;
        this.leaveRequestDate = leaveRequestDate;
        this.leaveRequestReason = leaveRequestReason;
    }
    
    // Getters and Setters
    public Long getLeaveRequestId() { return leaveRequestId; }
    public void setLeaveRequestId(Long leaveRequestId) { this.leaveRequestId = leaveRequestId; }
    
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    
    public LocalDate getLeaveRequestDate() { return leaveRequestDate; }
    public void setLeaveRequestDate(LocalDate leaveRequestDate) { 
        this.leaveRequestDate = leaveRequestDate; 
    }
    
    public String getLeaveRequestReason() { return leaveRequestReason; }
    public void setLeaveRequestReason(String leaveRequestReason) { 
        this.leaveRequestReason = leaveRequestReason; 
    }
    
    public String getLeaveRequestStatus() { return leaveRequestStatus; }
    public void setLeaveRequestStatus(String leaveRequestStatus) { 
        this.leaveRequestStatus = leaveRequestStatus; 
    }
    
    public Long getApprovedByEmployeeId() { return approvedByEmployeeId; }
    public void setApprovedByEmployeeId(Long approvedByEmployeeId) { 
        this.approvedByEmployeeId = approvedByEmployeeId; 
    }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}