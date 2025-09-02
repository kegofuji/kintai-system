package com.kintai.service;

import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class AuthService {
    
    private static final Logger logger = LoggerFactory.getLogger(AuthService.class);
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ValidationUtil validationUtil;
    
    /**
     * ログイン認証
     * @param employeeCode 社員コード
     * @param password パスワード
     * @return 認証成功時はEmployee、失敗時はnull
     */
    public Employee authenticate(String employeeCode, String password) {
        try {
            // 基本バリデーション
            validationUtil.validateRequired(employeeCode, "社員コード");
            validationUtil.validateRequired(password, "パスワード");
            
            Optional<Employee> employeeOpt = employeeRepository.findByEmployeeCode(employeeCode);
            
            if (employeeOpt.isEmpty()) {
                logger.warn("Authentication failed: Employee not found. Code: {}", employeeCode);
                throw new BusinessException("AUTH_FAILED", "社員IDまたはパスワードが正しくありません");
            }
            
            Employee employee = employeeOpt.get();
            
            // 退職者チェック
            if (employee.getEmploymentStatus() == Employee.EmploymentStatus.retired) {
                logger.warn("Authentication failed: Employee retired. Code: {}", employeeCode);
                throw new BusinessException("AUTH_FAILED", "社員IDまたはパスワードが正しくありません");
            }
            
            // パスワード照合
            if (!passwordEncoder.matches(password, employee.getEmployeePasswordHash())) {
                logger.warn("Authentication failed: Password mismatch. Code: {}", employeeCode);
                throw new BusinessException("AUTH_FAILED", "社員IDまたはパスワードが正しくありません");
            }
            
            logger.info("Authentication successful. Employee: {} ({})", employee.getEmployeeName(), employeeCode);
            return employee;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Authentication error for employee: " + employeeCode, e);
            throw new BusinessException("SYSTEM_ERROR", "認証処理中にエラーが発生しました");
        }
    }
    
    /**
     * パスワードバリデーション（新規作成・変更時）
     * @param password パスワード
     * @param employeeCode 社員コード
     */
    public void validatePassword(String password, String employeeCode) {
        validationUtil.validatePassword(password, employeeCode);
    }
    
    /**
     * パスワードハッシュ生成
     * @param password 平文パスワード
     * @return ハッシュ化パスワード
     */
    public String encodePassword(String password) {
        return passwordEncoder.encode(password);
    }
}