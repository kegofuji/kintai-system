package com.kintai.service;

import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
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
    public Employee authenticate(String employeeCode, String password, HttpSession session) {
        // 社員コード存在チェック
        Optional<Employee> employeeOpt = employeeRepository.findByEmployeeCode(employeeCode);
        if (employeeOpt.isEmpty()) {
            throw new BusinessException("AUTH_FAILED", "社員IDまたはパスワードが正しくありません");
        }
        
        Employee employee = employeeOpt.get();
        
        // 退職者チェック
        if (employee.getEmploymentStatus() == Employee.EmploymentStatus.retired) {
            throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
        }
        
        // パスワード照合
        if (!passwordEncoder.matches(password, employee.getEmployeePasswordHash())) {
            throw new BusinessException("AUTH_FAILED", "社員IDまたはパスワードが正しくありません");
        }
        
        // セッション生成
        session.setAttribute("employeeId", employee.getEmployeeId());
        session.setAttribute("employeeCode", employee.getEmployeeCode());
        session.setAttribute("employeeName", employee.getEmployeeName());
        session.setAttribute("employeeRole", employee.getEmployeeRole().name());
        session.setMaxInactiveInterval(600); // 10分
        
        return employee;
    }
    
    /**
     * ログアウト
     */
    public void logout(HttpSession session) {
        session.invalidate();
    }
    
    /**
     * セッション情報取得
     */
    public Employee getCurrentEmployee(HttpSession session) {
        Long employeeId = (Long) session.getAttribute("employeeId");
        if (employeeId == null) {
            throw new BusinessException("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
    }
    
    /**
     * 管理者権限チェック
     */
    public boolean isAdmin(HttpSession session) {
        String role = (String) session.getAttribute("employeeRole");
        return "admin".equals(role);
    }
    
    /**
     * 新規パスワード設定（初回ログイン時）
     */
    public void setInitialPassword(String employeeCode, String newPassword) {
        if (!ValidationUtil.isValidPassword(newPassword, employeeCode)) {
            String errorMessage = ValidationUtil.getPasswordValidationMessage(newPassword, employeeCode);
            throw new BusinessException("VALIDATION_ERROR", errorMessage);
        }
        
        Employee employee = employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
        
        employee.setEmployeePasswordHash(passwordEncoder.encode(newPassword));
        employeeRepository.save(employee);
    }
}