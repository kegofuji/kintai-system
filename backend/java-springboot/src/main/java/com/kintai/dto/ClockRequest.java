package com.kintai.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Data;

/**
 * 打刻リクエストDTO
 * 出退勤打刻時のリクエストデータ
 */
@Data
@Builder
public class ClockRequest {

    /**
     * 社員ID
     */
    @NotNull(message = "社員IDは必須です")
    private Long employeeId;
}
