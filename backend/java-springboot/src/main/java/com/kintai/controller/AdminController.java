package com.kintai.controller;

import com.kintai.dto.*;
import com.kintai.entity.Employee;
import com.kintai.service.AuthService;
import com.kintai.service.EmployeeService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * 管理者権限チェック
     */
    private ResponseEntity<ApiResponse<?>> checkAdminPermission(HttpSession session) {
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        if (!"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "管理者権限が必要です"));
        }
        
        return null; // 権限OK
    }
    
    /**
     * 社員一覧取得
     */
    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<?>> getEmployees(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        
        Employee.EmploymentStatus employmentStatus = null;
        if (status != null) {
            try {
                employmentStatus = Employee.EmploymentStatus.valueOf(status.toUpperCase());
            } catch (IllegalArgumentException e) {
                // 無効なステータスは無視
            }
        }
        
        List<Employee> employees;
        if (keyword != null && !keyword.trim().isEmpty()) {
            employees = employeeService.searchEmployees(keyword, employmentStatus);
        } else {
            employees = employeeService.getAllEmployees(employmentStatus);
        }
        
        Map<String, Object> data = Map.of("employees", employees);
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    /**
     * 社員追加
     */
    @PostMapping("/employees")
    public ResponseEntity<ApiResponse<?>> addEmployee(
            @Valid @RequestBody EmployeeCreateDto request,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        
        EmployeeService.EmployeeOperationResult result = employeeService.addEmployee(
            request.getEmployeeCode(),
            request.getEmployeeName(),
            request.getEmail(),
            request.getPassword(),
            request.getRole(),
            request.getHiredAt()
        );
        
        if (result.isSuccess()) {
            Map<String, Object> data = Map.of("employeeId", result.getEmployee().getEmployeeId());
            return ResponseEntity.ok(ApiResponse.success(data, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * 社員情報更新
     */
    @PutMapping("/employees/{employeeId}")
    public ResponseEntity<ApiResponse<?>> updateEmployee(
            @PathVariable Long employeeId,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        
        EmployeeService.EmployeeOperationResult result = employeeService.updateEmployee(
            employeeId,
            request.get("employeeName"),
            request.get("email"),
            request.get("role")
        );
        
        if (result.isSuccess()) {
            Map<String, Object> data = Map.of("employeeId", employeeId);
            return ResponseEntity.ok(ApiResponse.success(data, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * 退職処理
     */
    @PostMapping("/employees/{employeeId}/retire")
    public ResponseEntity<ApiResponse<?>> retireEmployee(
            @PathVariable Long employeeId,
            @Valid @RequestBody RetireRequestDto request,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        
        EmployeeService.EmployeeOperationResult result = 
            employeeService.retireEmployee(employeeId, request.getRetiredAt());
        
        if (result.isSuccess()) {
            Map<String, Object> data = Map.of(
                "employeeId", employeeId,
                "retiredAt", request.getRetiredAt()
            );
            return ResponseEntity.ok(ApiResponse.success(data, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * 有給日数調整
     */
    @PostMapping("/paid-leave/adjust")
    public ResponseEntity<ApiResponse<?>> adjustPaidLeave(
            @Valid @RequestBody PaidLeaveAdjustmentDto request,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        
        EmployeeService.PaidLeaveAdjustmentResult result = employeeService.adjustPaidLeave(
            request.getEmployeeId(),
            request.getAdjustmentDays(),
            request.getReason()
        );
        
        if (result.isSuccess()) {
            Map<String, Object> data = Map.of(
                "employeeId", result.getEmployeeId(),
                "previousDays", result.getPreviousDays(),
                "adjustmentDays", result.getAdjustmentDays(),
                "newRemainingDays", result.getNewRemainingDays()
            );
            return ResponseEntity.ok(ApiResponse.success(data, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * パスワードリセット
     */
    @PostMapping("/employees/{employeeId}/reset-password")
    public ResponseEntity<ApiResponse<?>> resetPassword(
            @PathVariable Long employeeId,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        
        String newPassword = request.get("newPassword");
        if (newPassword == null || newPassword.trim().isEmpty()) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("VALIDATION_ERROR", "新しいパスワードは必須です"));
        }
        
        EmployeeService.EmployeeOperationResult result = 
            employeeService.resetPassword(employeeId, newPassword);
        
        if (result.isSuccess()) {
            return ResponseEntity.ok(ApiResponse.success(null, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * PDF レポート生成依頼（FastAPI連携用）
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<ApiResponse<?>> generateReport(
            @Valid @RequestBody ReportGenerateDto request,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        
        // 現在は仮の実装
        Map<String, Object> data = Map.of(
            "reportUrl", "http://fastapi:8081/reports/download/report_" + 
                        request.getYearMonth().replace("-", "") + "_" + 
                        System.currentTimeMillis() + ".pdf",
            "expires", java.time.LocalDateTime.now().plusHours(24)
        );
        
        return ResponseEntity.ok(ApiResponse.success(data, "レポート生成要求を送信しました"));
    }
    
    /**
     * 勤怠整合性チェック
     */
    @GetMapping("/attendance/integrity-check")
    public ResponseEntity<ApiResponse<?>> checkAttendanceIntegrity(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String yearMonth,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        
 
        // 現在は仮の実装
        Map<String, Object> result = Map.of(
            "checkedPeriod", yearMonth != null ? yearMonth : "2025-08",
            "totalRecords", 100,
            "irregularRecords", 5,
            "message", "整合性チェックが完了しました"
        );
        
        return ResponseEntity.ok(ApiResponse.success(result));
    }
    
    /**
     * 勤怠データ管理（管理者用勤怠データ検索・編集）
     */
    @GetMapping("/attendance")
    public ResponseEntity<ApiResponse<?>> searchAttendanceRecords(
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String employeeName,
            @RequestParam(required = false) String fromDate,
            @RequestParam(required = false) String toDate,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        

        // 現在は仮の実装
        return ResponseEntity.ok(ApiResponse.success(List.of()));
    }
    
    /**
     * 月末申請承認処理
     */
    @PostMapping("/attendance/approve-monthly/{employeeId}")
    public ResponseEntity<ApiResponse<?>> approveMonthlySubmission(
            @PathVariable Long employeeId,
            @RequestParam String yearMonth,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        

        return ResponseEntity.ok(ApiResponse.success(null, "月末申請を承認しました"));
    }
    
    /**
     * 月末申請却下処理
     */
    @PostMapping("/attendance/reject-monthly/{employeeId}")
    public ResponseEntity<ApiResponse<?>> rejectMonthlySubmission(
            @PathVariable Long employeeId,
            @RequestParam String yearMonth,
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        ResponseEntity<ApiResponse<?>> permissionCheck = checkAdminPermission(session);
        if (permissionCheck != null) return permissionCheck;
        

        return ResponseEntity.ok(ApiResponse.success(null, "月末申請を却下しました"));
    }
}