package com.kintai.exception;

/**
 * 業務例外クラス
 * 業務ロジックで発生する例外を表現
 */
public class BusinessException extends RuntimeException {
    
    private String errorCode;
    
    /**
     * コンストラクタ
     * @param errorCode エラーコード
     * @param message エラーメッセージ
     */
    public BusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }
    
    /**
     * コンストラクタ
     * @param errorCode エラーコード
     * @param message エラーメッセージ
     * @param cause 原因例外
     */
    public BusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }
    
    /**
     * エラーコード取得
     * @return エラーコード
     */
    public String getErrorCode() {
        return errorCode;
    }
}
