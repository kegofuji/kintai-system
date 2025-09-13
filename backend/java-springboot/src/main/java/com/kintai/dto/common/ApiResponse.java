package com.kintai.dto.common;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 統一APIレスポンス形式
 * 全APIのレスポンスを統一するためのDTO
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ApiResponse<T> {
    
    /**
     * 成功フラグ
     */
    private boolean success;
    
    /**
     * レスポンスデータ
     */
    private T data;
    
    /**
     * エラーコード
     */
    private String errorCode;
    
    /**
     * メッセージ
     */
    private String message;
    
    /**
     * 詳細情報
     */
    private Object details;
    
    /**
     * 成功レスポンス作成
     * @param data レスポンスデータ
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }
    
    /**
     * 成功レスポンス作成（メッセージ付き）
     * @param data レスポンスデータ
     * @param message メッセージ
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> success(T data, String message) {
        return ApiResponse.<T>builder()
                .success(true)
                .data(data)
                .message(message)
                .build();
    }
    
    /**
     * エラーレスポンス作成
     * @param errorCode エラーコード
     * @param message エラーメッセージ
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String errorCode, String message) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .build();
    }
    
    /**
     * エラーレスポンス作成（詳細情報付き）
     * @param errorCode エラーコード
     * @param message エラーメッセージ
     * @param details 詳細情報
     * @return ApiResponse
     */
    public static <T> ApiResponse<T> error(String errorCode, String message, Object details) {
        return ApiResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .message(message)
                .details(details)
                .build();
    }
}
