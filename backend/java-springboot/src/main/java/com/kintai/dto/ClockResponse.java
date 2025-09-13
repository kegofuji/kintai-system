package com.kintai.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

/**
 * 打刻レスポンスDTO
 * 出退勤打刻結果のレスポンスデータ
 */
@Data
@Builder
public class ClockResponse {

    /**
     * 打刻成功フラグ
     */
    private boolean success;

    /**
     * 勤怠記録ID
     */
    private Long attendanceRecordId;

    /**
     * 出勤時刻（出勤時のみ）
     */
    private LocalDateTime clockInTime;

    /**
     * 退勤時刻（退勤時のみ）
     */
    private LocalDateTime clockOutTime;

    /**
     * 遅刻分（分単位）
     */
    private Integer lateMinutes;

    /**
     * 残業分（分単位）
     */
    private Integer overtimeMinutes;

    /**
     * 深夜分（分単位）
     */
    private Integer nightShiftMinutes;

    /**
     * 勤務分（分単位）
     */
    private Integer workingMinutes;

    /**
     * メッセージ
     */
    private String message;
}
