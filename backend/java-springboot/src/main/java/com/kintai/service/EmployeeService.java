package com.kintai.service;

import com.kintai.entity.Employee;
import com.kintai.entity.Employee.EmploymentStatus;
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
import java.util.Optional;

@Service
public class EmployeeService {
    
    private static final Logger logger = LoggerFactory.getLogger(EmployeeService.class);
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private PasswordEncoder passwordEncoder;
    
    @Autowired
    private ValidationUtil validationUtil;

    public List<Employee> getEmployees(EmploymentStatus employmentStatus, String keyword) {
        // TODO Auto-generated method stub
        throw new UnsupportedOperationException("Unimplemented method 'getEmployees'");
    }
    
    /**
     * 社員一覧取得
     * @param status 在籍状況フィ        @NotBlank(message = "社員コードは必須です")
        @Size(min = 3, max = 10, message = "社員コードは3-10文字で入力してください")
        private String employeeCode;
        
        @NotBlank(message = "氏名は必須です")
        @Size(max = 50, message = "氏名は50文字以内で入力してください")
        private String employeeName;
        
        @NotBlank(message = "メールアドレスは必須です")
        @Size(max = 100, message = "メールアドレスは100文字以内で入力してください")
        private String email;
        
        @NotBlank(message = "パスワードは必須です")
        @Size(min = 8, max = 20, message = "パスワードは8-20文字で入力してください")
        private String password;
        
        private String role = "employee";
        
        // Constructors
        public EmployeeCreateDto() {}
        
        // Getters and Setters
        public String getEmployeeCode() { return employeeCode; }
        public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
        
        public String getEmployeeName() { return employeeName; }
        public void setEmployeeName(String employeeName) { this.employeeName = employeeName; }
        
        public String getEmail() { return email; }
        public void setEmail(String email) { this.email = email; }
        
        public String getPassword() { return password; }
        public void setPassword(String password) { this.password = password; }
        
        public String getRole() { return role; }
        public void setRole(String role) { this.role = role; }
    }
    
    // 有給日数調整用DTO
    public static class PaidLeaveAdjustmentDto {
        @NotNull(message = "社員IDは必須です")
        private Long employeeId;
        
        @NotNull(message = "調整日数は必須です")
        private Integer adjustmentDays;
        
        @NotBlank(message = "調整理由は必須です")
        @Size(max = 200, message = "調整理由は200文字以内で入力してください")
        private String reason;
        
        // Constructors
        public PaidLeaveAdjustmentDto() {}
        
        public PaidLeaveAdjustmentDto(Long employeeId, Integer adjustmentDays, String reason) {
            this.employeeId = employeeId;
            this.adjustmentDays = adjustmentDays;
            this.reason = reason;
        }
        
        // Getters and Setters
        public Long getEmployeeId() { return employeeId; }
        public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
        
        public Integer getAdjustmentDays() { return adjustmentDays; }
        public void setAdjustmentDays(Integer adjustmentDays) { this.adjustmentDays = adjustmentDays; }
        
        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }
    }
}