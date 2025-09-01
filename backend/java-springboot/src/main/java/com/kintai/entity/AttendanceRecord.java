package com.kintai.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "attendance_records", indexes = {
    @Index(name = "idx_employee_date", columnList = "employee_id, attendance_date", unique = true),
    @Index(name = "idx_attendance_date", columnList = "attendance_date")
})
public class AttendanceRecord {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id")
    private Long attendanceId;
    
    @Column(name = "employee_id", nullable = false)
    private Long employeeId;
    
    @Column(name = "attendance_date", nullable = false)
    private LocalDate attendanceDate;
    
    @Column(name = "clock_in_time")
    private LocalDateTime clockInTime;
    
    @Column(name = "clock_out_time")
    private LocalDateTime clockOutTime;
    
    @Column(name = "late_minutes", nullable = false)
    private Integer lateMinutes = 0;
    
    @Column(name = "early_leave_minutes", nullable = false)
    private Integer earlyLeaveMinutes = 0;
    
    @Column(name = "overtime_minutes", nullable = false)
    private Integer overtimeMinutes = 0;
    
    @Column(name = "night_shift_minutes", nullable = false)
    private Integer nightShiftMinutes = 0;
    
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false)
    private AttendanceStatus attendanceStatus = AttendanceStatus.normal;
    
    @Column(name = "submission_status", nullable = false)
    private String submissionStatus = "未提出";
    
    @Column(name = "attendance_fixed_flag", nullable = false)
    private Boolean attendanceFixedFlag = false;
    
    @CreationTimestamp
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;
    
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
    public void setEarlyLeaveMinutes(Integer earlyLeaveMinutes) { 
        this.earlyLeaveMinutes = earlyLeaveMinutes; 
    }
    
    public Integer getOvertimeMinutes() { return overtimeMinutes; }
    public void setOvertimeMinutes(Integer overtimeMinutes) { this.overtimeMinutes = overtimeMinutes; }
    
    public Integer getNightShiftMinutes() { return nightShiftMinutes; }
    public void setNightShiftMinutes(Integer nightShiftMinutes) { 
        this.nightShiftMinutes = nightShiftMinutes; 
    }
    
    public AttendanceStatus getAttendanceStatus() { return attendanceStatus; }
    public void setAttendanceStatus(AttendanceStatus attendanceStatus) { 
        this.attendanceStatus = attendanceStatus; 
    }
    
    public String getSubmissionStatus() { return submissionStatus; }
    public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }
    
    public Boolean getAttendanceFixedFlag() { return attendanceFixedFlag; }
    public void setAttendanceFixedFlag(Boolean attendanceFixedFlag) { 
        this.attendanceFixedFlag = attendanceFixedFlag; 
    }
    
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
    
    // Enum
    public enum AttendanceStatus {
        normal, paid_leave, absent
    }
}
