package com.kintai.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records", 
       indexes = {
           @Index(name = "idx_employee_date", columnList = "employee_id, attendance_date"),
           @Index(name = "idx_attendance_date", columnList = "attendance_date")
       })
@EntityListeners(AuditingEntityListener.class)
public class AttendanceRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;
    
    @NotNull
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @NotNull
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;
    
    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;
    
    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;
    
    @NotNull
    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes = 0;
    
    @NotNull
    @Column(name = "early_leave_minutes", nullable = false)
    private Integer earlyLeaveMinutes = 0;
    
    @NotNull
    @Column(name = "overtime_minutes", nullable = false)
    private Integer overtimeMinutes = 0;
    
    @NotNull
    @Column(name = "night_shift_minutes", nullable = false)
    private Integer nightShiftMinutes = 0;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private AttendanceStatus attendanceStatus = AttendanceStatus.NORMAL;
    
    @NotNull
    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", nullable = false)
    private SubmissionStatus submissionStatus = SubmissionStatus.未提出;
    
    @NotNull
    @Column(name = "attendance_fixed_flag", columnDefinition = "TINYINT(1)", nullable = false)
    private Boolean attendanceFixedFlag = false;
    
    @CreatedDate
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;
    
    @LastModifiedDate
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
    // Foreign key relationship
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;
    
    // Enums - 設計書通りの値を使用
    public enum AttendanceStatus {
        NORMAL("normal"), PAID_LEAVE("paid_leave"), ABSENT("absent");
        
        private final String value;
        AttendanceStatus(String value) { this.value = value; }
        public String getValue() { return value; }
    }
    
    public enum SubmissionStatus {
        未提出("未提出"), 申請済("申請済"), 承認("承認"), 却下("却下");
        
        private final String value;
        SubmissionStatus(String value) { this.value = value; }
        public String getValue() { return value; }
    }
    
    // Constructors
    public AttendanceRecord() {}
    
    public AttendanceRecord(Long employeeId, LocalDate attendanceDate) {
        this.employeeId = employeeId;
        this.attendanceDate = attendanceDate;
    }
    
    // Getters and Setters
    public Long getAttendanceId() { return attendanceId; }
    public void setAttendanceId(Long attendanceId) { this.attendanceId = attendanceId; }
    
    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    
    public LocalDate getAttendanceDate() { return attendanceDate; }
    public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
    
    public LocalDateTime getClockInTime() { return clockInTime; }
    public void setClockInTime(LocalDateTime clockInTime) { this.clockInTime = clockInTime; }
    
    public LocalDateTime getClockOutTime() { return clockOutTime; }
    public void setClockOutTime(LocalDateTime clockOutTime) { this.clockOutTime = clockOutTime; }
    
    public Integer getLateMinutes() { return lateMinutes; }
    public void setLateMinutes(Integer lateMinutes) { this.lateMinutes = lateMinutes; }
    
    public Integer getEarlyLeaveMinutes() { return earlyLeaveMinutes; }
    public void setEarlyLeaveMinutes(Integer earlyLeaveMinutes) { this.earlyLeaveMinutes = earlyLeaveMinutes; }
    
    public Integer getOvertimeMinutes() { return overtimeMinutes; }
    public void setOvertimeMinutes(Integer overtimeMinutes) { this.overtimeMinutes = overtimeMinutes; }
    
    public Integer getNightShiftMinutes() { return nightShiftMinutes; }
    public void setNightShiftMinutes(Integer nightShiftMinutes) { this.nightShiftMinutes = nightShiftMinutes; }
    
    public AttendanceStatus getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(AttendanceStatus attendanceStatus) { this.attendanceStatus = attendanceStatus; }
    
    public SubmissionStatus getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(SubmissionStatus submissionStatus) { this.submissionStatus = submissionStatus; }
    
    public Boolean getAttendanceFixedFlag() { return attendanceFixedFlag; }
    public void setAttendanceFixedFlag(Boolean attendanceFixedFlag) { this.attendanceFixedFlag = attendanceFixedFlag; }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    public Employee getEmployee() { return employee; }
    public void setEmployee(Employee employee) { this.employee = employee; }
    
    // Helper methods
    public boolean hasClockInTime() { return clockInTime != null; }
    public boolean hasClockOutTime() { return clockOutTime != null; }
    public boolean isCompleteAttendance() { return hasClockInTime() && hasClockOutTime(); }
    public boolean isFixed() { return attendanceFixedFlag != null && attendanceFixedFlag; }
    public boolean canModify() { return !isFixed(); }
    
    /**
     * 実働時間計算（分単位）- 設計書通りの仕様
     * 昼休憩（12:00-13:00）は自動控除
     */
    public Integer getTotalWorkingMinutes() {
        if (!isCompleteAttendance()) return 0;
        
        long totalMinutes = java.time.Duration.between(clockInTime, clockOutTime).toMinutes();
        
        // 昼休憩時間控除（12:00-13:00にかかる場合）
        if (clockInTime.toLocalTime().isBefore(java.time.LocalTime.of(13, 0)) &&
            clockOutTime.toLocalTime().isAfter(java.time.LocalTime.of(12, 0))) {
            totalMinutes -= 60; // 60分控除
        }
        
        return Math.max(0, (int) totalMinutes);
    }
}