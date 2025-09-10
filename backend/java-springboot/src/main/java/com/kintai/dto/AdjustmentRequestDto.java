package com.kintai.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class AdjustmentRequestDto {
    @NotNull
    private Long employeeId;
    @NotNull
    private LocalDate targetDate;
    private LocalTime correctedClockInTime;
    private LocalTime correctedClockOutTime;
    private String reason;

    public Long getEmployeeId() { return employeeId; }
    public void setEmployeeId(Long employeeId) { this.employeeId = employeeId; }
    public LocalDate getTargetDate() { return targetDate; }
    public void setTargetDate(LocalDate targetDate) { this.targetDate = targetDate; }
    public LocalTime getCorrectedClockInTime() { return correctedClockInTime; }
    public void setCorrectedClockInTime(LocalTime correctedClockInTime) { this.correctedClockInTime = correctedClockInTime; }
    public LocalTime getCorrectedClockOutTime() { return correctedClockOutTime; }
    public void setCorrectedClockOutTime(LocalTime correctedClockOutTime) { this.correctedClockOutTime = correctedClockOutTime; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

