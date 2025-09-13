package com.kintai.service;

import com.kintai.dto.LoginRequest;
import com.kintai.dto.LoginResponse;
import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

/**
 * 認証・セッション管理サービス
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {
    
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
    /**
     * ログイン処理
     */
    public LoginResponse login(LoginRequest request) {
        // 社員存在チェック
        Employee employee = employeeRepository
                .findByEmployeeCodeAndEmploymentStatus(request.getEmployeeCode(), "active")
                .orElseThrow(() -> new BusinessException("AUTH_FAILED", "認証に失敗しました"));
        
        // パスワード検証
        if (!passwordEncoder.matches(request.getPassword(), employee.getEmployeePasswordHash())) {
            throw new BusinessException("AUTH_FAILED", "認証に失敗しました");
        }
        
        // JWTトークン生成（10分有効）
        String token = jwtUtil.generateToken(employee);
        
        return LoginResponse.builder()
                .success(true)
                .employeeId(employee.getEmployeeId())
                .employeeName(employee.getEmployeeName())
                .role(employee.getEmployeeRole().getValue())
                .sessionToken(token)
                .build();
    }
    
    /**
     * セッション検証
     */
    public Employee validateSession(String token) {
        if (!jwtUtil.validateToken(token)) {
            throw new BusinessException("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        Employee employee = jwtUtil.getEmployeeFromToken(token);
        
        // 退職者チェック
        if ("retired".equals(employee.getEmploymentStatus().getValue())) {
            throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
        }
        
        return employee;
    }
    
    /**
     * パスワードハッシュ化
     */
    public String hashPassword(String password) {
        return passwordEncoder.encode(password);
    }
}