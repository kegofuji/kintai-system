package com.kintai.dto;

import jakarta.validation.constraints.NotBlank;

public class ReportGenerateDto {
    @NotBlank
    private String yearMonth; // e.g. 2025-08

    public String getYearMonth() { return yearMonth; }
    public void setYearMonth(String yearMonth) { this.yearMonth = yearMonth; }
}

