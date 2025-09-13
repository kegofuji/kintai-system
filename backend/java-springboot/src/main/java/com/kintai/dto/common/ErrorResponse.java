package com.kintai.dto.common;

import lombok.Builder;
import lombok.Data;

/**
 * エラーレスポンスDTO
 * エラー情報を格納するためのDTO
 */
@Data
@Builder
public class ErrorResponse {
    
    /**
     * 成功フラグ（常にfalse）
     */
    @Builder.Default
    private boolean success = false;
    
    /**
     * エラーコード
     */
    private String errorCode;
    
    /**
     * エラーメッセージ
     */
    private String message;
    
    /**
     * 詳細情報
     */
    private Object details;
}
