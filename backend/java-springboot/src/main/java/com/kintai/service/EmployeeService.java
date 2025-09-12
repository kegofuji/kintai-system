package com.kintai.service;

import com.kintai.entity.Employee;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.ValidationUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EmployeeService {
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    
    
    /**
     * 社員一覧取得
     */
    @Transactional(readOnly = true)
    public List<Employee> getAllEmployees(Employee.EmploymentStatus status) {
        if (status == null) {
            return employeeRepository.findAll();
        }
        return employeeRepository.findByEmploymentStatus(status);
    }
    
    /**
     * 在籍社員一覧取得
     */
    @Transactional(readOnly = true)
    public List<Employee> getActiveEmployees() {
        return employeeRepository.findActiveEmployees();
    }
    
    /**
     * 社員検索
     */
    @Transactional(readOnly = true)
    public List<Employee> searchEmployees(String keyword, Employee.EmploymentStatus status) {
        return employeeRepository.searchEmployees(keyword, status);
    }
    
    /**
     * 社員取得
     */
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId);
    }
    
    @Transactional(readOnly = true)
    public Optional<Employee> getEmployeeByCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode);
    }
    
    /**
     * 社員追加
     */
    public EmployeeOperationResult addEmployee(String employeeCode, String employeeName, String email,
                                             String password, String role, LocalDate hiredAt) {
        try {
            // バリデーション
            if (!ValidationUtil.isValidEmployeeCode(employeeCode)) {
                return EmployeeOperationResult.failure("VALIDATION_ERROR", "社員コードの形式が正しくありません");
            }
            
            if (!ValidationUtil.isValidEmail(email)) {
                return EmployeeOperationResult.failure("VALIDATION_ERROR", "メールアドレスの形式が正しくありません");
            }
            
            if (!ValidationUtil.isValidPassword(password, employeeCode)) {
                return EmployeeOperationResult.failure("VALIDATION_ERROR", "パスワードの形式が正しくありません");
            }
            
            // 重複チェック
            if (employeeRepository.existsByEmployeeCode(employeeCode)) {
                return EmployeeOperationResult.failure("DUPLICATE_CODE", "社員コードが既に使用されています");
            }
            
            if (employeeRepository.existsByEmail(email)) {
                return EmployeeOperationResult.failure("DUPLICATE_EMAIL", "メールアドレスが既に使用されています");
            }
            
            // 社員作成
            Employee employee = new Employee();
            employee.setEmployeeCode(employeeCode);
            employee.setEmployeeName(employeeName);
            employee.setEmail(email);
            employee.setEmployeePasswordHash(passwordEncoder.encode(password));
            employee.setEmployeeRole(Employee.EmployeeRole.valueOf(role.toUpperCase()));
            employee.setHiredAt(hiredAt);
            employee.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
            employee.setPaidLeaveRemainingDays(10); // 初期値10日
            
            Employee saved = employeeRepository.save(employee);
            
            return EmployeeOperationResult.success(saved, "社員を追加しました");
            
        } catch (Exception e) {
            return EmployeeOperationResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 社員情報更新
     */
    public EmployeeOperationResult updateEmployee(Long employeeId, String employeeName, String email, String role) {
        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                return EmployeeOperationResult.failure("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            Employee employee = employeeOpt.get();
            
            // メール重複チェック（自分以外）
            if (!employee.getEmail().equals(email) && employeeRepository.existsByEmail(email)) {
                return EmployeeOperationResult.failure("DUPLICATE_EMAIL", "メールアドレスが既に使用されています");
            }
            
            // バリデーション
            if (!ValidationUtil.isValidEmail(email)) {
                return EmployeeOperationResult.failure("VALIDATION_ERROR", "メールアドレスの形式が正しくありません");
            }
            
            // 更新
            employee.setEmployeeName(employeeName);
            employee.setEmail(email);
            if (role != null) {
                employee.setEmployeeRole(Employee.EmployeeRole.valueOf(role.toUpperCase()));
            }
            
            Employee saved = employeeRepository.save(employee);
            
            return EmployeeOperationResult.success(saved, "社員情報を更新しました");
            
        } catch (Exception e) {
            return EmployeeOperationResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 退職処理
     */
    public EmployeeOperationResult retireEmployee(Long employeeId, LocalDate retiredAt) {
        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                return EmployeeOperationResult.failure("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            Employee employee = employeeOpt.get();
            
            if (employee.getEmploymentStatus() == Employee.EmploymentStatus.RETIRED) {
                return EmployeeOperationResult.failure("ALREADY_RETIRED", "既に退職済みです");
            }
            
            // 退職処理
            employee.setEmploymentStatus(Employee.EmploymentStatus.RETIRED);
            employee.setRetiredAt(retiredAt);
            
            Employee saved = employeeRepository.save(employee);
            
            return EmployeeOperationResult.success(saved, "退職処理が完了しました");
            
        } catch (Exception e) {
            return EmployeeOperationResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 有給日数調整
     */
    public PaidLeaveAdjustmentResult adjustPaidLeave(Long employeeId, int adjustmentDays, String reason) {
        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                return PaidLeaveAdjustmentResult.failure("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            Employee employee = employeeOpt.get();
            
            if (employee.getEmploymentStatus() == Employee.EmploymentStatus.RETIRED) {
                return PaidLeaveAdjustmentResult.failure("RETIRED_EMPLOYEE", "退職者の有給日数は調整できません");
            }
            
            int previousDays = employee.getPaidLeaveRemainingDays();
            int newRemainingDays = previousDays + adjustmentDays;
            
            // 負数チェック
            if (newRemainingDays < 0) {
                return PaidLeaveAdjustmentResult.failure("INVALID_ADJUSTMENT", 
                    "調整後の残日数が負数になります（現在残日数: " + previousDays + "日）");
            }
            
            // 上限チェック（99日まで）
            if (newRemainingDays > 99) {
                return PaidLeaveAdjustmentResult.failure("INVALID_ADJUSTMENT", 
                    "調整後の残日数が上限を超えます（上限: 99日）");
            }
            
            // 有給日数更新
            employee.setPaidLeaveRemainingDays(newRemainingDays);
            Employee saved = employeeRepository.save(employee);
            
            return PaidLeaveAdjustmentResult.success(saved.getEmployeeId(), 
                previousDays, adjustmentDays, newRemainingDays, "有給日数を調整しました");
            
        } catch (Exception e) {
            return PaidLeaveAdjustmentResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * パスワードリセット（管理者用）
     */
    public EmployeeOperationResult resetPassword(Long employeeId, String newPassword) {
        try {
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                return EmployeeOperationResult.failure("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            Employee employee = employeeOpt.get();
            
            if (!ValidationUtil.isValidPassword(newPassword, employee.getEmployeeCode())) {
                return EmployeeOperationResult.failure("VALIDATION_ERROR", "パスワードの形式が正しくありません");
            }
            
            employee.setEmployeePasswordHash(passwordEncoder.encode(newPassword));
            Employee saved = employeeRepository.save(employee);
            
            return EmployeeOperationResult.success(saved, "パスワードをリセットしました");
            
        } catch (Exception e) {
            return EmployeeOperationResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 社員操作結果クラス
     */
    public static class EmployeeOperationResult {
        private final boolean success;
        private final Employee employee;
        private final String message;
        private final String errorCode;
        
        private EmployeeOperationResult(boolean success, Employee employee, String message, String errorCode) {
            this.success = success;
            this.employee = employee;
            this.message = message;
            this.errorCode = errorCode;
        }
        
        public static EmployeeOperationResult success(Employee employee, String message) {
            return new EmployeeOperationResult(true, employee, message, null);
        }
        
        public static EmployeeOperationResult failure(String errorCode, String message) {
            return new EmployeeOperationResult(false, null, message, errorCode);
        }
        
        
        public boolean isSuccess() { return success; }
        public Employee getEmployee() { return employee; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
    }
    
    /**
     * 有給調整結果クラス
     */
    public static class PaidLeaveAdjustmentResult {
        private final boolean success;
        private final Long employeeId;
        private final Integer previousDays;
        private final Integer adjustmentDays;
        private final Integer newRemainingDays;
        private final String message;
        private final String errorCode;
        
        private PaidLeaveAdjustmentResult(boolean success, Long employeeId, Integer previousDays,
                                        Integer adjustmentDays, Integer newRemainingDays,
                                        String message, String errorCode) {
            this.success = success;
            this.employeeId = employeeId;
            this.previousDays = previousDays;
            this.adjustmentDays = adjustmentDays;
            this.newRemainingDays = newRemainingDays;
            this.message = message;
            this.errorCode = errorCode;
        }
        
        public static PaidLeaveAdjustmentResult success(Long employeeId, Integer previousDays,
                                                       Integer adjustmentDays, Integer newRemainingDays,
                                                       String message) {
            return new PaidLeaveAdjustmentResult(true, employeeId, previousDays, adjustmentDays,
                                               newRemainingDays, message, null);
        }
        
        public static PaidLeaveAdjustmentResult failure(String errorCode, String message) {
            return new PaidLeaveAdjustmentResult(false, null, null, null, null, message, errorCode);
        }
        
        
        public boolean isSuccess() { return success; }
        public Long getEmployeeId() { return employeeId; }
        public Integer getPreviousDays() { return previousDays; }
        public Integer getAdjustmentDays() { return adjustmentDays; }
        public Integer getNewRemainingDays() { return newRemainingDays; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
    }
}