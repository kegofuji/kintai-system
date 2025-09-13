package com.kintai.audit;

import com.kintai.entity.AuditLog;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDateTime;

/**
 * 監査ログサービス
 * データアクセスの詳細な監査記録
 */
@Service
@Transactional
public class AuditLogService {
    
    private static final Logger auditLogger = LoggerFactory.getLogger("AUDIT");
    
    /**
     * データアクセス監査ログ記録
     */
    public void logDataAccess(String entityType, String entityId, String operation, 
                             String employeeCode, String oldValues, String newValues) {
        
        AuditLog auditLog = new AuditLog();
        auditLog.setEntityType(entityType);
        auditLog.setEntityId(entityId);
        auditLog.setOperation(operation);
        auditLog.setEmployeeCode(employeeCode);
        auditLog.setOldValues(oldValues);
        auditLog.setNewValues(newValues);
        auditLog.setTimestamp(LocalDateTime.now());
        
        // HTTPリクエスト情報を取得
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes != null) {
            HttpServletRequest request = attributes.getRequest();
            auditLog.setIpAddress(getClientIP(request));
            auditLog.setUserAgent(request.getHeader("User-Agent"));
            auditLog.setSessionId(request.getSession().getId());
            auditLog.setRequestId(request.getHeader("X-Request-ID"));
        }
        
        // 監査ログ出力
        auditLogger.info("DATA_ACCESS: entityType={}, entityId={}, operation={}, employeeCode={}, " +
                        "timestamp={}, ipAddress={}, sessionId={}", 
                        auditLog.getEntityType(),
                        auditLog.getEntityId(),
                        auditLog.getOperation(),
                        auditLog.getEmployeeCode(),
                        auditLog.getTimestamp(),
                        auditLog.getIpAddress(),
                        auditLog.getSessionId());
    }
    
    /**
     * ログイン試行監査ログ
     */
    public void logLoginAttempt(String employeeCode, String result, String ipAddress) {
        auditLogger.info("LOGIN_ATTEMPT: employeeCode={}, result={}, timestamp={}, ipAddress={}", 
                        employeeCode, result, LocalDateTime.now(), ipAddress);
    }
    
    /**
     * 権限エラー監査ログ
     */
    public void logAuthorizationError(String employeeCode, String resource, String action, String ipAddress) {
        auditLogger.warn("AUTHORIZATION_ERROR: employeeCode={}, resource={}, action={}, timestamp={}, ipAddress={}", 
                        employeeCode, resource, action, LocalDateTime.now(), ipAddress);
    }
    
    /**
     * データ削除監査ログ
     */
    public void logDataDeletion(String entityType, String entityId, String employeeCode, String ipAddress) {
        auditLogger.warn("DATA_DELETION: entityType={}, entityId={}, employeeCode={}, timestamp={}, ipAddress={}", 
                        entityType, entityId, employeeCode, LocalDateTime.now(), ipAddress);
    }
    
    /**
     * クライアントIP取得
     */
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}