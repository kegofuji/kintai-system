package com.kintai.audit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * データアクセスイベント
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class DataAccessEvent extends ApplicationEvent {
    private final Long employeeId;
    private final String resource;
    private final String action;
    private final LocalDateTime eventTimestamp;
    
    public DataAccessEvent(Object source, Long employeeId, String resource, String action) {
        super(source);
        this.employeeId = employeeId;
        this.resource = resource;
        this.action = action;
        this.eventTimestamp = LocalDateTime.now();
    }
}
