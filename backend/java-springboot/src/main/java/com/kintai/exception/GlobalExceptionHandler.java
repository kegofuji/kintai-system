package com.kintai.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import jakarta.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {
    
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<ErrorResponse> handleValidation(ValidationException e, HttpServletRequest request) {
        log.warn("Validation error at {}: {}", request.getRequestURI(), e.getMessage());
        
        return ResponseEntity.badRequest().body(
            ErrorResponse.builder()
                .success(false)
                .errorCode("VALIDATION_ERROR")
                .message(e.getMessage())
                .details(e.getValidationErrors())
                .build()
        );
    }
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException e, HttpServletRequest request) {
        log.warn("Business error at {}: {} - {}", request.getRequestURI(), e.getErrorCode(), e.getMessage());
        
        HttpStatus status = switch (e.getErrorCode()) {
            case "AUTH_FAILED", "SESSION_TIMEOUT" -> HttpStatus.UNAUTHORIZED;
            case "ACCESS_DENIED" -> HttpStatus.FORBIDDEN;
            case "EMPLOYEE_NOT_FOUND", "REQUEST_NOT_FOUND", "ATTENDANCE_NOT_FOUND" -> HttpStatus.NOT_FOUND;
            default -> HttpStatus.BAD_REQUEST;
        };
        
        return ResponseEntity.status(status).body(
            ErrorResponse.builder()
                .success(false)
                .errorCode(e.getErrorCode())
                .message(e.getMessage())
                .build()
        );
    }
    
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException e) {
        Map<String, String> errors = new HashMap<>();
        e.getBindingResult().getFieldErrors().forEach(error -> 
            errors.put(error.getField(), error.getDefaultMessage())
        );
        
        return ResponseEntity.badRequest().body(
            ErrorResponse.builder()
                .success(false)
                .errorCode("VALIDATION_ERROR")
                .message("入力内容に誤りがあります")
                .details(errors)
                .build()
        );
    }
    
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ErrorResponse> handleRuntimeException(RuntimeException e, HttpServletRequest request) {
        log.error("Runtime error at {}: {}", request.getRequestURI(), e.getMessage(), e);
        
        // 既知のエラーコードを持つRuntimeExceptionをBusinessExceptionとして処理
        String message = e.getMessage();
        if (isKnownErrorCode(message)) {
            BusinessException businessException = new BusinessException(message, getErrorMessage(message));
            return handleBusiness(businessException, request);
        }
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse.builder()
                .success(false)
                .errorCode("SYSTEM_ERROR")
                .message("システムエラーが発生しました")
                .build()
        );
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneral(Exception e, HttpServletRequest request) {
        log.error("Unexpected error at {}: {}", request.getRequestURI(), e.getMessage(), e);
        
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            ErrorResponse.builder()
                .success(false)
                .errorCode("SYSTEM_ERROR")
                .message("システムエラーが発生しました")
                .build()
        );
    }
    
    private boolean isKnownErrorCode(String message) {
        return message != null && (
            message.equals("AUTH_FAILED") ||
            message.equals("SESSION_TIMEOUT") ||
            message.equals("ACCESS_DENIED") ||
            message.equals("ALREADY_CLOCKED_IN") ||
            message.equals("NOT_CLOCKED_IN") ||
            message.equals("INCOMPLETE_ATTENDANCE") ||
            message.equals("INSUFFICIENT_LEAVE_DAYS") ||
            message.equals("DUPLICATE_REQUEST") ||
            message.equals("FIXED_ATTENDANCE") ||
            message.equals("EMPLOYEE_NOT_FOUND") ||
            message.equals("REQUEST_NOT_FOUND") ||
            message.equals("ALREADY_WORKED") ||
            message.equals("DUPLICATE_EMPLOYEE_CODE") ||
            message.equals("DUPLICATE_EMAIL") ||
            message.equals("EMPLOYEE_RETIRED")
        );
    }
    
    private String getErrorMessage(String errorCode) {
        return switch (errorCode) {
            case "AUTH_FAILED" -> "認証に失敗しました";
            case "SESSION_TIMEOUT" -> "セッションがタイムアウトしました";
            case "ACCESS_DENIED" -> "アクセス権限がありません";
            case "ALREADY_CLOCKED_IN" -> "既に出勤打刻済みです";
            case "NOT_CLOCKED_IN" -> "出勤打刻が必要です";
            case "INCOMPLETE_ATTENDANCE" -> "打刻漏れがあります";
            case "INSUFFICIENT_LEAVE_DAYS" -> "有給残日数が不足しています";
            case "DUPLICATE_REQUEST" -> "既に申請済みです";
            case "FIXED_ATTENDANCE" -> "確定済みのため変更できません";
            case "EMPLOYEE_NOT_FOUND" -> "社員が見つかりません";
            case "REQUEST_NOT_FOUND" -> "申請が見つかりません";
            case "ALREADY_WORKED" -> "既に出勤済みの日は申請できません";
            case "DUPLICATE_EMPLOYEE_CODE" -> "社員IDが既に存在します";
            case "DUPLICATE_EMAIL" -> "メールアドレスが既に存在します";
            case "EMPLOYEE_RETIRED" -> "退職者は操作できません";
            default -> "エラーが発生しました";
        };
    }
}