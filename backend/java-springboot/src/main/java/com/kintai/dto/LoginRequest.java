package com.kintai.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * ログインリクエストDTO
 * ログイン時のリクエストデータ
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LoginRequest {

    /**
     * 社員コード
     */
    @NotBlank(message = "社員IDは3-10文字の半角英数字で入力してください")
    @Size(min = 3, max = 10, message = "社員IDは3-10文字で入力してください")
    @Pattern(regexp = "^[a-zA-Z0-9]+$", message = "社員IDは半角英数字で入力してください")
    private String employeeCode;

    /**
     * パスワード
     */
    @NotBlank(message = "パスワードは8文字以上で入力してください")
    @Size(min = 8, max = 20, message = "パスワードは8-20文字で入力してください")
    private String password;
}
