package com.kintai.controller;

import com.kintai.dto.common.ApiResponse;
import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 管理者APIコントローラー
 * 社員管理・有給調整・レポート生成
 */
@RestController
@RequestMapping("/api/admin")
@RequiredArgsConstructor
@Slf4j
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    
    private final EmployeeService employeeService;
    
    /**
     * GET /api/admin/employees - 社員一覧取得
     */
    @GetMapping("/employees")
    public ResponseEntity<ApiResponse<Object>> getEmployees(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword) {
        
        try {
            List<Employee> employees = employeeService.getAllEmployees(status, keyword);
            
            Map<String, Object> data = new HashMap<>();
            data.put("employees", employees);
            
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/admin/employees - 社員追加
     */
    @PostMapping("/employees")
    public ResponseEntity<ApiResponse<Object>> addEmployee(@Valid @RequestBody Employee employee) {
        try {
            Employee result = employeeService.addEmployee(employee);
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", result.getEmployeeId());
            
            return ResponseEntity.ok(ApiResponse.success(data, "社員を追加しました"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * PUT /api/admin/employees/{employeeId} - 社員情報更新
     */
    @PutMapping("/employees/{employeeId}")
    public ResponseEntity<ApiResponse<Object>> updateEmployee(
            @PathVariable Long employeeId,
            @Valid @RequestBody Employee employee) {
        
        try {
            Employee result = employeeService.updateEmployee(employeeId, employee);
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", result.getEmployeeId());
            
            return ResponseEntity.ok(ApiResponse.success(data, "社員情報を更新しました"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/admin/employees/{employeeId}/retire - 退職処理
     */
    @PostMapping("/employees/{employeeId}/retire")
    public ResponseEntity<ApiResponse<Object>> retireEmployee(
            @PathVariable Long employeeId,
            @RequestBody Map<String, String> body) {
        
        LocalDate retiredAt = LocalDate.parse(body.get("retiredAt"));
        
        try {
            employeeService.retireEmployee(employeeId, retiredAt);
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", employeeId);
            data.put("retiredAt", retiredAt.toString());
            
            return ResponseEntity.ok(ApiResponse.success(data, "退職処理が完了しました"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/admin/paid-leave/adjust - 有給日数調整
     */
    @PostMapping("/paid-leave/adjust")
    public ResponseEntity<ApiResponse<Object>> adjustPaidLeave(@RequestBody Map<String, Object> body) {
        Long employeeId = Long.valueOf(body.get("employeeId").toString());
        Integer adjustmentDays = (Integer) body.get("adjustmentDays");
        String reason = (String) body.get("reason");
        
        try {
            // 調整前の残日数取得は別途実装が必要
            employeeService.adjustPaidLeave(employeeId, adjustmentDays, reason);
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", employeeId);
            data.put("adjustmentDays", adjustmentDays);
            // previousDays, newRemainingDays は別途取得
            
            return ResponseEntity.ok(ApiResponse.success(data, "有給日数を調整しました"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/reports/generate - PDF生成依頼（FastAPI連携）
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<ApiResponse<Object>> generateReport(@RequestBody Map<String, Object> body) {
        // FastAPI連携処理は別途実装
        // 設計書通りのレスポンス形式で返却
        
        Map<String, Object> data = new HashMap<>();
        data.put("reportUrl", "http://fastapi:8081/reports/download/report_example.pdf");
        data.put("expires", "2025-08-26T23:59:59");
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }
}
