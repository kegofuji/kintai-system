package com.kintai.controller;

import com.kintai.dto.AttendanceResponse;
import com.kintai.dto.RequestDto;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.service.AttendanceService;
import com.kintai.service.EmployeeService;
import com.kintai.service.RequestService;
import com.kintai.util.DateUtil;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/admin")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class AdminController {
    
    private static final Logger logger = LoggerFactory.getLogger(AdminController.class);
    
    @Autowired
    private EmployeeService employeeService;
    
    @Autowired
    private AttendanceService attendanceService;
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private DateUtil dateUtil;
    
    @Value("${kintai.fastapi.url:http://localhost:8081}")
    private String fastApiUrl;
    
    private final WebClient webClient = WebClient.create();
    
    // 社員管理
    @GetMapping("/employees")
    public AttendanceResponse getEmployees(@RequestParam(required = false) String status,
                                         @RequestParam(required = false) String keyword,
                                         HttpSession session) {
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            Employee.EmploymentStatus employmentStatus = null;
            if (status != null && !status.isEmpty()) {
                try {
                    employmentStatus = Employee.EmploymentStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                    // 無効なステータスは無視
                }
            }
            
            List<Employee> employees = employeeService.getEmployees(employmentStatus, keyword);
            
            List<Map<String, Object>> employeeList = employees.stream().map(employee -> {
                Map<String, Object> emp = new HashMap<>();
                emp.put("employeeId", employee.getEmployeeId());
                emp.put("employeeCode", employee.getEmployeeCode());
                emp.put("employeeName", employee.getEmployeeName());
                emp.put("email", employee.getEmail());
                emp.put("role", employee.getEmployeeRole().name());
                emp.put("employmentStatus", employee.getEmploymentStatus().name());
                emp.put("hiredAt", employee.getHiredAt());
                emp.put("retiredAt", employee.getRetiredAt());
                emp.put("paidLeaveRemainingDays", employee.getPaidLeaveRemainingDays());
                return emp;
            }).collect(Collectors.toList());
            
            Map<String, Object> data = new HashMap<>();
            data.put("employees", employeeList);
            
            return AttendanceResponse.success(data);
            
        } catch (Exception e) {
            logger.error("Get employees error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @PostMapping("/employees")
    public AttendanceResponse createEmployee(@Valid @RequestBody RequestDto.EmployeeCreateDto createDto,
                                           HttpSession session) {
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            Long employeeId = employeeService.createEmployee(
                createDto.getEmployeeCode(),
                createDto.getEmployeeName(),
                createDto.getEmail(),
                createDto.getPassword(),
                createDto.getRole()
            );
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", employeeId);
            
            return AttendanceResponse.success(data, "社員を追加しました");
            
        } catch (Exception e) {
            logger.error("Create employee error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @PutMapping("/employees/{employeeId}")
    public AttendanceResponse updateEmployee(@PathVariable Long employeeId,
                                           @RequestBody Map<String, String> updateData,
                                           HttpSession session) {
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            employeeService.updateEmployee(
                employeeId,
                updateData.get("employeeName"),
                updateData.get("email"),
                updateData.get("role")
            );
            
            return AttendanceResponse.success(null, "社員情報を更新しました");
            
        } catch (Exception e) {
            logger.error("Update employee error for ID: " + employeeId, e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @PostMapping("/employees/{employeeId}/retire")
    public AttendanceResponse retireEmployee(@PathVariable Long employeeId,
                                           @RequestBody Map<String, String> retireData,
                                           HttpSession session) {
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            LocalDate retiredAt = null;
            if (retireData.get("retiredAt") != null) {
                retiredAt = LocalDate.parse(retireData.get("retiredAt"));
            }
            
            employeeService.retireEmployee(employeeId, retiredAt);
            
            return AttendanceResponse.success(null, "退職処理が完了しました");
            
        } catch (Exception e) {
            logger.error("Retire employee error for ID: " + employeeId, e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    // 有給管理
    @PostMapping("/paid-leave/adjust")
    public AttendanceResponse adjustPaidLeave(@Valid @RequestBody RequestDto.PaidLeaveAdjustmentDto adjustmentDto,
                                             HttpSession session) {
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            Employee employee = employeeService.getEmployee(adjustmentDto.getEmployeeId());
            int previousDays = employee.getPaidLeaveRemainingDays();
            
            employeeService.adjustPaidLeaveRemainingDays(
                adjustmentDto.getEmployeeId(),
                adjustmentDto.getAdjustmentDays(),
                adjustmentDto.getReason()
            );
            
            int newDays = previousDays + adjustmentDto.getAdjustmentDays();
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", adjustmentDto.getEmployeeId());
            data.put("previousDays", previousDays);
            data.put("adjustmentDays", adjustmentDto.getAdjustmentDays());
            data.put("newRemainingDays", newDays);
            
            return AttendanceResponse.success(data, "有給日数を調整しました");
            
        } catch (Exception e) {
            logger.error("Adjust paid leave error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    // 勤怠管理
    @GetMapping("/attendance/search")
    public AttendanceResponse searchAttendance(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                             @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                             @RequestParam(required = false) String employeeCode,
                                             @RequestParam(required = false) String employeeName,
                                             HttpSession session) {
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            List<AttendanceRecord> records = attendanceService.searchAttendanceRecords(
                startDate, endDate, employeeCode, employeeName);
            
            List<Map<String, Object>> attendanceList = records.stream().map(record -> {
                Map<String, Object> item = new HashMap<>();
                item.put("attendanceId", record.getAttendanceId());
                item.put("employeeId", record.getEmployeeId());
                item.put("attendanceDate", dateUtil.formatDate(record.getAttendanceDate()));
                item.put("clockInTime", record.getClockInTime() != null ? 
                    record.getClockInTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null);
                item.put("clockOutTime", record.getClockOutTime() != null ? 
                    record.getClockOutTime().format(DateTimeFormatter.ofPattern("HH:mm")) : null);
                item.put("lateMinutes", record.getLateMinutes());
                item.put("earlyLeaveMinutes", record.getEarlyLeaveMinutes());
                item.put("overtimeMinutes", record.getOvertimeMinutes());
                item.put("nightShiftMinutes", record.getNightShiftMinutes());
                item.put("attendanceStatus", record.getAttendanceStatus().name());
                item.put("submissionStatus", record.getSubmissionStatus());
                item.put("attendanceFixedFlag", record.getAttendanceFixedFlag());
                return item;
            }).collect(Collectors.toList());
            
            return AttendanceResponse.success(attendanceList);
            
        } catch (Exception e) {
            logger.error("Search attendance error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    // 月末申請承認
    @PostMapping("/attendance/approve-monthly")
    public AttendanceResponse approveMonthlyAttendance(@RequestBody Map<String, Object> requestData,
                                                      HttpSession session) {
        Long approverId = (Long) session.getAttribute("employeeId");
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            Long employeeId = Long.valueOf(requestData.get("employeeId").toString());
            String targetMonth = (String) requestData.get("targetMonth");
            
            attendanceService.approveMonthlyAttendance(employeeId, targetMonth, approverId);
            
            return AttendanceResponse.success(null, "月末申請を承認しました");
            
        } catch (Exception e) {
            logger.error("Approve monthly attendance error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @PostMapping("/attendance/reject-monthly")
    public AttendanceResponse rejectMonthlyAttendance(@RequestBody Map<String, Object> requestData,
                                                     HttpSession session) {
        Long approverId = (Long) session.getAttribute("employeeId");
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            Long employeeId = Long.valueOf(requestData.get("employeeId").toString());
            String targetMonth = (String) requestData.get("targetMonth");
            
            attendanceService.rejectMonthlyAttendance(employeeId, targetMonth, approverId);
            
            return AttendanceResponse.success(null, "月末申請を却下しました");
            
        } catch (Exception e) {
            logger.error("Reject monthly attendance error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    // PDF レポート生成
    @PostMapping("/reports/generate")
    public AttendanceResponse generateReport(@RequestBody Map<String, Object> reportRequest,
                                           HttpSession session) {
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            // FastAPI に PDF生成要求
            Map<String, Object> request = new HashMap<>();
            request.put("employee_id", reportRequest.get("employeeId"));
            request.put("year_month", reportRequest.get("yearMonth"));
            request.put("report_type", "monthly");
            
            Map<String, Object> response = webClient.post()
                .uri(fastApiUrl + "/reports/pdf")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .block();
            
            if (response != null && Boolean.TRUE.equals(response.get("success"))) {
                return AttendanceResponse.success(response.get("data"), "レポートを生成しました");
            } else {
                return AttendanceResponse.error("REPORT_GENERATION_FAILED", "レポート生成に失敗しました");
            }
            
        } catch (Exception e) {
            logger.error("Generate report error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", "レポート生成中にエラーが発生しました");
        }
    }
    
    // システム統計
    @GetMapping("/stats")
    public AttendanceResponse getSystemStats(HttpSession session) {
        if (!isAdmin(session)) {
            return AttendanceResponse.error("ACCESS_DENIED", "管理者権限が必要です");
        }
        
        try {
            Map<String, Object> stats = new HashMap<>();
            stats.put("activeEmployeeCount", employeeService.getActiveEmployeeCount());
            stats.put("currentMonth", dateUtil.getCurrentYearMonth());
            stats.put("systemVersion", "1.0.0");
            
            return AttendanceResponse.success(stats);
            
        } catch (Exception e) {
            logger.error("Get system stats error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    private boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("role");
        return "admin".equals(role);
    }
}