package com.kintai.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 勤怠情報DTO
 * 1日分の勤怠情報
 */
@Data
@Builder
public class AttendanceInfo {

    /**
     * 勤怠日（YYYY-MM-DD形式）
     */
    private String attendanceDate;

    /**
     * 出勤時刻（HH:MM:SS形式）
     */
    private String clockInTime;

    /**
     * 退勤時刻（HH:MM:SS形式）
     */
    private String clockOutTime;

    /**
     * 遅刻分（分単位）
     */
    private Integer lateMinutes;

    /**
     * 早退分（分単位）
     */
    private Integer earlyLeaveMinutes;

    /**
     * 残業分（分単位）
     */
    private Integer overtimeMinutes;

    /**
     * 深夜分（分単位）
     */
    private Integer nightShiftMinutes;

    /**
     * 勤怠ステータス
     */
    private String attendanceStatus;

    /**
     * 月末申請ステータス
     */
    private String submissionStatus;

    /**
     * 勤怠確定フラグ
     */
    private Boolean attendanceFixedFlag;
}
