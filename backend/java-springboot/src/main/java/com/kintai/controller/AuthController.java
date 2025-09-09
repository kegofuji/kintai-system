package com.kintai.controller;

import com.kintai.dto.ApiResponse;
import com.kintai.dto.LoginRequest;
import com.kintai.dto.LoginResponse;
import com.kintai.service.AuthService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    
    @Autowired
    private AuthService authService;
    
    /**
     * ログイン
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpSession session) {
        
        AuthService.AuthResult result = authService.authenticate(
            request.getEmployeeCode(), request.getPassword(), session);
        
        if (result.isSuccess()) {
            LoginResponse response = new LoginResponse(
                result.getEmployee().getEmployeeId(),
                result.getEmployee().getEmployeeName(),
                result.getEmployee().getEmployeeRole().getValue(),
                result.getSessionToken()
            );
            
            return ResponseEntity.ok(ApiResponse.success(response, "ログインしました"));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * ログアウト
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(HttpSession session) {
        authService.logout(session);
        return ResponseEntity.ok(ApiResponse.success(null, "ログアウトしました"));
    }
    
    /**
     * セッション確認
     */
    @GetMapping("/session")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getSession(HttpSession session) {
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        Map<String, Object> data = Map.of(
            "employeeId", sessionInfo.getEmployeeId(),
            "employeeName", sessionInfo.getEmployeeName(),
            "role", sessionInfo.getRole(),
            "remainingTime", sessionInfo.getRemainingTime()
        );
        
        return ResponseEntity.ok(ApiResponse.success(data));
    }
    
    /**
     * パスワード変更
     */
    @PostMapping("/change-password")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @RequestBody Map<String, String> request,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        String oldPassword = request.get("oldPassword");
        String newPassword = request.get("newPassword");
        
        try {
            boolean success = authService.changePassword(
                sessionInfo.getEmployeeId(), oldPassword, newPassword);
            
            if (success) {
                return ResponseEntity.ok(ApiResponse.success(null, "パスワードを変更しました"));
            } else {
                return ResponseEntity.badRequest().body(
                    ApiResponse.error("INVALID_PASSWORD", "現在のパスワードが正しくありません"));
            }
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("VALIDATION_ERROR", e.getMessage()));
        }
    }
}