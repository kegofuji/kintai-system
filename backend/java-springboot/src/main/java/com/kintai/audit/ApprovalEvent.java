package com.kintai.audit;

import lombok.Data;
import lombok.EqualsAndHashCode;
import org.springframework.context.ApplicationEvent;

import java.time.LocalDateTime;

/**
 * 承認イベント
 */
@Data
@EqualsAndHashCode(callSuper = false)
public class ApprovalEvent extends ApplicationEvent {
    private final Long requestId;
    private final String requestType;
    private final String result;
    private final Long approverId;
    private final LocalDateTime eventTimestamp;
    
    public ApprovalEvent(Object source, Long requestId, String requestType, String result, Long approverId) {
        super(source);
        this.requestId = requestId;
        this.requestType = requestType;
        this.result = result;
        this.approverId = approverId;
        this.eventTimestamp = LocalDateTime.now();
    }
}
