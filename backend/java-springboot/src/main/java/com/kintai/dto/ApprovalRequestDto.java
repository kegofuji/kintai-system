package com.kintai.dto;

import jakarta.validation.constraints.NotNull;

public class ApprovalRequestDto {
    @NotNull
    private Long approverId;

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
}


