package com.kintai.controller;

import com.kintai.dto.AttendanceResponse;
import com.kintai.dto.LoginRequest;
import com.kintai.entity.Employee;
import com.kintai.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class AuthController {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);
    
    @Autowired
    private AuthService authService;
    
    @PostMapping("/login")
    public AttendanceResponse login(@Valid @RequestBody LoginRequest loginRequest, HttpSession session) {
        try {
            Employee employee = authService.authenticate(loginRequest.getEmployeeCode(), loginRequest.getPassword());
            
            // セッション設定
            session.setAttribute("employeeId", employee.getEmployeeId());
            session.setAttribute("employeeCode", employee.getEmployeeCode());
            session.setAttribute("employeeName", employee.getEmployeeName());
            session.setAttribute("role", employee.getEmployeeRole().name());
            session.setMaxInactiveInterval(600); // 10分
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", employee.getEmployeeId());
            data.put("employeeName", employee.getEmployeeName());
            data.put("role", employee.getEmployeeRole().name());
            data.put("sessionToken", session.getId());
            
            logger.info("Login successful: {} ({})", employee.getEmployeeName(), employee.getEmployeeCode());
            return AttendanceResponse.success(data, "ログインしました");
            
        } catch (Exception e) {
            logger.warn("Login failed: {}", e.getMessage());
            return AttendanceResponse.error("AUTH_FAILED", e.getMessage());
        }
    }
    
    @PostMapping("/logout")
    public AttendanceResponse logout(HttpSession session) {
        try {
            String employeeName = (String) session.getAttribute("employeeName");
            session.invalidate();
            logger.info("Logout successful: {}", employeeName);
            return AttendanceResponse.success(null, "ログアウトしました");
        } catch (Exception e) {
            logger.error("Logout error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", "ログアウト中にエラーが発生しました");
        }
    }
    
    @GetMapping("/session")
    public AttendanceResponse getSession(HttpSession session) {
        Long employeeId = (Long) session.getAttribute("employeeId");
        
        if (employeeId == null) {
            return AttendanceResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        Map<String, Object> data = new HashMap<>();
        data.put("employeeId", employeeId);
        data.put("employeeName", session.getAttribute("employeeName"));
        data.put("role", session.getAttribute("role"));
        data.put("remainingTime", session.getMaxInactiveInterval());
        
        return AttendanceResponse.success(data);
    }
}