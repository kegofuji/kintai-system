package com.kintai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class PaidLeaveAdjustmentDto {
    @NotNull
    private Long employeeId;
    @NotNull
    private Integer adjustmentDays;
    @NotBlank
    private String reason;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public Integer getAdjustmentDays() { return adjustmentDays; }
    public void setAdjustmentDays(Integer adjustmentDays) { this.adjustmentDays = adjustmentDays; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}







