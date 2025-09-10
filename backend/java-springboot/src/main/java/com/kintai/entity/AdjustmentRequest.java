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
@Table(name = "adjustment_requests",
       indexes = {
           @Index(name = "idx_employee_target_date", columnList = "employee_id, adjustment_target_date"),
           @Index(name = "idx_target_date", columnList = "adjustment_target_date"),
           @Index(name = "idx_status", columnList = "adjustment_status")
       })
@EntityListeners(AuditingEntityListener.class)
public class AdjustmentRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjustment_request_id")
    private Long adjustmentRequestId;
    
    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @NotNull
    @Column(name = "adjustment_target_date", nullable = false)
    private LocalDate adjustmentTargetDate;
    
    @Column(name = "original_clock_in_time")
    private LocalDateTime originalClockInTime;
    
    @Column(name = "original_clock_out_time")
    private LocalDateTime originalClockOutTime;
    
    @Column(name = "adjustment_requested_time_in")
    private LocalDateTime adjustmentRequestedTimeIn;
    
    @Column(name = "adjustment_requested_time_out")
    private LocalDateTime adjustmentRequestedTimeOut;
    
    @NotBlank
    @Column(name = "adjustment_reason", length = 200, nullable = false)
    private String adjustmentReason;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_status", nullable = false)
    private AdjustmentStatus adjustmentStatus = AdjustmentStatus.未処理;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "approved_by_employee_id")
    private Long approvedByEmployeeId;
    
    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;
    
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
    
    // Enums - 設計書通りの日本語ステータス
    public enum AdjustmentStatus {
        未処理("未処理"), 承認("承認"), 却下("却下");
        
        private final String value;
        AdjustmentStatus(String value) { this.value = value; }
        public String getValue() { return value; }
    }
    
    // Constructors
    public AdjustmentRequest() {}
    
    public AdjustmentRequest(Long employeeId, LocalDate adjustmentTargetDate, String adjustmentReason) {
        this.employeeId = employeeId;
        this.adjustmentTargetDate = adjustmentTargetDate;
        this.adjustmentReason = adjustmentReason;
    }
    
    // Getters and Setters
    public Long getAdjustmentRequestId() { return adjustmentRequestId; }
    public void setAdjustmentRequestId(Long adjustmentRequestId) { this.adjustmentRequestId = adjustmentRequestId; }
    
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    
    public LocalDate getAdjustmentTargetDate() { return adjustmentTargetDate; }
    public void setAdjustmentTargetDate(LocalDate adjustmentTargetDate) { this.adjustmentTargetDate = adjustmentTargetDate; }
    
    public LocalDateTime getOriginalClockInTime() { return originalClockInTime; }
    public void setOriginalClockInTime(LocalDateTime originalClockInTime) { this.originalClockInTime = originalClockInTime; }
    
    public LocalDateTime getOriginalClockOutTime() { return originalClockOutTime; }
    public void setOriginalClockOutTime(LocalDateTime originalClockOutTime) { this.originalClockOutTime = originalClockOutTime; }
    
    public LocalDateTime getAdjustmentRequestedTimeIn() { return adjustmentRequestedTimeIn; }
    public void setAdjustmentRequestedTimeIn(LocalDateTime adjustmentRequestedTimeIn) { this.adjustmentRequestedTimeIn = adjustmentRequestedTimeIn; }
    
    public LocalDateTime getAdjustmentRequestedTimeOut() { return adjustmentRequestedTimeOut; }
    public void setAdjustmentRequestedTimeOut(LocalDateTime adjustmentRequestedTimeOut) { this.adjustmentRequestedTimeOut = adjustmentRequestedTimeOut; }
    
    public String getAdjustmentReason() { return adjustmentReason; }
    public void setAdjustmentReason(String adjustmentReason) { this.adjustmentReason = adjustmentReason; }
    
    public AdjustmentStatus getAdjustmentStatus() { return adjustmentStatus; }
    public void setAdjustmentStatus(AdjustmentStatus adjustmentStatus) { this.adjustmentStatus = adjustmentStatus; }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public Long getApprovedByEmployeeId() { return approvedByEmployeeId; }
    public void setApprovedByEmployeeId(Long approvedByEmployeeId) { this.approvedByEmployeeId = approvedByEmployeeId; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    public Employee getApprovedBy() { return approvedBy; }
    public void setApprovedBy(Employee approvedBy) { this.approvedBy = approvedBy; }
    
    // Helper methods
    public boolean isPending() { return adjustmentStatus == AdjustmentStatus.未処理; }
    public boolean isApproved() { return adjustmentStatus == AdjustmentStatus.承認; }
    public boolean isRejected() { return adjustmentStatus == AdjustmentStatus.却下; }
    
    public void approve(Long approverId) {
        this.adjustmentStatus = AdjustmentStatus.承認;
        this.approvedByEmployeeId = approverId;
        this.approvedAt = LocalDateTime.now();
    }
    
    public void reject(Long approverId, String reason) {
        this.adjustmentStatus = AdjustmentStatus.却下;
        this.approvedByEmployeeId = approverId;
        this.rejectionReason = reason;
        this.approvedAt = LocalDateTime.now();
    }
    
    public boolean hasValidTimeAdjustment() {
        return adjustmentRequestedTimeIn != null || adjustmentRequestedTimeOut != null;
    }
}