package com.kintai.controller;

import com.kintai.dto.*;
import com.kintai.entity.AttendanceRecord;
import com.kintai.service.AttendanceService;
import com.kintai.service.AuthService;
import com.kintai.util.DateUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/attendance")
public class AttendanceController {
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private AuthService authService;
    
    
    
    /**
     * 出勤打刻
     */
    @PostMapping("/clock-in")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockIn(
            @Valid @RequestBody ClockInRequest request,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        // 権限チェック（自分の打刻のみ or 管理者）
        if (!sessionInfo.getEmployeeId().equals(request.getEmployeeId()) && 
            !"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "アクセス権限がありません"));
        }
        
        AttendanceService.ClockResult result = attendanceService.clockIn(request.getEmployeeId());
        
        if (result.isSuccess()) {
            AttendanceResponse response = new AttendanceResponse();
            response.setAttendanceRecordId(result.getRecord().getAttendanceId());
            response.setClockInTime(result.getRecord().getClockInTime());
            response.setLateMinutes(result.getRecord().getLateMinutes());
            response.setMessage(result.getMessage());
            
            return ResponseEntity.ok(ApiResponse.success(response, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * 退勤打刻
     */
    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<AttendanceResponse>> clockOut(
            @Valid @RequestBody ClockOutRequest request,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        // 権限チェック
        if (!sessionInfo.getEmployeeId().equals(request.getEmployeeId()) && 
            !"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "アクセス権限がありません"));
        }
        
        AttendanceService.ClockResult result = attendanceService.clockOut(request.getEmployeeId());
        
        if (result.isSuccess()) {
            AttendanceResponse response = new AttendanceResponse();
            response.setAttendanceRecordId(result.getRecord().getAttendanceId());
            response.setClockInTime(result.getRecord().getClockInTime());
            response.setClockOutTime(result.getRecord().getClockOutTime());
            response.setEarlyLeaveMinutes(result.getRecord().getEarlyLeaveMinutes());
            response.setOvertimeMinutes(result.getRecord().getOvertimeMinutes());
            response.setNightShiftMinutes(result.getRecord().getNightShiftMinutes());
            response.setWorkingMinutes(result.getRecord().getTotalWorkingMinutes());
            response.setMessage(result.getMessage());
            
            return ResponseEntity.ok(ApiResponse.success(response, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * 勤怠履歴取得
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<AttendanceHistoryResponse>> getHistory(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) String dateFrom,
            @RequestParam(required = false) String dateTo,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        // 管理者以外は自分の履歴のみ閲覧可能
        Long targetEmployeeId = employeeId;
        if (!"admin".equals(sessionInfo.getRole())) {
            targetEmployeeId = sessionInfo.getEmployeeId();
        }
        if (targetEmployeeId == null) {
            targetEmployeeId = sessionInfo.getEmployeeId();
        }
        
        try {
            LocalDate fromDate, toDate;
            
            // 日付範囲の決定
            if (yearMonth != null) {
                YearMonth ym = DateUtil.parseYearMonth(yearMonth);
                fromDate = DateUtil.getFirstDayOfMonth(ym);
                toDate = DateUtil.getLastDayOfMonth(ym);
            } else {
                fromDate = dateFrom != null ? DateUtil.parseDate(dateFrom) : LocalDate.now().withDayOfMonth(1);
                toDate = dateTo != null ? DateUtil.parseDate(dateTo) : LocalDate.now();
            }
            
            // データ取得
            List<AttendanceRecord> records = attendanceService.getAttendanceHistory(targetEmployeeId, fromDate, toDate);
            AttendanceService.AttendanceSummary summary = attendanceService.getAttendanceSummary(targetEmployeeId, fromDate, toDate);
            
            // 社員情報取得（実装は簡略化）
            AttendanceHistoryResponse.EmployeeInfo employeeInfo = 
                new AttendanceHistoryResponse.EmployeeInfo(targetEmployeeId, sessionInfo.getEmployeeName(), "E" + String.format("%03d", targetEmployeeId));
            
            AttendanceHistoryResponse.PeriodInfo periodInfo = 
                new AttendanceHistoryResponse.PeriodInfo(fromDate, toDate);
            
            AttendanceHistoryResponse.AttendanceSummary summaryInfo = 
                new AttendanceHistoryResponse.AttendanceSummary();
            summaryInfo.setTotalLateMinutes(summary.getTotalLateMinutes());
            summaryInfo.setTotalEarlyLeaveMinutes(summary.getTotalEarlyLeaveMinutes());
            summaryInfo.setTotalOvertimeMinutes(summary.getTotalOvertimeMinutes());
            summaryInfo.setTotalNightShiftMinutes(summary.getTotalNightShiftMinutes());
            summaryInfo.setPaidLeaveDays(summary.getPaidLeaveDays());
            summaryInfo.setAbsentDays(summary.getAbsentDays());
            
            AttendanceHistoryResponse response = new AttendanceHistoryResponse();
            response.setEmployee(employeeInfo);
            response.setPeriod(periodInfo);
            response.setAttendanceList(records);
            response.setSummary(summaryInfo);
            
            return ResponseEntity.ok(ApiResponse.success(response));
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("VALIDATION_ERROR", "日付形式が正しくありません"));
        }
    }
    
    /**
     * 月末申請
     */
    @PostMapping("/monthly-submit")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitMonthly(
            @Valid @RequestBody MonthlySubmitRequest request,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        // 権限チェック
        if (!sessionInfo.getEmployeeId().equals(request.getEmployeeId()) && 
            !"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "アクセス権限がありません"));
        }
        
        try {
            YearMonth targetMonth = DateUtil.parseYearMonth(request.getTargetMonth());
            
            AttendanceService.SubmissionResult result = 
                attendanceService.submitMonthlyAttendance(request.getEmployeeId(), targetMonth);
            
            if (result.isSuccess()) {
                Map<String, Object> data = Map.of(
                    "submissionMonth", request.getTargetMonth(),
                    "workingDaysCount", result.getWorkingDaysCount(),
                    "completedDaysCount", result.getCompletedDaysCount(),
                    "paidLeaveDaysCount", result.getPaidLeaveDaysCount()
                );
                
                return ResponseEntity.ok(ApiResponse.success(data, result.getMessage()));
            } else {
                Map<String, Object> errorDetails = null;
                if (result.getMissingDates() != null) {
                    errorDetails = Map.of("missingDates", result.getMissingDates());
                }
                
                ApiResponse<Map<String, Object>> errorResponse = 
                    ApiResponse.error(result.getErrorCode(), result.getMessage());
                if (errorDetails != null) {
                    errorResponse.setData(errorDetails);
                }
                
                return ResponseEntity.badRequest().body(errorResponse);
            }
            
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("VALIDATION_ERROR", "年月形式が正しくありません"));
        }
    }
    
    /**
     * 当日の勤怠状況取得
     */
    @GetMapping("/today-status")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTodayStatus(
            @RequestParam(required = false) Long employeeId,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        Long targetEmployeeId = employeeId != null ? employeeId : sessionInfo.getEmployeeId();
        
        // 権限チェック
        if (!sessionInfo.getEmployeeId().equals(targetEmployeeId) && 
            !"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "アクセス権限がありません"));
        }
        
        LocalDate today = DateUtil.todayInJapan();
        List<AttendanceRecord> records = attendanceService.getAttendanceHistory(targetEmployeeId, today, today);
        
        Map<String, Object> status = Map.of(
            "date", today,
            "hasClockIn", records.isEmpty() ? false : records.get(0).hasClockInTime(),
            "hasClockOut", records.isEmpty() ? false : records.get(0).hasClockOutTime(),
            "clockInTime", records.isEmpty() ? null : records.get(0).getClockInTime(),
            "clockOutTime", records.isEmpty() ? null : records.get(0).getClockOutTime()
        );
        
        return ResponseEntity.ok(ApiResponse.success(status));
    }
}