package com.kintai.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;
import java.time.LocalTime;

public class RequestDto {
    
    // 有給申請用DTO
    public static class LeaveRequestDto {
        @NotNull(message = "取得日は必須です")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate leaveDate;
        
        @NotBlank(message = "理由は必須です")
        @Size(max = 200, message = "理由は200文字以内で入力してください")
        private String reason;
        
        // Constructors
        public LeaveRequestDto() {}
        
        public LeaveRequestDto(LocalDate leaveDate, String reason) {
            this.leaveDate = leaveDate;
            this.reason = reason;
        }
        
        // Getters and Setters
        public LocalDate getLeaveDate() { return leaveDate; }
        public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    // 打刻修正申請用DTO
    public static class AdjustmentRequestDto {
        @NotNull(message = "対象日は必須です")
        @JsonFormat(pattern = "yyyy-MM-dd")
        private LocalDate targetDate;
        
        @JsonFormat(pattern = "HH:mm")
        private LocalTime correctedClockInTime;
        
        @JsonFormat(pattern = "HH:mm")
        private LocalTime correctedClockOutTime;
        
        @NotBlank(message = "理由は必須です")
        @Size(max = 200, message = "理由は200文字以内で入力してください")
        private String reason;
        
        // Constructors
        public AdjustmentRequestDto() {}
        
        public AdjustmentRequestDto(LocalDate targetDate, LocalTime correctedClockInTime, 
                                   LocalTime correctedClockOutTime, String reason) {
            this.targetDate = targetDate;
            this.correctedClockInTime = correctedClockInTime;
            this.correctedClockOutTime = correctedClockOutTime;
            this.reason = reason;
        }
        
        // Getters and Setters
        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
        
        public LocalTime getCorrectedClockInTime() { return correctedClockInTime; }
        public void setCorrectedClockInTime(LocalTime correctedClockInTime) { 
            this.correctedClockInTime = correctedClockInTime; 
        }
        
        public LocalTime getCorrectedClockOutTime() { return correctedClockOutTime; }
        public void setCorrectedClockOutTime(LocalTime correctedClockOutTime) { 
            this.correctedClockOutTime = correctedClockOutTime; 
        }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    
    // 申請承認・却下用DTO
    public static class ApprovalDto {
        private Long approverId;
        private String comment;
        
        // Constructors
        public ApprovalDto() {}
        
        public ApprovalDto(Long approverId, String comment) {
            this.approverId = approverId;
            this.comment = comment;
        }
        
        // Getters and Setters
        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
    
    // 申請却下用DTO
    public static class RejectionDto {
        private Long approverId;
        
        @NotBlank(message = "却下理由は必須です")
        @Size(max = 200, message = "却下理由は200文字以内で入力してください")
        private String rejectionReason;
        
        // Constructors
        public RejectionDto() {}
        
        public RejectionDto(Long approverId, String rejectionReason) {
            this.approverId = approverId;
            this.rejectionReason = rejectionReason;
        }
        
        // Getters and Setters
        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
        
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
    
    // 社員追加用DTO
    public static class EmployeeCreateDto {
        @NotBlank(message