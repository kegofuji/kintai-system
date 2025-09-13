package com.kintai.controller;

import com.kintai.dto.LoginRequest;
import com.kintai.dto.LoginResponse;
import com.kintai.dto.common.ApiResponse;
import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

/**
 * 認証APIコントローラー
 * ログイン・ログアウト・セッション管理
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Slf4j
public class AuthController {
    
    private final AuthService authService;
    
    /**
     * POST /api/auth/login - ログイン認証
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        try {
            LoginResponse response = authService.login(request);
            return ResponseEntity.ok(ApiResponse.success(response));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/auth/logout - ログアウト処理
     */
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Object>> logout(HttpServletRequest request) {
        // セッション無効化処理
        return ResponseEntity.ok(ApiResponse.success(null, "ログアウトしました"));
    }
    
    /**
     * GET /api/auth/session - セッション確認
     */
    @GetMapping("/session")
    public ResponseEntity<ApiResponse<Object>> checkSession(HttpServletRequest request) {
        String token = extractToken(request);
        if (token == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました")
            );
        }
        
        try {
            Employee employee = authService.validateSession(token);
            Map<String, Object> sessionInfo = new HashMap<>();
            sessionInfo.put("employeeId", employee.getEmployeeId());
            sessionInfo.put("employeeName", employee.getEmployeeName());
            sessionInfo.put("role", employee.getEmployeeRole().getValue());
            sessionInfo.put("remainingTime", 480); // 10分=600秒、実際はJWTから取得
            
            return ResponseEntity.ok(ApiResponse.success(sessionInfo));
        } catch (BusinessException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * リクエストヘッダーからトークンを抽出
     * @param request HTTPリクエスト
     * @return トークン文字列
     */
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
