package com.kintai.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class AttendanceResponse {
    
    private Boolean success;
    private AttendanceData data;
    private String message;
    private String errorCode;
    
    public AttendanceResponse() {}
    
    public AttendanceResponse(Boolean success, AttendanceData data, String message) {
        this.success = success;
        this.data = data;
        this.message = message;
    }
    
    public AttendanceResponse(Boolean success, String errorCode, String message) {
        this.success = success;
        this.errorCode = errorCode;
        this.message = message;
    }
    
    public Boolean getSuccess() { return success; }
    public void setSuccess(Boolean success) { this.success = success; }
    
    public AttendanceData getData() { return data; }
    public void setData(AttendanceData data) { this.data = data; }
    
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
    
    public String getErrorCode() { return errorCode; }
    public void setErrorCode(String errorCode) { this.errorCode = errorCode; }
    
    public static class AttendanceData {
        private Long attendanceRecordId;
        private LocalDateTime clockInTime;
        private LocalDateTime clockOutTime;
        private Integer lateMinutes;
        private Integer earlyLeaveMinutes;
        private Integer overtimeMinutes;
        private Integer nightShiftMinutes;
        private Integer workingMinutes;
        private EmployeeInfo employee;
        private PeriodInfo period;
        private List<AttendanceRecord> attendanceList;
        private AttendanceSummary summary;
        
        public AttendanceData() {}
        
        // Getters and Setters
        public Long getAttendanceRecordId() { return attendanceRecordId; }
        public void setAttendanceRecordId(Long attendanceRecordId) { 
            this.attendanceRecordId = attendanceRecordId; 
        }
        
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
        
        public Integer getWorkingMinutes() { return workingMinutes; }
        public void setWorkingMinutes(Integer workingMinutes) { this.workingMinutes = workingMinutes; }
        
        public EmployeeInfo getEmployee() { return employee; }
        public void setEmployee(EmployeeInfo employee) { this.employee = employee; }
        
        public PeriodInfo getPeriod() { return period; }
        public void setPeriod(PeriodInfo period) { this.period = period; }
        
        public List<AttendanceRecord> getAttendanceList() { return attendanceList; }
        public void setAttendanceList(List<AttendanceRecord> attendanceList) { 
            this.attendanceList = attendanceList; 
        }
        
        public AttendanceSummary getSummary() { return summary; }
        public void setSummary(AttendanceSummary summary) { this.summary = summary; }
    }
    
    public static class EmployeeInfo {
        private Long employeeId;
        private String employeeName;
        private String employeeCode;
        
        public EmployeeInfo() {}
        
        public EmployeeInfo(Long employeeId, String employeeName, String employeeCode) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.employeeCode = employeeCode;
        }
        
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        
        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    }
    
    public static class PeriodInfo {
        private LocalDate from;
        private LocalDate to;
        
        public PeriodInfo() {}
        
        public PeriodInfo(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }
        
        public LocalDate getFrom() { return from; }
        public void setFrom(LocalDate from) { this.from = from; }
        
        public LocalDate getTo() { return to; }
        public void setTo(LocalDate to) { this.to = to; }
    }
    
    public static class AttendanceRecord {
        private LocalDate attendanceDate;
        private String clockInTime;
        private String clockOutTime;
        private Integer lateMinutes;
        private Integer earlyLeaveMinutes;
        private Integer overtimeMinutes;
        private Integer nightShiftMinutes;
        private String attendanceStatus;
        private String submissionStatus;
        private Boolean attendanceFixedFlag;
        
        public AttendanceRecord() {}
        
        // Getters and Setters
        public LocalDate getAttendanceDate() { return attendanceDate; }
        public void setAttendanceDate(LocalDate attendanceDate) { this.attendanceDate = attendanceDate; }
        
        public String getClockInTime() { return clockInTime; }
        public void setClockInTime(String clockInTime) { this.clockInTime = clockInTime; }
        
        public String getClockOutTime() { return clockOutTime; }
        public void setClockOutTime(String clockOutTime) { this.clockOutTime = clockOutTime; }
        
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
        
        public String getAttendanceStatus() { return attendanceStatus; }
        public void setAttendanceStatus(String attendanceStatus) { this.attendanceStatus = attendanceStatus; }
        
        public String getSubmissionStatus() { return submissionStatus; }
        public void setSubmissionStatus(String submissionStatus) { this.submissionStatus = submissionStatus; }
        
        public Boolean getAttendanceFixedFlag() { return attendanceFixedFlag; }
        public void setAttendanceFixedFlag(Boolean attendanceFixedFlag) { 
            this.attendanceFixedFlag = attendanceFixedFlag; 
        }
    }
    
    public static class AttendanceSummary {
        private Integer totalWorkingMinutes;
        private Integer totalOvertimeMinutes;
        private Integer totalNightShiftMinutes;
        private Integer totalLateMinutes;
        private Integer totalEarlyLeaveMinutes;
        private Integer paidLeaveDays;
        private Integer absentDays;
        
        public AttendanceSummary() {}
        
        // Getters and Setters
        public Integer getTotalWorkingMinutes() { return totalWorkingMinutes; }
        public void setTotalWorkingMinutes(Integer totalWorkingMinutes) { 
            this.totalWorkingMinutes = totalWorkingMinutes; 
        }
        
        public Integer getTotalOvertimeMinutes() { return totalOvertimeMinutes; }
        public void setTotalOvertimeMinutes(Integer totalOvertimeMinutes) { 
            this.totalOvertimeMinutes = totalOvertimeMinutes; 
        }
        
        public Integer getTotalNightShiftMinutes() { return totalNightShiftMinutes; }
        public void setTotalNightShiftMinutes(Integer totalNightShiftMinutes) { 
            this.totalNightShiftMinutes = totalNightShiftMinutes; 
        }
        
        public Integer getTotalLateMinutes() { return totalLateMinutes; }
        public void setTotalLateMinutes(Integer totalLateMinutes) { this.totalLateMinutes = totalLateMinutes; }
        
        public Integer getTotalEarlyLeaveMinutes() { return totalEarlyLeaveMinutes; }
        public void setTotalEarlyLeaveMinutes(Integer totalEarlyLeaveMinutes) { 
            this.totalEarlyLeaveMinutes = totalEarlyLeaveMinutes; 
        }
        
        public Integer getPaidLeaveDays() { return paidLeaveDays; }
        public void setPaidLeaveDays(Integer paidLeaveDays) { this.paidLeaveDays = paidLeaveDays; }
        
        public Integer getAbsentDays() { return absentDays; }
        public void setAbsentDays(Integer absentDays) { this.absentDays = absentDays; }
    }
}