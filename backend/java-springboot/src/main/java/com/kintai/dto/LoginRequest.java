package com.kintai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class LoginRequest {
    
    @NotBlank(message = "社員コードは必須です")
    @Size(min = 3, max = 10, message = "社員コードは3-10文字で入力してください")
    private String employeeCode;
    
    @NotBlank(message = "パスワードは必須です")
    @Size(min = 8, max = 20, message = "パスワードは8-20文字で入力してください")
    private String password;
    
    // Constructors
    public LoginRequest() {}
    
    public LoginRequest(String employeeCode, String password) {
        this.employeeCode = employeeCode;
        this.password = password;
    }
    
    // Getters and Setters
    public String getEmployeeCode() { return employeeCode; }
    public void setEmployeeCode(String employeeCode) { this.employeeCode = employeeCode; }
    
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
