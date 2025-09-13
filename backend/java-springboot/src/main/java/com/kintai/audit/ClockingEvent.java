package com.kintai.audit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 打刻イベント
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ClockingEvent extends ApplicationEvent {
    private final Long employeeId;
    private final String clockingType;
    private final LocalDateTime eventTimestamp;
    
    public ClockingEvent(Object source, Long employeeId, String clockingType) {
        super(source);
        this.employeeId = employeeId;
        this.clockingType = clockingType;
        this.eventTimestamp = LocalDateTime.now();
    }
}
