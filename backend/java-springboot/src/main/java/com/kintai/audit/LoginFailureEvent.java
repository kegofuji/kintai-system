package com.kintai.audit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * ログイン失敗イベント
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LoginFailureEvent extends ApplicationEvent {
    private final String employeeCode;
    private final String reason;
    private final LocalDateTime eventTimestamp;
    private final String ipAddress;
    
    public LoginFailureEvent(Object source, String employeeCode, String reason, String ipAddress) {
        super(source);
        this.employeeCode = employeeCode;
        this.reason = reason;
        this.eventTimestamp = LocalDateTime.now();
        this.ipAddress = ipAddress;
    }
}
