package com.kintai.controller;

import com.kintai.dto.AttendanceHistoryRequest;
import com.kintai.dto.AttendanceHistoryResponse;
import com.kintai.dto.ClockRequest;
import com.kintai.dto.ClockResponse;
import com.kintai.dto.MonthlySubmitRequest;
import com.kintai.dto.common.ApiResponse;
import com.kintai.exception.BusinessException;
import com.kintai.service.AttendanceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

/**
 * 勤怠APIコントローラー
 * 出退勤打刻・勤怠履歴・月末申請
 */
@RestController
@RequestMapping("/api/attendance")
@RequiredArgsConstructor
@Slf4j
public class AttendanceController {
    
    private final AttendanceService attendanceService;
    
    /**
     * POST /api/attendance/clock-in - 出勤打刻
     */
    @PostMapping("/clock-in")
    public ResponseEntity<ApiResponse<ClockResponse>> clockIn(@Valid @RequestBody ClockRequest request) {
        try {
            ClockResponse response = attendanceService.clockIn(request.getEmployeeId());
            return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/attendance/clock-out - 退勤打刻
     */
    @PostMapping("/clock-out")
    public ResponseEntity<ApiResponse<ClockResponse>> clockOut(@Valid @RequestBody ClockRequest request) {
        try {
            ClockResponse response = attendanceService.clockOut(request.getEmployeeId());
            return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * GET /api/attendance/history - 勤怠履歴取得
     */
    @GetMapping("/history")
    public ResponseEntity<ApiResponse<AttendanceHistoryResponse>> getHistory(
            @RequestParam Long employeeId,
            @RequestParam(required = false) String yearMonth,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateFrom,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate dateTo) {
        
        AttendanceHistoryRequest request = new AttendanceHistoryRequest();
        request.setEmployeeId(employeeId);
        request.setYearMonth(yearMonth);
        request.setDateFrom(dateFrom);
        request.setDateTo(dateTo);
        
        try {
            AttendanceHistoryResponse response = attendanceService.getAttendanceHistory(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/attendance/monthly-submit - 月末勤怠申請
     */
    @PostMapping("/monthly-submit")
    public ResponseEntity<ApiResponse<Object>> submitMonthly(@Valid @RequestBody MonthlySubmitRequest request) {
        try {
            attendanceService.submitMonthlyAttendance(request.getEmployeeId(), request.getTargetMonth());
            
            Map<String, Object> result = new HashMap<>();
            result.put("submissionMonth", request.getTargetMonth());
            result.put("message", request.getTargetMonth() + "分の勤怠申請が完了しました");
            
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (BusinessException e) {
            if ("INCOMPLETE_ATTENDANCE".equals(e.getErrorCode())) {
                return ResponseEntity.badRequest().body(
                        ApiResponse.error(e.getErrorCode(), e.getMessage(), null) // detailsは別途設定
                );
            }
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
}
