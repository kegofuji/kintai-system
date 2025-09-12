package com.kintai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class MonthlySubmitRequest {
    @NotNull
    private Long employeeId;
    @NotBlank
    private String targetMonth;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public String getTargetMonth() { return targetMonth; }
    public void setTargetMonth(String targetMonth) { this.targetMonth = targetMonth; }
}







