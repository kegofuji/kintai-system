package com.kintai.config;

import com.kintai.dto.common.ApiResponse;
import com.kintai.exception.BusinessException;
import com.kintai.exception.ValidationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * グローバル例外ハンドラー
 * 設計書エラーコード完全対応
 */
@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {
    
    /**
     * バリデーション例外ハンドリング
     * @param e ValidationException
     * @return エラーレスポンス
     */
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ApiResponse<?>> handleValidation(ValidationException e) {
        log.warn("Validation error occurred: {}", e.getMessage());
        return ResponseEntity.badRequest().body(
                ApiResponse.error("VALIDATION_ERROR", "入力内容に誤りがあります", e.getValidationErrors())
        );
    }
    
    /**
     * 業務例外ハンドリング
     * @param e BusinessException
     * @return エラーレスポンス
     */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiResponse<?>> handleBusiness(BusinessException e) {
        log.warn("Business error occurred: {} - {}", e.getErrorCode(), e.getMessage());
        HttpStatus status = getHttpStatusByErrorCode(e.getErrorCode());
        return ResponseEntity.status(status).body(
                ApiResponse.error(e.getErrorCode(), e.getMessage())
        );
    }
    
    /**
     * メソッド引数バリデーション例外ハンドリング
     * @param e MethodArgumentNotValidException
     * @return エラーレスポンス
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<?>> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        log.warn("Method argument validation error occurred: {}", e.getMessage());
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
        });
        return ResponseEntity.badRequest().body(
                ApiResponse.error("VALIDATION_ERROR", "入力内容に誤りがあります", errors)
        );
    }
    
    /**
     * 一般例外ハンドリング
     * @param e Exception
     * @return エラーレスポンス
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<?>> handleGeneral(Exception e) {
        log.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                ApiResponse.error("SYSTEM_ERROR", "システムエラーが発生しました")
        );
    }
    
    /**
     * エラーコードに基づくHTTPステータス取得
     * @param errorCode エラーコード
     * @return HTTPステータス
     */
    private HttpStatus getHttpStatusByErrorCode(String errorCode) {
        return switch (errorCode) {
            // 認証・認可エラー
            case "AUTH_FAILED", "SESSION_TIMEOUT" -> HttpStatus.UNAUTHORIZED;
            case "ACCESS_DENIED" -> HttpStatus.FORBIDDEN;
            
            // リソース未発見エラー
            case "EMPLOYEE_NOT_FOUND", "REQUEST_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            
            // 業務ロジックエラー
            case "ALREADY_CLOCKED_IN", "NOT_CLOCKED_IN", "INCOMPLETE_ATTENDANCE", 
                 "INSUFFICIENT_LEAVE_DAYS", "DUPLICATE_REQUEST", "FIXED_ATTENDANCE" -> HttpStatus.BAD_REQUEST;
            
            // バリデーションエラー
            case "VALIDATION_ERROR" -> HttpStatus.BAD_REQUEST;
            
            // システムエラー
            case "SYSTEM_ERROR" -> HttpStatus.INTERNAL_SERVER_ERROR;
            
            // デフォルト
            default -> HttpStatus.INTERNAL_SERVER_ERROR;
        };
    }
}
