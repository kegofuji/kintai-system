package com.kintai.dto;

public class LoginResponse {
    private Long employeeId;
    private String employeeName;
    private String role;
    private String sessionToken;

    public LoginResponse(Long employeeId, String employeeName, String role, String sessionToken) {
        this.employeeId = employeeId;
        this.employeeName = employeeName;
        this.role = role;
        this.sessionToken = sessionToken;
    }

    public Long getEmployeeId() { return employeeId; }
    public String getEmployeeName() { return employeeName; }
    public String getRole() { return role; }
    public String getSessionToken() { return sessionToken; }
}







