package com.kintai.dto;

import jakarta.validation.constraints.NotNull;

public class ClockOutRequest {
    @NotNull
    private Long employeeId;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
}

