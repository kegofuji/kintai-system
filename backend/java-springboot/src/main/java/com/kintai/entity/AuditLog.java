package com.kintai.entity;

import jakarta.persistence.*;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 監査ログエンティティ
 * データアクセスの完全な監査記録
 */
@Entity
@Table(name = "audit_logs")
@Data
public class AuditLog {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "audit_id")
    private Long auditId;
    
    @Column(name = "entity_type", nullable = false)
    private String entityType;
    
    @Column(name = "entity_id", nullable = false)
    private String entityId;
    
    @Column(name = "operation", nullable = false)
    private String operation; // CREATE, READ, UPDATE, DELETE
    
    @Column(name = "employee_code", nullable = false)
    private String employeeCode;
    
    @Column(name = "old_values", columnDefinition = "JSON")
    private String oldValues;
    
    @Column(name = "new_values", columnDefinition = "JSON")
    private String newValues;
    
    @Column(name = "timestamp", nullable = false)
    private LocalDateTime timestamp;
    
    @Column(name = "ip_address")
    private String ipAddress;
    
    @Column(name = "user_agent")
    private String userAgent;
    
    @Column(name = "session_id")
    private String sessionId;
    
    @Column(name = "request_id")
    private String requestId;
    
    @PrePersist
    protected void onCreate() {
        if (timestamp == null) {
            timestamp = LocalDateTime.now();
        }
    }
}
