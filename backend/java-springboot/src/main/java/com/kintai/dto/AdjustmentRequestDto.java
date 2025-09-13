package com.kintai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalTime;

/**
 * 打刻修正申請DTO
 * 打刻修正申請のリクエストデータ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentRequestDto {

    /**
     * 社員ID
     */
    @NotNull(message = "社員IDは必須です")
    private Long employeeId;

    /**
     * 修正対象日
     */
    @NotNull(message = "修正対象日は必須です")
    @PastOrPresent(message = "修正対象日は当日または過去日を選択してください")
    private LocalDate targetDate;

    /**
     * 修正後出勤時刻（どちらか必須）
     */
    private LocalTime correctedClockInTime;

    /**
     * 修正後退勤時刻（どちらか必須）
     */
    private LocalTime correctedClockOutTime;

    /**
     * 修正理由
     */
    @NotBlank(message = "理由は必須です")
    @Size(max = 200, message = "理由は200文字以内で入力してください")
    private String reason;
}
