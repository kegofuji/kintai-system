package com.kintai.exception;

import com.kintai.dto.AttendanceResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.BindException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger logger = LoggerFactory.getLogger(GlobalExceptionHandler.class);
    
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<AttendanceResponse> handleBusinessException(BusinessException e) {
        logger.warn("Business exception occurred: {} - {}", e.getErrorCode(), e.getMessage());
        return ResponseEntity.badRequest().body(
            AttendanceResponse.error(e.getErrorCode(), e.getMessage())
        );
    }
    
    @ExceptionHandler(ValidationException.class)
    public ResponseEntity<AttendanceResponse> handleValidationException(ValidationException e) {
        logger.warn("Validation exception occurred: {}", e.getMessage());
        
        List<String> details = e.getValidationErrors().entrySet().stream()
                .flatMap(entry -> entry.getValue().stream())
                .collect(Collectors.toList());
                
        return ResponseEntity.badRequest().body(
            AttendanceResponse.error("VALIDATION_ERROR", e.getMessage(), details)
        );
    }
    
    @ExceptionHandler({MethodArgumentNotValidException.class, BindException.class})
    public ResponseEntity<AttendanceResponse> handleValidationErrors(Exception e) {
        Map<String, List<String>> errors = new HashMap<>();
        
        if (e instanceof MethodArgumentNotValidException) {
            ((MethodArgumentNotValidException) e).getBindingResult().getAllErrors()
                    .forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.computeIfAbsent(fieldName, k -> List.of()).add(errorMessage);
                    });
        } else if (e instanceof BindException) {
            ((BindException) e).getBindingResult().getAllErrors()
                    .forEach(error -> {
                        String fieldName = ((FieldError) error).getField();
                        String errorMessage = error.getDefaultMessage();
                        errors.computeIfAbsent(fieldName, k -> List.of()).add(errorMessage);
                    });
        }
        
        List<String> details = errors.values().stream()
                .flatMap(List::stream)
                .collect(Collectors.toList());
                
        return ResponseEntity.badRequest().body(
            AttendanceResponse.error("VALIDATION_ERROR", "入力値に誤りがあります", details)
        );
    }
    
    @ExceptionHandler(Exception.class)
    public ResponseEntity<AttendanceResponse> handleGeneralException(Exception e) {
        logger.error("Unexpected error occurred", e);
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
            AttendanceResponse.error("SYSTEM_ERROR", "システムエラーが発生しました")
        );
    }
}