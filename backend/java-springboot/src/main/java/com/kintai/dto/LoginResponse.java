package com.kintai.dto;

import lombok.Builder;
import lombok.Data;

/**
 * ログインレスポンスDTO
 * ログイン結果のレスポンスデータ
 */
@Data
@Builder
public class LoginResponse {

    /**
     * ログイン成功フラグ
     */
    private boolean success;

    /**
     * 社員ID
     */
    private Long employeeId;

    /**
     * 社員名
     */
    private String employeeName;

    /**
     * ロール
     */
    private String role;

    /**
     * セッショントークン
     */
    private String sessionToken;
}
