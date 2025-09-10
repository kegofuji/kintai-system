package com.kintai.service;

import com.kintai.entity.Employee;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import jakarta.servlet.http.HttpSession;
import java.util.Optional;

@Service
public class AuthService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    
    
    /**
     * ログイン認証
     */
    public AuthResult authenticate(String employeeCode, String password, HttpSession session) {
        try {
            // 社員存在チェック
            Optional<Employee> employeeOpt = employeeRepository.findByEmployeeCode(employeeCode);
            if (employeeOpt.isEmpty()) {
                return AuthResult.failure("AUTH_FAILED", "社員IDまたはパスワードが正しくありません");
            }
            
            Employee employee = employeeOpt.get();
            
            // 在籍状況チェック
            if (!employee.isActive()) {
                return AuthResult.failure("ACCESS_DENIED", "アカウントが無効です");
            }
            
            // パスワード照合
            if (!passwordEncoder.matches(password, employee.getEmployeePasswordHash())) {
                return AuthResult.failure("AUTH_FAILED", "社員IDまたはパスワードが正しくありません");
            }
            
            // セッション作成
            session.setAttribute("employeeId", employee.getEmployeeId());
            session.setAttribute("employeeCode", employee.getEmployeeCode());
            session.setAttribute("employeeName", employee.getEmployeeName());
            session.setAttribute("role", employee.getEmployeeRole().getValue());
            session.setMaxInactiveInterval(600); // 10分タイムアウト
            
            return AuthResult.success(employee, session.getId());
            
        } catch (Exception e) {
            return AuthResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * セッション情報取得
     */
    public SessionInfo getSessionInfo(HttpSession session) {
        Long employeeId = (Long) session.getAttribute("employeeId");
        String employeeName = (String) session.getAttribute("employeeName");
        String role = (String) session.getAttribute("role");
        
        if (employeeId == null) {
            return null;
        }
        
        int remainingTime = session.getMaxInactiveInterval();
        return new SessionInfo(employeeId, employeeName, role, remainingTime);
    }
    
    /**
     * ログアウト
     */
    public void logout(HttpSession session) {
        session.invalidate();
    }
    
    /**
     * パスワード変更
     */
    public boolean changePassword(Long employeeId, String oldPassword, String newPassword) {
        Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
        if (employeeOpt.isEmpty()) {
            return false;
        }
        
        Employee employee = employeeOpt.get();
        
        // 現在のパスワード確認
        if (!passwordEncoder.matches(oldPassword, employee.getEmployeePasswordHash())) {
            return false;
        }
        
        // 新しいパスワードの検証
        if (!ValidationUtil.isValidPassword(newPassword, employee.getEmployeeCode())) {
            throw new IllegalArgumentException("パスワードの形式が正しくありません");
        }
        
        // パスワード更新
        employee.setEmployeePasswordHash(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
        
        return true;
    }
    
    /**
     * 認証結果クラス
     */
    public static class AuthResult {
        private final boolean success;
        private final Employee employee;
        private final String sessionToken;
        private final String errorCode;
        private final String message;
        
        private AuthResult(boolean success, Employee employee, String sessionToken, 
                          String errorCode, String message) {
            this.success = success;
            this.employee = employee;
            this.sessionToken = sessionToken;
            this.errorCode = errorCode;
            this.message = message;
        }
        
        public static AuthResult success(Employee employee, String sessionToken) {
            return new AuthResult(true, employee, sessionToken, null, null);
        }
        
        public static AuthResult failure(String errorCode, String message) {
            return new AuthResult(false, null, null, errorCode, message);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public Employee getEmployee() { return employee; }
        public String getSessionToken() { return sessionToken; }
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
    }
    
    /**
     * セッション情報クラス
     */
    public static class SessionInfo {
        private final Long employeeId;
        private final String employeeName;
        private final String role;
        private final int remainingTime;
        
        public SessionInfo(Long employeeId, String employeeName, String role, int remainingTime) {
            this.employeeId = employeeId;
            this.employeeName = employeeName;
            this.role = role;
            this.remainingTime = remainingTime;
        }
        
        // Getters
        public Long getEmployeeId() { return employeeId; }
        public String getEmployeeName() { return employeeName; }
        public String getRole() { return role; }
        public int getRemainingTime() { return remainingTime; }
    }
}