package com.kintai.dto;

import com.kintai.entity.AttendanceRecord;
import java.time.LocalDate;
import java.util.List;

public class AttendanceHistoryResponse {
    private EmployeeInfo employee;
    private PeriodInfo period;
    private List<AttendanceRecord> attendanceList;
    private AttendanceSummary summary;

    public EmployeeInfo getEmployee() { return employee; }
    public void setEmployee(EmployeeInfo employee) { this.employee = employee; }

    public PeriodInfo getPeriod() { return period; }
    public void setPeriod(PeriodInfo period) { this.period = period; }

    public List<AttendanceRecord> getAttendanceList() { return attendanceList; }
    public void setAttendanceList(List<AttendanceRecord> attendanceList) { this.attendanceList = attendanceList; }

    public AttendanceSummary getSummary() { return summary; }
    public void setSummary(AttendanceSummary summary) { this.summary = summary; }

    public static class EmployeeInfo {
        private Long employeeId;
        private String employeeName;
        private String employeeCode;

        public EmployeeInfo(Long employeeId, String employeeName, String employeeCode) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.employeeCode = employeeCode;
        }

        public Long getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public String getEmployeeCode() { return employeeCode; }
    }

    public static class PeriodInfo {
        private LocalDate from;
        private LocalDate to;

        public PeriodInfo(LocalDate from, LocalDate to) {
            this.from = from;
            this.to = to;
        }

        public LocalDate getFrom() { return from; }
        public LocalDate getTo() { return to; }
    }

    public static class AttendanceSummary {
        private Integer totalWorkingMinutes;
        private Integer totalOvertimeMinutes;
        private Integer totalNightShiftMinutes;
        private Integer totalLateMinutes;
        private Integer totalEarlyLeaveMinutes;
        private Integer paidLeaveDays;
        private Integer absentDays;

        public Integer getTotalWorkingMinutes() { return totalWorkingMinutes; }
        public void setTotalWorkingMinutes(Integer totalWorkingMinutes) { this.totalWorkingMinutes = totalWorkingMinutes; }
        public Integer getTotalOvertimeMinutes() { return totalOvertimeMinutes; }
        public void setTotalOvertimeMinutes(Integer totalOvertimeMinutes) { this.totalOvertimeMinutes = totalOvertimeMinutes; }
        public Integer getTotalNightShiftMinutes() { return totalNightShiftMinutes; }
        public void setTotalNightShiftMinutes(Integer totalNightShiftMinutes) { this.totalNightShiftMinutes = totalNightShiftMinutes; }
        public Integer getTotalLateMinutes() { return totalLateMinutes; }
        public void setTotalLateMinutes(Integer totalLateMinutes) { this.totalLateMinutes = totalLateMinutes; }
        public Integer getTotalEarlyLeaveMinutes() { return totalEarlyLeaveMinutes; }
        public void setTotalEarlyLeaveMinutes(Integer totalEarlyLeaveMinutes) { this.totalEarlyLeaveMinutes = totalEarlyLeaveMinutes; }
        public Integer getPaidLeaveDays() { return paidLeaveDays; }
        public void setPaidLeaveDays(Integer paidLeaveDays) { this.paidLeaveDays = paidLeaveDays; }
        public Integer getAbsentDays() { return absentDays; }
        public void setAbsentDays(Integer absentDays) { this.absentDays = absentDays; }
    }
}


