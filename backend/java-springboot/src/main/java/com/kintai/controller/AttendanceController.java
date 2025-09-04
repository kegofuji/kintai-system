package com.kintai.controller;

import com.kintai.dto.AttendanceResponse;
import com.kintai.dto.RequestDto;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.service.AttendanceService;
import com.kintai.service.AuthService;
import com.kintai.service.EmployeeService;
import com.kintai.util.TimeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private AuthService authService;
    
    @Autowired
    private EmployeeService employeeService;
    
    /**
     * 出勤打刻
     */
    @PostMapping("/clock-in")
    public ResponseEntity<Map<String, Object>> clockIn(@Valid @RequestBody RequestDto.ClockRequest request,
                                                      HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 権限チェック（本人または管理者のみ）
            if (!currentEmployee.getEmployeeId().equals(request.getEmployeeId()) &&
                currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new RuntimeException("ACCESS_DENIED");
            }
            
            AttendanceRecord record = attendanceService.clockIn(request.getEmployeeId());
            
            Map<String, Object> data = new HashMap<>();
            data.put("attendanceRecordId", record.getAttendanceId());
            data.put("clockInTime", record.getClockInTime());
            data.put("lateMinutes", record.getLateMinutes());
            
            String message = record.getLateMinutes() > 0 ? 
                String.format("出勤打刻が完了しました（%d分遅刻）", record.getLateMinutes()) :
                "出勤打刻が完了しました";
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", message);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("errorCode", e.getMessage());
            
            String message = switch (e.getMessage()) {
                case "ALREADY_CLOCKED_IN" -> "本日は既に出勤打刻済みです";
                case "ACCESS_DENIED" -> "アクセス権限がありません";
                case "SESSION_TIMEOUT" -> "セッションがタイムアウトしました";
                default -> "出勤打刻に失敗しました";
            };
            
            response.put("message", message);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 退勤打刻
     */
    @PostMapping("/clock-out")
    public ResponseEntity<Map<String, Object>> clockOut(@Valid @RequestBody RequestDto.ClockRequest request,
                                                       HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 権限チェック（本人または管理者のみ）
            if (!currentEmployee.getEmployeeId().equals(request.getEmployeeId()) &&
                currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new RuntimeException("ACCESS_DENIED");
            }
            
            AttendanceRecord record = attendanceService.clockOut(request.getEmployeeId());
            
            Map<String, Object> data = new HashMap<>();
            data.put("attendanceRecordId", record.getAttendanceId());
            data.put("clockOutTime", record.getClockOutTime());
            data.put("overtimeMinutes", record.getOvertimeMinutes());
            data.put("nightShiftMinutes", record.getNightShiftMinutes());
            data.put("workingMinutes", TimeCalculator.calculateWorkingMinutes(
                    record.getClockInTime(), record.getClockOutTime()));
            
            StringBuilder messageBuilder = new StringBuilder("退勤打刻が完了しました");
            if (record.getOvertimeMinutes() > 0) {
                messageBuilder.append(String.format("（%d分残業）", record.getOvertimeMinutes()));
            }
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", messageBuilder.toString());
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("errorCode", e.getMessage());
            
            String message = switch (e.getMessage()) {
                case "NOT_CLOCKED_IN" -> "出勤打刻が必要です";
                case "ALREADY_CLOCKED_OUT" -> "本日は既に退勤打刻済みです";
                case "ACCESS_DENIED" -> "アクセス権限がありません";
                case "SESSION_TIMEOUT" -> "セッションがタイムアウトしました";
                default -> "退勤打刻に失敗しました";
            };
            
            response.put("message", message);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 勤怠履歴取得
     */
    @GetMapping("/history")
    public ResponseEntity<Map<String, Object>> getAttendanceHistory(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 権限チェック
            Long targetEmployeeId = employeeId != null ? employeeId : currentEmployee.getEmployeeId();
            if (!currentEmployee.getEmployeeId().equals(targetEmployeeId) &&
                currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new RuntimeException("ACCESS_DENIED");
            }
            
            List<AttendanceRecord> records;
            
            if (yearMonth != null) {
                records = attendanceService.getAttendanceHistory(targetEmployeeId, yearMonth);
            } else if (dateFrom != null && dateTo != null) {
                LocalDate from = LocalDate.parse(dateFrom);
                LocalDate to = LocalDate.parse(dateTo);
                records = attendanceService.getAttendanceHistory(targetEmployeeId, from, to);
            } else {
                // デフォルトは当月
                String currentMonth = LocalDate.now().toString().substring(0, 7);
                records = attendanceService.getAttendanceHistory(targetEmployeeId, currentMonth);
            }
            
            Employee targetEmployee = employeeService.getEmployeeById(targetEmployeeId);
            
            AttendanceResponse.AttendanceData data = new AttendanceResponse.AttendanceData();
            data.setEmployee(new AttendanceResponse.EmployeeInfo(
                    targetEmployee.getEmployeeId(),
                    targetEmployee.getEmployeeName(),
                    targetEmployee.getEmployeeCode()));
            
            // 期間設定
            if (!records.isEmpty()) {
                LocalDate minDate = records.stream().map(AttendanceRecord::getAttendanceDate).min(LocalDate::compareTo).orElse(null);
                LocalDate maxDate = records.stream().map(AttendanceRecord::getAttendanceDate).max(LocalDate::compareTo).orElse(null);
                data.setPeriod(new AttendanceResponse.PeriodInfo(minDate, maxDate));
            }
            
            // 勤怠リスト変換
            List<AttendanceResponse.AttendanceRecord> attendanceList = records.stream()
                    .map(this::convertToResponseRecord)
                    .collect(Collectors.toList());
            data.setAttendanceList(attendanceList);
            
            // 集計情報
            AttendanceResponse.AttendanceSummary summary = calculateSummary(records);
            data.setSummary(summary);
            
            response.put("success", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("errorCode", e.getMessage());
            response.put("message", "勤怠履歴の取得に失敗しました");
            
            return ResponseEntity.badRequest().body(response);
            
        } catch (DateTimeParseException e) {
            response.put("success", false);
            response.put("errorCode", "INVALID_DATE_FORMAT");
            response.put("message", "日付の形式が正しくありません");
            
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 月末申請
     */
    @PostMapping("/monthly-submit")
    public ResponseEntity<Map<String, Object>> submitMonthlyAttendance(
            @Valid @RequestBody RequestDto.MonthlySubmissionRequest request,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 権限チェック（本人のみ）
            if (!currentEmployee.getEmployeeId().equals(request.getEmployeeId())) {
                throw new RuntimeException("ACCESS_DENIED");
            }
            
            attendanceService.submitMonthlyAttendance(request.getEmployeeId(), request.getTargetMonth());
            
            response.put("success", true);
            response.put("message", String.format("%s分の勤怠申請が完了しました", request.getTargetMonth()));
            
            return ResponseEntity.ok(response);
            
        } catch (RuntimeException e) {
            response.put("success", false);
            response.put("errorCode", e.getMessage());
            
            String message = switch (e.getMessage()) {
                case "INCOMPLETE_ATTENDANCE" -> "打刻漏れがあります";
                case "ACCESS_DENIED" -> "アクセス権限がありません";
                default -> "月末申請に失敗しました";
            };
            
            response.put("message", message);
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * AttendanceRecordをレスポンス用に変換
     */
    private AttendanceResponse.AttendanceRecord convertToResponseRecord(AttendanceRecord record) {
        AttendanceResponse.AttendanceRecord responseRecord = new AttendanceResponse.AttendanceRecord();
        responseRecord.setAttendanceDate(record.getAttendanceDate());
        responseRecord.setClockInTime(TimeCalculator.formatTimeToHHMM(record.getClockInTime()));
        responseRecord.setClockOutTime(TimeCalculator.formatTimeToHHMM(record.getClockOutTime()));
        responseRecord.setLateMinutes(record.getL# 

