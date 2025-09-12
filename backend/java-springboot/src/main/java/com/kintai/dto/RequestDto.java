package com.kintai.dto;

import jakarta.validation.constraints.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

public class RequestDto {
    

    public static class ClockRequest {
        @NotNull(message = "社員IDは必須です")
        private Long employeeId;
        
        public ClockRequest() {}
        
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    }
    

    public static class MonthlySubmissionRequest {
        @NotNull(message = "社員IDは必須です")
        private Long employeeId;
        
        @NotBlank(message = "対象月は必須です")
        @Pattern(regexp = "\\d{4}-\\d{2}", message = "対象月はYYYY-MM形式で入力してください")
        private String targetMonth;
        
        public MonthlySubmissionRequest() {}
        
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        
        public String getTargetMonth() { return targetMonth; }
        public void setTargetMonth(String targetMonth) { this.targetMonth = targetMonth; }
    }
    

    public static class LeaveRequestDto {
        @NotNull(message = "社員IDは必須です")
        private Long employeeId;
        
        @NotNull(message = "有給取得日は必須です")
        @Future(message = "有給取得日は明日以降を選択してください")
        private LocalDate leaveDate;
        
        @NotBlank(message = "理由は必須です")
        @Size(max = 200, message = "理由は200文字以内で入力してください")
        private String reason;
        
        public LeaveRequestDto() {}
        
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        
        public LocalDate getLeaveDate() { return leaveDate; }
        public void setLeaveDate(LocalDate leaveDate) { this.leaveDate = leaveDate; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    

    public static class AdjustmentRequestDto {
        @NotNull(message = "社員IDは必須です")
        private Long employeeId;
        
        @NotNull(message = "修正対象日は必須です")
        @PastOrPresent(message = "修正対象日は当日または過去日を選択してください")
        private LocalDate targetDate;
        
        private LocalDateTime correctedClockInTime;
        
        private LocalDateTime correctedClockOutTime;
        
        @NotBlank(message = "理由は必須です")
        @Size(max = 200, message = "理由は200文字以内で入力してください")
        private String reason;
        
        public AdjustmentRequestDto() {}
        
        @AssertTrue(message = "出勤時刻または退勤時刻の少なくとも一方は必須です")
        public boolean isAtLeastOneTimeProvided() {
            return correctedClockInTime != null || correctedClockOutTime != null;
        }
        
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        
        public LocalDate getTargetDate() { return targetDate; }
        public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
        
        public LocalDateTime getCorrectedClockInTime() { return correctedClockInTime; }
        public void setCorrectedClockInTime(LocalDateTime correctedClockInTime) { 
            this.correctedClockInTime = correctedClockInTime; 
        }
        
        public LocalDateTime getCorrectedClockOutTime() { return correctedClockOutTime; }
        public void setCorrectedClockOutTime(LocalDateTime correctedClockOutTime) { 
            this.correctedClockOutTime = correctedClockOutTime; 
        }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
    

    public static class ApprovalRequest {
        @NotNull(message = "承認者IDは必須です")
        private Long approverId;
        
        private String comment;
        
        public ApprovalRequest() {}
        
        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
        
        public String getComment() { return comment; }
        public void setComment(String comment) { this.comment = comment; }
    }
    

    public static class RejectionRequest {
        @NotNull(message = "承認者IDは必須です")
        private Long approverId;
        
        @NotBlank(message = "却下理由は必須です")
        @Size(max = 200, message = "却下理由は200文字以内で入力してください")
        private String rejectionReason;
        
        public RejectionRequest() {}
        
        public Long getApproverId() { return approverId; }
        public void setApproverId(Long approverId) { this.approverId = approverId; }
        
        public String getRejectionReason() { return rejectionReason; }
        public void setRejectionReason(String rejectionReason) { this.rejectionReason = rejectionReason; }
    }
    
  
    public static class EmployeeRegistrationRequest {
        @NotBlank(message = "社員IDは必須です")
        @Size(min = 3, max = 10, message = "社員IDは3-10文字の半角英数字で入力してください")
        @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "社員IDは半角英数字のみで入力してください")
        private String employeeCode;
        
        @NotBlank(message = "氏名は必須です")
        @Size(max = 50, message = "氏名は50文字以内で入力してください")
        private String employeeName;
        
        @NotBlank(message = "メールアドレスは必須です")
        @Email(message = "正しいメールアドレスを入力してください")
        @Size(max = 100, message = "メールアドレスは100文字以内で入力してください")
        private String email;
        
        @NotBlank(message = "パスワードは必須です")
        @Size(min = 8, max = 20, message = "パスワードは8-20文字で入力してください")
        private String password;
        
        public EmployeeRegistrationRequest() {}
        
        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
        
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
    }
    
    public static class PaidLeaveAdjustmentRequest {
        @NotNull(message = "社員IDは必須です")
        private Long employeeId;
        
        @NotNull(message = "調整日数は必須です")
        @Min(value = -99, message = "調整日数は-99以上で入力してください")
        @Max(value = 99, message = "調整日数は99以下で入力してください")
        private Integer adjustmentDays;
        
        @NotBlank(message = "調整理由は必須です")
        @Size(max = 200, message = "調整理由は200文字以内で入力してください")
        private String reason;
        
        public PaidLeaveAdjustmentRequest() {}
        
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        
        public Integer getAdjustmentDays() { return adjustmentDays; }
        public void setAdjustmentDays(Integer adjustmentDays) { this.adjustmentDays = adjustmentDays; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}