package com.kintai.controller;

import com.kintai.dto.AttendanceResponse;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.service.AttendanceService;
import com.kintai.service.EmployeeService;
import com.kintai.util.DateUtil;
import jakarta.servlet.http.HttpSession;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class AttendanceController {
    
    private static final Logger logger = LoggerFactory.getLogger(AttendanceController.class);
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private DateUtil dateUtil;
    
    @PostMapping("/clock-in")
    public AttendanceResponse clockIn(HttpSession session) {
        Long employeeId = (Long) session.getAttribute("employeeId");
        
        if (employeeId == null) {
            return AttendanceResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        try {
            AttendanceRecord record = attendanceService.clockIn(employeeId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("attendanceRecordId", record.getAttendanceId());
            data.put("clockInTime", record.getClockInTime());
            data.put("lateMinutes", record.getLateMinutes());
            
            String message = "出勤打刻が完了しました";
            if (record.getLateMinutes() > 0) {
                message += "（" + record.getLateMinutes() + "分遅刻）";
            }
            
            return AttendanceResponse.success(data, message);
            
        } catch (Exception e) {
            logger.error("Clock in error for employee: " + employeeId, e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @PostMapping("/clock-out")
    public AttendanceResponse clockOut(HttpSession session) {
        Long employeeId = (Long) session.getAttribute("employeeId");
        
        if (employeeId == null) {
            return AttendanceResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        try {
            AttendanceRecord record = attendanceService.clockOut(employeeId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("attendanceRecordId", record.getAttendanceId());
            data.put("clockOutTime", record.getClockOutTime());
            data.put("overtimeMinutes", record.getOvertimeMinutes());
            data.put("nightShiftMinutes", record.getNightShiftMinutes());
            
            String message = "退勤打刻が完了しました";
            if (record.getOvertimeMinutes() > 0) {
                message += "（" + record.getOvertimeMinutes() + "分残業）";
            }
            
            return AttendanceResponse.success(data, message);
            
        } catch (Exception e) {
            logger.error("Clock out error for employee: " + employeeId, e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @GetMapping("/history")
    public AttendanceResponse getHistory(@RequestParam String yearMonth, HttpSession session) {
        Long employeeId = (Long) session.getAttribute("employeeId");
        
        if (employeeId == null) {
            return AttendanceResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        try {
            List<AttendanceRecord> records = attendanceService.getAttendanceHistory(employeeId, yearMonth);
            Employee employee = employeeService.getEmployee(employeeId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("employee", Map.of(
                "employeeId", employeeId,
                "employeeName", employee.getEmployeeName(),
                "employeeCode", employee.getEmployeeCode()
            ));
            
            String[] yearMonthParts = yearMonth.split("-");
            LocalDate firstDay = LocalDate.of(Integer.parseInt(yearMonthParts[0]), Integer.parseInt(yearMonthParts[1]), 1);
            LocalDate lastDay = firstDay.withDayOfMonth(firstDay.lengthOfMonth());
            
            data.put("period", Map.of(
                "from", firstDay.toString(),
                "to", lastDay.toString()
            ));
            
            // レコードを表示用に変換
            List<Map<String, Object>> attendanceList = records.stream().map(record -> {
                Map<String, Object> item = new HashMap<>();
                item.put("attendanceDate", dateUtil.formatDate(record.getAttendanceDate()));
                item.put("clockInTime", record.getClockInTime() != null ? 
                    record.getClockInTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null);
                item.put("clockOutTime", record.getClockOutTime() != null ? 
                    record.getClockOutTime().toLocalTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null);
                item.put("lateMinutes", record.getLateMinutes());
                item.put("earlyLeaveMinutes", record.getEarlyLeaveMinutes());
                item.put("overtimeMinutes", record.getOvertimeMinutes());
                item.put("nightShiftMinutes", record.getNightShiftMinutes());
                item.put("attendanceStatus", record.getAttendanceStatus().name());
                item.put("submissionStatus", record.getSubmissionStatus());
                item.put("attendanceFixedFlag", record.getAttendanceFixedFlag());
                return item;
            }).toList();
            
            data.put("attendanceList", attendanceList);
            
            // 集計データ
            Map<String, Object> summary = calculateSummary(records);
            data.put("summary", summary);
            
            return AttendanceResponse.success(data);
            
        } catch (Exception e) {
            logger.error("Get attendance history error for employee: " + employeeId, e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @PostMapping("/monthly-submit")
    public AttendanceResponse submitMonthlyAttendance(@RequestBody Map<String, String> request, HttpSession session) {
        Long employeeId = (Long) session.getAttribute("employeeId");
        
        if (employeeId == null) {
            return AttendanceResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        try {
            String targetMonth = request.get("targetMonth");
            attendanceService.submitMonthlyAttendance(employeeId, targetMonth);
            
            return AttendanceResponse.success(null, targetMonth + "分の勤怠申請が完了しました");
            
        } catch (Exception e) {
            logger.error("Monthly submission error for employee: " + employeeId, e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    private Map<String, Object> calculateSummary(List<AttendanceRecord> records) {
        Map<String, Object> summary = new HashMap<>();
        
        int totalWorkingMinutes = 0;
        int totalOvertimeMinutes = 0;
        int totalNightShiftMinutes = 0;
        int totalLateMinutes = 0;
        int totalEarlyLeaveMinutes = 0;
        int paidLeaveDays = 0;
        int absentDays = 0;
        
        for (AttendanceRecord record : records) {
            if (record.getClockInTime() != null && record.getClockOutTime() != null) {
                long minutes = java.time.Duration.between(record.getClockInTime(), record.getClockOutTime()).toMinutes();
                totalWorkingMinutes += Math.max(0, (int) minutes - 60); // 昼休憩控除
            }
            
            totalOvertimeMinutes += record.getOvertimeMinutes();
            totalNightShiftMinutes += record.getNightShiftMinutes();
            totalLateMinutes += record.getLateMinutes();
            totalEarlyLeaveMinutes += record.getEarlyLeaveMinutes();
            
            if (record.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.paid_leave) {
                paidLeaveDays++;
            } else if (record.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.absent) {
                absentDays++;
            }
        }
        
        summary.put("totalWorkingMinutes", totalWorkingMinutes);
        summary.put("totalOvertimeMinutes", totalOvertimeMinutes);
        summary.put("totalNightShiftMinutes", totalNightShiftMinutes);
        summary.put("totalLateMinutes", totalLateMinutes);
        summary.put("totalEarlyLeaveMinutes", totalEarlyLeaveMinutes);
        summary.put("paidLeaveDays", paidLeaveDays);
        summary.put("absentDays", absentDays);
        
        return summary;
    }
}