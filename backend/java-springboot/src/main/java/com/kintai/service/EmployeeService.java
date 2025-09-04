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
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
@Transactional
public class EmployeeService {
    
    private static final Logger log = LoggerFactory.getLogger(EmployeeService.class);
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    /**
     * 社員登録
     * 
     * @param employeeCode 社員コード（3-10文字の半角英数字）
     * @param employeeName 氏名（50文字以内）
     * @param email メールアドレス（100文字以内）
     * @param password パスワード（8-20文字、複雑性要件あり）
     * @return 作成された社員エンティティ
     * @throws BusinessException 重複チェック、バリデーションエラー
     */
    public Employee createEmployee(String employeeCode, String employeeName, 
                                 String email, String password) {
        log.info("社員登録開始 - employeeCode: {}, employeeName: {}", employeeCode, employeeName);
        
        // 重複チェック
        if (employeeRepository.existsByEmployeeCode(employeeCode)) {
            log.warn("社員コード重複 - employeeCode: {}", employeeCode);
            throw new BusinessException("DUPLICATE_EMPLOYEE_CODE", "社員IDが既に存在します");
        }
        
        if (employeeRepository.existsByEmail(email)) {
            log.warn("メールアドレス重複 - email: {}", email);
            throw new BusinessException("DUPLICATE_EMAIL", "メールアドレスが既に存在します");
        }
        
        // パスワードバリデーション
        if (!ValidationUtil.isValidPassword(password, employeeCode)) {
            String errorMessage = ValidationUtil.getPasswordValidationMessage(password, employeeCode);
            log.warn("パスワードバリデーションエラー - employeeCode: {}, error: {}", employeeCode, errorMessage);
            throw new BusinessException("VALIDATION_ERROR", errorMessage);
        }
        
        // パスワードハッシュ化
        String hashedPassword = passwordEncoder.encode(password);
        
        // 社員エンティティ作成
        Employee employee = new Employee(employeeCode, employeeName, email, 
                                       hashedPassword, Employee.EmployeeRole.employee);
        employee.setPaidLeaveRemainingDays(10); // 初期有給日数
        
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("社員登録完了 - employeeId: {}, employeeCode: {}", savedEmployee.getEmployeeId(), employeeCode);
        
        return savedEmployee;
    }
    
    /**
     * 社員情報更新
     * 
     * @param employeeId 社員ID
     * @param employeeName 新しい氏名
     * @param email 新しいメールアドレス
     * @return 更新された社員エンティティ
     * @throws BusinessException 社員が見つからない、メール重複
     */
    public Employee updateEmployee(Long employeeId, String employeeName, String email) {
        log.info("社員情報更新開始 - employeeId: {}", employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("社員が見つからない - employeeId: {}", employeeId);
                    return new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
                });
        
        // メール重複チェック（自分以外）
        if (!employee.getEmail().equals(email) && employeeRepository.existsByEmail(email)) {
            log.warn("メールアドレス重複（更新時） - email: {}", email);
            throw new BusinessException("DUPLICATE_EMAIL", "メールアドレスが既に存在します");
        }
        
        employee.setEmployeeName(employeeName);
        employee.setEmail(email);
        
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("社員情報更新完了 - employeeId: {}", employeeId);
        
