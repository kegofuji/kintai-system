package com.kintai.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * 有給申請DTO
 * 有給休暇申請のリクエストデータ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequestDto {

    /**
     * 社員ID
     */
    @NotNull(message = "社員IDは必須です")
    private Long employeeId;

    /**
     * 有給取得日
     */
    @NotNull(message = "有給取得日は必須です")
    @Future(message = "有給取得日は明日以降を選択してください")
    private LocalDate leaveDate;

    /**
     * 申請理由
     */
    @NotBlank(message = "理由は必須です")
    @Size(max = 200, message = "理由は200文字以内で入力してください")
    private String reason;
}
