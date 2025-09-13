package com.kintai.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 勤怠サマリーDTO
 * 勤怠履歴の集計情報
 */
@Data
@Builder
public class AttendanceSummary {

    /**
     * 総勤務分（分単位）
     */
    private Integer totalWorkingMinutes;

    /**
     * 総残業分（分単位）
     */
    private Integer totalOvertimeMinutes;

    /**
     * 総深夜分（分単位）
     */
    private Integer totalNightShiftMinutes;

    /**
     * 総遅刻分（分単位）
     */
    private Integer totalLateMinutes;

    /**
     * 総早退分（分単位）
     */
    private Integer totalEarlyLeaveMinutes;

    /**
     * 有給取得日数
     */
    private Integer paidLeaveDays;

    /**
     * 欠勤日数
     */
    private Integer absentDays;
}