        return savedEmployee;
    }
    
    /**
     * 退職処理
     * 
     * @param employeeId 社員ID
     * @param retiredAt 退職日
     * @return 退職処理された社員エンティティ
     * @throws BusinessException 社員が見つからない
     */
    public Employee retireEmployee(Long employeeId, LocalDate retiredAt) {
        log.info("退職処理開始 - employeeId: {}, retiredAt: {}", employeeId, retiredAt);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("社員が見つからない（退職処理） - employeeId: {}", employeeId);
                    return new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
                });
        
        employee.setEmploymentStatus(Employee.EmploymentStatus.retired);
        employee.setRetiredAt(retiredAt);
        
        Employee savedEmployee = employeeRepository.save(employee);
        log.info("退職処理完了 - employeeId: {}, employeeCode: {}", employeeId, employee.getEmployeeCode());
        
        return savedEmployee;
    }
    
    /**
     * 有給日数調整
     * 
     * @param employeeId 社員ID
     * @param adjustmentDays 調整日数（正数=追加、負数=減算）
     * @param reason 調整理由
     * @return 調整後の社員エンティティ
     * @throws BusinessException 社員が見つからない、退職者、残日数不足
     */
    public Employee adjustPaidLeaveDays(Long employeeId, Integer adjustmentDays, String reason) {
        log.info("有給日数調整開始 - employeeId: {}, adjustmentDays: {}, reason: {}", 
                employeeId, adjustmentDays, reason);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("社員が見つからない（有給調整） - employeeId: {}", employeeId);
                    return new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
                });
        
        // 退職者チェック
        if (employee.getEmploymentStatus() == Employee.EmploymentStatus.retired) {
            log.warn("退職者への有給調整試行 - employeeId: {}, employeeCode: {}", 
                    employeeId, employee.getEmployeeCode());
            throw new BusinessException("EMPLOYEE_RETIRED", "退職者の有給日数は調整できません");
        }
        
        int currentDays = employee.getPaidLeaveRemainingDays();
        int newRemainingDays = currentDays + adjustmentDays;
        
        // 負数チェック
        if (newRemainingDays < 0) {
            log.warn("有給残日数が負数になる調整 - employeeId: {}, current: {}, adjustment: {}, result: {}", 
                    employeeId, currentDays, adjustmentDays, newRemainingDays);
            throw new BusinessException("INSUFFICIENT_LEAVE_DAYS", "調整後の残日数が負数になります");
        }
        
        employee.setPaidLeaveRemainingDays(newRemainingDays);
        Employee savedEmployee = employeeRepository.save(employee);
        
        log.info("有給日数調整完了 - employeeId: {}, 調整前: {}日, 調整: {}日, 調整後: {}日", 
                employeeId, currentDays, adjustmentDays, newRemainingDays);
        
        return savedEmployee;
    }
    
    /**
     * 社員検索
     * 
     * @param keyword 検索キーワード（氏名または社員コード）
     * @param status 在籍状況フィルター（null=全て）
     * @return 検索結果の社員リスト
     */
    public List<Employee> searchEmployees(String keyword, Employee.EmploymentStatus status) {
        log.info("社員検索 - keyword: {}, status: {}", keyword, status);
        
        if (keyword == null || keyword.trim().isEmpty()) {
            if (status != null) {
                return employeeRepository.findByEmploymentStatus(status);
            } else {
                return employeeRepository.findAll();
            }
        }
        
        List<Employee> results = employeeRepository.searchEmployees(keyword.trim(), status);
        log.info("社員検索完了 - 検索結果: {}件", results.size());
        
        return results;
    }
    
    /**
     * 在籍社員一覧取得
     * 
     * @return 在籍中の社員リスト
     */
    public List<Employee> getActiveEmployees() {
        log.debug("在籍社員一覧取得");
        return employeeRepository.findByEmploymentStatus(Employee.EmploymentStatus.active);
    }
    
    /**
     * 管理者一覧取得
     * 
     * @return 管理者権限を持つ社員リスト
     */
    public List<Employee> getAdminEmployees() {
        log.debug("管理者一覧取得");
        return employeeRepository.findByEmployeeRole(Employee.EmployeeRole.admin);
    }
    
    /**
     * 社員詳細取得
     * 
     * @param employeeId 社員ID
     * @return 社員エンティティ
     * @throws BusinessException 社員が見つからない
     */
    public Employee getEmployeeById(Long employeeId) {
        return employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("社員が見つからない（ID検索） - employeeId: {}", employeeId);
                    return new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
                });
    }
    
    /**
     * 社員コードで検索
     * 
     * @param employeeCode 社員コード
     * @return 社員エンティティ
     * @throws BusinessException 社員が見つからない
     */
    public Employee getEmployeeByCode(String employeeCode) {
        return employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> {
                    log.error("社員が見つからない（コード検索） - employeeCode: {}", employeeCode);
                    return new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
                });
    }
    
    /**
     * 社員の基本統計情報取得
     * 
     * @return 統計情報マップ
     */
    public Map<String, Long> getEmployeeStatistics() {
        log.debug("社員統計情報取得");
        
        long totalEmployees = employeeRepository.count();
        long activeEmployees = employeeRepository.findByEmploymentStatus(Employee.EmploymentStatus.active).size();
        long retiredEmployees = employeeRepository.findByEmploymentStatus(Employee.EmploymentStatus.retired).size();
        long adminEmployees = employeeRepository.findByEmployeeRole(Employee.EmployeeRole.admin).size();
        
        Map<String, Long> statistics = new HashMap<>();
        statistics.put("total", totalEmployees);
        statistics.put("active", activeEmployees);
        statistics.put("retired", retiredEmployees);
        statistics.put("admin", adminEmployees);
        
        return statistics;
    }
    
    /**
     * 管理者権限を付与
     * 
     * @param employeeId 社員ID
     * @return 更新された社員エンティティ
     * @throws BusinessException 社員が見つからない、退職者
     */
    public Employee grantAdminRole(Long employeeId) {
        log.info("管理者権限付与 - employeeId: {}", employeeId);
        
        Employee employee = getEmployeeById(employeeId);
        
        if (employee.getEmploymentStatus() == Employee.EmploymentStatus.retired) {
            log.warn("退職者への管理者権限付与試行 - employeeId: {}", employeeId);
            throw new BusinessException("EMPLOYEE_RETIRED", "退職者には権限を付与できません");
        }
        
        employee.setEmployeeRole(Employee.EmployeeRole.admin);
        Employee savedEmployee = employeeRepository.save(employee);
        
        log.info("管理者権限付与完了 - employeeId: {}, employeeCode: {}", 
                employeeId, employee.getEmployeeCode());
        
        return savedEmployee;
    }
    
    /**
     * 管理者権限を削除
     * 
     * @param employeeId 社員ID
     * @return 更新された社員エンティティ
     * @throws BusinessException 社員が見つからない
     */
    public Employee revokeAdminRole(Long employeeId) {
        log.info("管理者権限削除 - employeeId: {}", employeeId);
        
        Employee employee = getEmployeeById(employeeId);
        employee.setEmployeeRole(Employee.EmployeeRole.employee);
        Employee savedEmployee = employeeRepository.save(employee);
        
        log.info("管理者権限削除完了 - employeeId: {}, employeeCode: {}", 
                employeeId, employee.getEmployeeCode());
        
        return savedEmployee;
    }
}