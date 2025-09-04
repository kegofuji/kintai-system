package com.kintai.controller;

import com.kintai.dto.LoginRequest;
import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * ログイン
     * POST /api/auth/login
     * 
     * リクエスト:
     * {
     *   "employeeCode": "E001",
     *   "password": "password123"
     * }
     * 
     * レスポンス（成功）:
     * {
     *   "success": true,
     *   "data": {
     *     "employeeId": 1,
     *     "employeeName": "山田太郎",
     *     "role": "employee",
     *     "sessionToken": "ABC123..."
     *   }
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<Map<String, Object>> login(@Valid @RequestBody LoginRequest loginRequest,
                                                    HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee employee = authService.authenticate(
                    loginRequest.getEmployeeCode(), 
                    loginRequest.getPassword(), 
                    session);
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", employee.getEmployeeId());
            data.put("employeeName", employee.getEmployeeName());
            data.put("role", employee.getEmployeeRole().name());
            data.put("sessionToken", session.getId());
            
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
     * ログアウト
     * POST /api/auth/logout
     * 
     * レスポンス:
     * {
     *   "success": true,
     *   "data": {},
     *   "message": "ログアウトしました"
     * }
     */
    @PostMapping("/logout")
    public ResponseEntity<Map<String, Object>> logout(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        authService.logout(session);
        
        response.put("success", true);
        response.put("data", new HashMap<>());
        response.put("message", "ログアウトしました");
        
        return ResponseEntity.ok(response);
    }
    
    /**
     * セッション確認
     * GET /api/auth/session
     * 
     * レスポンス（成功）:
     * {
     *   "success": true,
     *   "data": {
     *     "employeeId": 1,
     *     "employeeName": "山田太郎",
     *     "role": "employee",
     *     "remainingTime": 480
     *   }
     * }
     * 
     * レスポンス（セッション切れ）:
     * {
     *   "success": false,
     *   "errorCode": "SESSION_TIMEOUT",
     *   "message": "セッションがタイムアウトしました"
     * }
     */
    @GetMapping("/session")
    public ResponseEntity<Map<String, Object>> checkSession(HttpSession session) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            Employee employee = authService.getCurrentEmployee(session);
            
            Map<String, Object> data = new HashMap<>();
            data.put("employeeId", employee.getEmployeeId());
            data.put("employeeName", employee.getEmployeeName());
            data.put("role", employee.getEmployeeRole().name());
            data.put("remainingTime", session.getMaxInactiveInterval());
            
            response.put("success", true);
            response.put("data", data);
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            
            return ResponseEntity.status(401).body(response);
        }
    }
    
    /**
     * 初回パスワード設定
     * POST /api/auth/set-password
     * 
     * リクエスト:
     * {
     *   "employeeCode": "E001",
     *   "newPassword": "NewSecure123!"
     * }
     */
    @PostMapping("/set-password")
    public ResponseEntity<Map<String, Object>> setInitialPassword(
            @RequestBody Map<String, String> requestBody) {
        Map<String, Object> response = new HashMap<>();
        
        try {
            String employeeCode = requestBody.get("employeeCode");
            String newPassword = requestBody.get("newPassword");
            
            authService.setInitialPassword(employeeCode, newPassword);
            
            response.put("success", true);
            response.put("message", "パスワードが設定されました");
            
            return ResponseEntity.ok(response);
            
        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}