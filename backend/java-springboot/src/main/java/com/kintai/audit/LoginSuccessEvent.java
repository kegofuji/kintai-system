package com.kintai.audit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * ログイン成功イベント
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class LoginSuccessEvent extends ApplicationEvent {
    private final String employeeCode;
    private final LocalDateTime eventTimestamp;
    private final String ipAddress;
    
    public LoginSuccessEvent(Object source, String employeeCode, String ipAddress) {
        super(source);
        this.employeeCode = employeeCode;
        this.eventTimestamp = LocalDateTime.now();
        this.ipAddress = ipAddress;
    }
}
