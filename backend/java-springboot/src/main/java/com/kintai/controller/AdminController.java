package com.kintai.controller;

import com.kintai.dto.RequestDto;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.service.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
public class AdminController {
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * 社員一覧取得
     */
    @GetMapping("/employees")
    public ResponseEntity<Map<String, Object>> getEmployees(
            @RequestParam(required = false) String status,
            @RequestParam(required = false) String keyword,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }
            
            Employee.EmploymentStatus employmentStatus = null;
            if (status != null) {
                employmentStatus = Employee.EmploymentStatus.valueOf(status);
            }
            
            List<Employee> employees = employeeService.searchEmployees(keyword, employmentStatus);
            
            List<Map<String, Object>> employeeList = employees.stream().map(emp -> {
                Map<String, Object> item = new HashMap<>();
                item.put("employeeId", emp.getEmployeeId());
                item.put("employeeCode", emp.getEmployeeCode());
                item.put("employeeName", emp.getEmployeeName());
                item.put("email", emp.getEmail());
                item.put("role", emp.getEmployeeRole().name());
                item.put("employmentStatus", emp.getEmploymentStatus().name());
                item.put("hiredAt", emp.getHiredAt());
                item.put("paidLeaveRemainingDays", emp.getPaidLeaveRemainingDays());
                if (emp.getRetiredAt() != null) {
                    item.put("retiredAt", emp.getRetiredAt());
                }
                return item;
            }).collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("employees", employeeList);
            
            response.put("success", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 社員追加
     */
    @PostMapping("/employees")
    public ResponseEntity<Map<String, Object>> createEmployee(
            @Valid @RequestBody RequestDto.EmployeeRegistrationRequest request,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }
            
            Employee newEmployee = employeeService.createEmployee(
                    request.getEmployeeCode(),
                    request.getEmployeeName(),
                    request.getEmail(),
                    request.getPassword());
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", newEmployee.getEmployeeId());
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", "社員を追加しました");
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 社員情報更新
     */
    @PutMapping("/employees/{employeeId}")
    public ResponseEntity<Map<String, Object>> updateEmployee(
            @PathVariable Long employeeId,
            @Valid @RequestBody RequestDto.EmployeeRegistrationRequest request,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }
            
            Employee updatedEmployee = employeeService.updateEmployee(
                    employeeId, request.getEmployeeName(), request.getEmail());
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", updatedEmployee.getEmployeeId());
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", "社員情報を更新しました");
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 退職処理
     */
    @PostMapping("/employees/{employeeId}/retire")
    public ResponseEntity<Map<String, Object>> retireEmployee(
            @PathVariable Long employeeId,
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }
            
            LocalDate retiredAt = LocalDate.parse((String) requestBody.get("retiredAt"));
            
            Employee retiredEmployee = employeeService.retireEmployee(employeeId, retiredAt);
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", retiredEmployee.getEmployeeId());
            data.put("retiredAt", retiredEmployee.getRetiredAt());
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", "退職処理が完了しました");
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 有給日数調整
     */
    @PostMapping("/paid-leave/adjust")
    public ResponseEntity<Map<String, Object>> adjustPaidLeaveDays(
            @Valid @RequestBody RequestDto.PaidLeaveAdjustmentRequest request,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }
            
            Employee targetEmployee = employeeService.getEmployeeById(request.getEmployeeId());
            int previousDays = targetEmployee.getPaidLeaveRemainingDays();
            
            Employee updatedEmployee = employeeService.adjustPaidLeaveDays(
                    request.getEmployeeId(), request.getAdjustmentDays(), request.getReason());
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", updatedEmployee.getEmployeeId());
            data.put("previousDays", previousDays);
            data.put("adjustmentDays", request.getAdjustmentDays());
            data.put("newRemainingDays", updatedEmployee.getPaidLeaveRemainingDays());
            
            response.put("success", true);
            response.put("data", data);
            response.put("message", "有給日数を調整しました");
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * PDF生成依頼（FastAPI連携）
     */
    @PostMapping("/reports/generate")
    public ResponseEntity<Map<String, Object>> generateReport(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }
            
            Long employeeId = Long.valueOf(requestBody.get("employeeId").toString());
            String yearMonth = (String) requestBody.get("yearMonth");
            String reportType = (String) requestBody.get("reportType");
            
            // TODO: FastAPI連携実装
            // 現在は仮のレスポンスを返す
            Map<String, Object> data = new HashMap<>();
            data.put("reportUrl", "http://fastapi:8081/reports/download/report_" + 
                    java.time.LocalDateTime.now().format(java.time.format.DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss")) + ".pdf");
            data.put("expires", java.time.LocalDateTime.now().plusDays(1));
            
            response.put("success", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 月末申請承認
     */
    @PostMapping("/attendance/approve-monthly")
    public ResponseEntity<Map<String, Object>> approveMonthlyAttendance(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }
            
            Long employeeId = Long.valueOf(requestBody.get("employeeId").toString());
            String targetMonth = (String) requestBody.get("targetMonth");
            
            attendanceService.approveMonthlyAttendance(employeeId, targetMonth, currentEmployee.getEmployeeId());
            
            response.put("success", true);
            response.put("message", String.format("%s分の月末申請を承認しました", targetMonth));
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
    
    /**
     * 月末申請却下
     */
    @PostMapping("/attendance/reject-monthly")
    public ResponseEntity<Map<String, Object>> rejectMonthlyAttendance(
            @RequestBody Map<String, Object> requestBody,
            HttpSession session) {
        
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);
            
            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }
            
            Long employeeId = Long.valueOf(requestBody.get("employeeId").toString());
            String targetMonth = (String) requestBody.get("targetMonth");
            
            attendanceService.rejectMonthlyAttendance(employeeId, targetMonth, currentEmployee.getEmployeeId());
            
            response.put("success", true);
            response.put("message", String.format("%s分の月末申請を却下しました", targetMonth));
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}