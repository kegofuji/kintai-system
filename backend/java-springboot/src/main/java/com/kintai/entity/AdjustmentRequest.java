package com.kintai.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "adjustment_requests", indexes = {
    @Index(name = "idx_employee_target_date", columnList = "employee_id, adjustment_target_date"),
    @Index(name = "idx_target_date", columnList = "adjustment_target_date"),
    @Index(name = "idx_adjustment_status", columnList = "adjustment_status")
})
public class AdjustmentRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjustment_request_id")
    private Long adjustmentRequestId;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
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
    
    @Column(name = "adjustment_reason", nullable = false, length = 200)
    private String adjustmentReason;
    
    @Column(name = "adjustment_status", nullable = false)
    private String adjustmentStatus = "未処理";
    
    @Column(name = "approved_by_employee_id")
    private Long approvedByEmployeeId;
    
    @Column(name = "approved_at")
    private LocalDateTime approvedAt;
    
    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Constructors
    public AdjustmentRequest() {}
    
    public AdjustmentRequest(Long employeeId, LocalDate adjustmentTargetDate, 
                           LocalDateTime requestedTimeIn, LocalDateTime requestedTimeOut,
                           String adjustmentReason) {
        this.employeeId = employeeId;
        this.adjustmentTargetDate = adjustmentTargetDate;
        this.adjustmentRequestedTimeIn = requestedTimeIn;
        this.adjustmentRequestedTimeOut = requestedTimeOut;
        this.adjustmentReason = adjustmentReason;
    }
    
    // Getters and Setters
    public Long getAdjustmentRequestId() { return adjustmentRequestId; }
    public void setAdjustmentRequestId(Long adjustmentRequestId) { 
        this.adjustmentRequestId = adjustmentRequestId; 
    }
    
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    
    public LocalDate getAdjustmentTargetDate() { return adjustmentTargetDate; }
    public void setAdjustmentTargetDate(LocalDate adjustmentTargetDate) { 
        this.adjustmentTargetDate = adjustmentTargetDate; 
    }
    
    public LocalDateTime getOriginalClockInTime() { return originalClockInTime; }
    public void setOriginalClockInTime(LocalDateTime originalClockInTime) { 
        this.originalClockInTime = originalClockInTime; 
    }
    
    public LocalDateTime getOriginalClockOutTime() { return originalClockOutTime; }
    public void setOriginalClockOutTime(LocalDateTime originalClockOutTime) { 
        this.originalClockOutTime = originalClockOutTime; 
    }
    
    public LocalDateTime getAdjustmentRequestedTimeIn() { return adjustmentRequestedTimeIn; }
    public void setAdjustmentRequestedTimeIn(LocalDateTime adjustmentRequestedTimeIn) { 
        this.adjustmentRequestedTimeIn = adjustmentRequestedTimeIn; 
    }
    
    public LocalDateTime getAdjustmentRequestedTimeOut() { return adjustmentRequestedTimeOut; }
    public void setAdjustmentRequestedTimeOut(LocalDateTime adjustmentRequestedTimeOut) { 
        this.adjustmentRequestedTimeOut = adjustmentRequestedTimeOut; 
    }
    
    public String getAdjustmentReason() { return adjustmentReason; }
    public void setAdjustmentReason(String adjustmentReason) { this.adjustmentReason = adjustmentReason; }
    
    public String getAdjustmentStatus() { return adjustmentStatus; }
    public void setAdjustmentStatus(String adjustmentStatus) { this.adjustmentStatus = adjustmentStatus; }
    
    public Long getApprovedByEmployeeId() { return approvedByEmployeeId; }
    public void setApprovedByEmployeeId(Long approvedByEmployeeId) { 
        this.approvedByEmployeeId = approvedByEmployeeId; 
    }
    
    public LocalDateTime getApprovedAt() { return approvedAt; }
    public void setApprovedAt(LocalDateTime approvedAt) { this.approvedAt = approvedAt; }
    
    public String getRejectionReason() { return rejectionReason; }
    public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
