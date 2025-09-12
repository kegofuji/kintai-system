package com.kintai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "leave_requests",
       indexes = {
           @Index(name = "idx_employee_leave_date", columnList = "employee_id, leave_request_date"),
           @Index(name = "idx_leave_date", columnList = "leave_request_date"),
           @Index(name = "idx_status", columnList = "leave_request_status")
       })
@EntityListeners(AuditingEntityListener.class)
public class LeaveRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_request_id")
    private Long leaveRequestId;
    
    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @NotNull
    @Column(name = "leave_request_date", nullable = false)
    private LocalDate leaveRequestDate;
    
    @NotBlank
    @Column(name = "leave_request_reason", length = 200, nullable = false)
    private String leaveRequestReason;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_request_status", nullable = false)
    private LeaveRequestStatus leaveRequestStatus = LeaveRequestStatus.未処理;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by_employee_id")
    private Long approvedByEmployeeId;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Foreign key relationships
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_employee_id", insertable = false, updatable = false)
    private Employee approvedBy;
    
    // Enums 日本語ステータス
    public enum LeaveRequestStatus {
        未処理("未処理"), 承認("承認"), 却下("却下");
        
        private final String value;
        LeaveRequestStatus(String value) { this.value = value; }
        public String getValue() { return value; }
    }
    
 
    public LeaveRequest() {}
    
    public LeaveRequest(Long employeeId, LocalDate leaveRequestDate, String leaveRequestReason) {
        this.employeeId = employeeId;
        this.leaveRequestDate = leaveRequestDate;
        this.leaveRequestReason = leaveRequestReason;
    }
    

    public Long getLeaveRequestId() { return leaveRequestId; }
    public void setLeaveRequestId(Long leaveRequestId) { this.leaveRequestId = leaveRequestId; }
    
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    
    public LocalDate getLeaveRequestDate() { return leaveRequestDate; }
    public void setLeaveRequestDate(LocalDate leaveRequestDate) { this.leaveRequestDate = leaveRequestDate; }
    
    public String getLeaveRequestReason() { return leaveRequestReason; }
    public void setLeaveRequestReason(String leaveRequestReason) { this.leaveRequestReason = leaveRequestReason; }
    
    public LeaveRequestStatus getLeaveRequestStatus() { return leaveRequestStatus; }
    public void setLeaveRequestStatus(LeaveRequestStatus leaveRequestStatus) { this.leaveRequestStatus = leaveRequestStatus; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public Long getApprovedByEmployeeId() { return approvedByEmployeeId; }
    public void setApprovedByEmployeeId(Long approvedByEmployeeId) { this.approvedByEmployeeId = approvedByEmployeeId; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public Employee getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Employee approvedBy) { this.approvedBy = approvedBy; }
    
    // Helper methods
    public boolean isPending() { return leaveRequestStatus == LeaveRequestStatus.未処理; }
    public boolean isApproved() { return leaveRequestStatus == LeaveRequestStatus.承認; }
    public boolean isRejected() { return leaveRequestStatus == LeaveRequestStatus.却下; }
    
    public void approve(Long approverId) {
        this.leaveRequestStatus = LeaveRequestStatus.承認;
        this.approvedByEmployeeId = approverId;
        this.approvedAt = LocalDateTime.now();
    }
    
    public void reject(Long approverId) {
        this.leaveRequestStatus = LeaveRequestStatus.却下;
        this.approvedByEmployeeId = approverId;
        this.approvedAt = LocalDateTime.now();
    }
}