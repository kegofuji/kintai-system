package com.kintai.dto;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;

public class RetireRequestDto {
    @NotNull
    private LocalDate retiredAt;

    public LocalDate getRetiredAt() { return retiredAt; }
    public void setRetiredAt(LocalDate retiredAt) { this.retiredAt = retiredAt; }
}







