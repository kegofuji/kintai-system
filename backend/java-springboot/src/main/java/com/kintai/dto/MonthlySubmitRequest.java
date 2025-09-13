package com.kintai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Builder;
import lombok.Data;

/**
 * 月末申請リクエストDTO
 * 月末勤怠申請のリクエストデータ
 */
@Data
@Builder
public class MonthlySubmitRequest {

    /**
     * 社員ID
     */
    @NotNull(message = "社員IDは必須です")
    private Long employeeId;

    /**
     * 申請対象月（YYYY-MM形式）
     */
    @NotBlank(message = "申請対象月は必須です")
    @Pattern(regexp = "^\\d{4}-\\d{2}$", message = "YYYY-MM形式で入力してください")
    private String targetMonth;
}
