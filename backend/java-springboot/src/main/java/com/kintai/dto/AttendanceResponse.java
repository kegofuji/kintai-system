package com.kintai.dto;

import java.time.LocalDateTime;

public class AttendanceResponse {
    private Long attendanceRecordId;
    private LocalDateTime clockInTime;
    private LocalDateTime clockOutTime;
    private Integer lateMinutes;
    private Integer earlyLeaveMinutes;
    private Integer overtimeMinutes;
    private Integer nightShiftMinutes;
    private Integer workingMinutes;
    private String message;

    public Long getAttendanceRecordId() { return attendanceRecordId; }
    public void setAttendanceRecordId(Long attendanceRecordId) { this.attendanceRecordId = attendanceRecordId; }
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
    public Integer getWorkingMinutes() { return workingMinutes; }
    public void setWorkingMinutes(Integer workingMinutes) { this.workingMinutes = workingMinutes; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}