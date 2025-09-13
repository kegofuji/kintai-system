package com.kintai.exception;

import java.util.Map;

/**
 * バリデーション例外クラス
 * 入力値検証で発生する例外を表現
 */
public class ValidationException extends RuntimeException {
    
    private Map<String, String> validationErrors;
    
    /**
     * コンストラクタ
     * @param validationErrors バリデーションエラー詳細
     */
    public ValidationException(Map<String, String> validationErrors) {
        super("Validation failed");
        this.validationErrors = validationErrors;
    }
    
    /**
     * コンストラクタ
     * @param message エラーメッセージ
     * @param validationErrors バリデーションエラー詳細
     */
    public ValidationException(String message, Map<String, String> validationErrors) {
        super(message);
        this.validationErrors = validationErrors;
    }
    
    /**
     * バリデーションエラー詳細取得
     * @return バリデーションエラー詳細
     */
    public Map<String, String> getValidationErrors() {
        return validationErrors;
    }
}
