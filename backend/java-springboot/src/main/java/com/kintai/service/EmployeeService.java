package com.kintai.service;

import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.ValidationUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

/**
 * 社員管理サービス
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class EmployeeService {
    
    private final EmployeeRepository employeeRepository;
    private final PasswordEncoder passwordEncoder;
    
    /**
     * 社員一覧取得
     */
    public List<Employee> getAllEmployees(String status, String keyword) {
        if (status != null && !"all".equals(status)) {
            return employeeRepository.findByEmploymentStatusOrderByEmployeeCode(status);
        }
        
        if (keyword != null && !keyword.trim().isEmpty()) {
            return employeeRepository.findByEmployeeNameContainingOrEmployeeCodeContaining(keyword, keyword);
        }
        
        // デフォルトは在籍者のみ
        return employeeRepository.findByEmploymentStatusOrderByEmployeeCode("active");
    }
    
    /**
     * 社員追加
     */
    public Employee addEmployee(Employee employee) {
        // 社員コード重複チェック
        if (employeeRepository.findByEmployeeCode(employee.getEmployeeCode()).isPresent()) {
            throw new BusinessException("DUPLICATE_REQUEST", "社員コードが既に使用されています");
        }
        
        // パスワード検証
        if (!ValidationUtil.validatePassword(employee.getEmployeePasswordHash(), employee.getEmployeeCode())) {
            throw new BusinessException("VALIDATION_ERROR", "パスワードが要件を満たしていません");
        }
        
        // パスワードハッシュ化
        employee.setEmployeePasswordHash(passwordEncoder.encode(employee.getEmployeePasswordHash()));
        employee.setEmploymentStatus(Employee.EmploymentStatus.ACTIVE);
        employee.setPaidLeaveRemainingDays(10);  // 初期値
        employee.setHiredAt(LocalDate.now());
        
        return employeeRepository.save(employee);
    }
    
    /**
     * 社員情報更新
     */
    public Employee updateEmployee(Long employeeId, Employee updatedEmployee) {
        Employee existing = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
        
        existing.setEmployeeName(updatedEmployee.getEmployeeName());
        existing.setEmail(updatedEmployee.getEmail());
        existing.setEmployeeRole(updatedEmployee.getEmployeeRole());
        
        return employeeRepository.save(existing);
    }
    
    /**
     * 退職処理
     */
    public void retireEmployee(Long employeeId, LocalDate retiredAt) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
        
        employee.setEmploymentStatus(Employee.EmploymentStatus.RETIRED);
        employee.setRetiredAt(retiredAt);
        employeeRepository.save(employee);
    }
    
    /**
     * 有給日数調整
     */
    public void adjustPaidLeave(Long employeeId, Integer adjustmentDays, String reason) {
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
        
        if (Employee.EmploymentStatus.RETIRED.equals(employee.getEmploymentStatus())) {
            throw new BusinessException("ACCESS_DENIED", "退職者の有給日数は調整できません");
        }
        
        int newDays = employee.getPaidLeaveRemainingDays() + adjustmentDays;
        if (newDays < 0) {
            throw new BusinessException("VALIDATION_ERROR", "調整後の残日数が負数になります");
        }
        
        employee.setPaidLeaveRemainingDays(newDays);
        employeeRepository.save(employee);
    }
}
