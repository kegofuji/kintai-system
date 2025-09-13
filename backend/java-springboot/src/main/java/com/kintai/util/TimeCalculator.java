package com.kintai.util;

import lombok.Builder;
import lombok.Data;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * 時間計算ユーティリティ
 * 設計書の時間計算ロジック完全再現
 */
@Component
public class TimeCalculator {

    // 設計書記載の定数値（完全一致）
    public static final LocalTime STANDARD_START_TIME = LocalTime.of(9, 0);  // 09:00
    public static final LocalTime STANDARD_END_TIME = LocalTime.of(18, 0);   // 18:00
    public static final LocalTime LUNCH_START_TIME = LocalTime.of(12, 0);    // 12:00
    public static final LocalTime LUNCH_END_TIME = LocalTime.of(13, 0);      // 13:00
    public static final LocalTime NIGHT_START_TIME = LocalTime.of(22, 0);    // 22:00
    public static final LocalTime NIGHT_END_TIME = LocalTime.of(5, 0);       // 05:00（翌日）
    public static final int LUNCH_BREAK_MINUTES = 60;                        // 60分
    public static final int STANDARD_WORKING_MINUTES = 480;                  // 8時間（480分）

    /**
     * 遅刻時間計算（設計書のJavaコード完全再現）
     * @param clockInTime 出勤時刻
     * @return 遅刻時間（分）
     */
    public int calculateLateMinutes(LocalDateTime clockInTime) {
        LocalTime clockInTimeOnly = clockInTime.toLocalTime();
        if (clockInTimeOnly.isAfter(STANDARD_START_TIME)) {
            return (int) Duration.between(STANDARD_START_TIME, clockInTimeOnly).toMinutes();
        }
        return 0;
    }

    /**
     * 早退時間計算（設計書のJavaコード完全再現）
     * @param clockOutTime 退勤時刻
     * @return 早退時間（分）
     */
    public int calculateEarlyLeaveMinutes(LocalDateTime clockOutTime) {
        LocalTime clockOutTimeOnly = clockOutTime.toLocalTime();
        if (clockOutTimeOnly.isBefore(STANDARD_END_TIME)) {
            return (int) Duration.between(clockOutTimeOnly, STANDARD_END_TIME).toMinutes();
        }
        return 0;
    }

    /**
     * 実働時間計算（昼休憩自動控除）（設計書のJavaコード完全再現）
     * @param clockInTime 出勤時刻
     * @param clockOutTime 退勤時刻
     * @return 実働時間（分）
     */
    public int calculateWorkingMinutes(LocalDateTime clockInTime, LocalDateTime clockOutTime) {
        long totalMinutes = Duration.between(clockInTime, clockOutTime).toMinutes();
        
        // 昼休憩時間控除判定
        LocalTime clockInTimeOnly = clockInTime.toLocalTime();
        LocalTime clockOutTimeOnly = clockOutTime.toLocalTime();
        
        // 昼休憩をまたぐ場合は60分控除
        if (clockInTimeOnly.isBefore(LUNCH_END_TIME) && 
            clockOutTimeOnly.isAfter(LUNCH_START_TIME)) {
            totalMinutes -= LUNCH_BREAK_MINUTES;
        }
        
        return Math.max(0, (int) totalMinutes);
    }

    /**
     * 残業時間計算（設計書のJavaコード完全再現）
     * @param workingMinutes 実働時間（分）
     * @return 残業時間（分）
     */
    public int calculateOvertimeMinutes(int workingMinutes) {
        int overtime = workingMinutes - STANDARD_WORKING_MINUTES;
        return Math.max(0, overtime);
    }

    /**
     * 深夜勤務時間計算（22:00-翌5:00）（設計書のJavaコード完全再現）
     * @param clockInTime 出勤時刻
     * @param clockOutTime 退勤時刻
     * @return 深夜勤務時間（分）
     */
    public int calculateNightShiftMinutes(LocalDateTime clockInTime, LocalDateTime clockOutTime) {
        int nightMinutes = 0;
        LocalDate workDate = clockInTime.toLocalDate();
        
        // 当日22:00-24:00の深夜時間
        LocalDateTime nightStart = workDate.atTime(NIGHT_START_TIME);
        LocalDateTime nightEndToday = workDate.plusDays(1).atStartOfDay();
        
        if (clockOutTime.isAfter(nightStart)) {
            LocalDateTime actualStart = clockInTime.isAfter(nightStart) ? clockInTime : nightStart;
            LocalDateTime actualEnd = clockOutTime.isBefore(nightEndToday) ? clockOutTime : nightEndToday;
            if (actualStart.isBefore(actualEnd)) {
                nightMinutes += Duration.between(actualStart, actualEnd).toMinutes();
            }
        }
        
        // 翌日0:00-5:00の深夜時間
        LocalDateTime nightStartTomorrow = workDate.plusDays(1).atStartOfDay();
        LocalDateTime nightEndTomorrow = workDate.plusDays(1).atTime(NIGHT_END_TIME);
        
        if (clockOutTime.isAfter(nightStartTomorrow)) {
            LocalDateTime actualStart = clockInTime.isAfter(nightStartTomorrow) ? clockInTime : nightStartTomorrow;
            LocalDateTime actualEnd = clockOutTime.isBefore(nightEndTomorrow) ? clockOutTime : nightEndTomorrow;
            if (actualStart.isBefore(actualEnd)) {
                nightMinutes += Duration.between(actualStart, actualEnd).toMinutes();
            }
        }
        
        return nightMinutes;
    }

    /**
     * 勤怠時間統合計算（設計書のJavaコード完全再現）
     * @param clockInTime 出勤時刻
     * @param clockOutTime 退勤時刻
     * @return AttendanceCalculationResult
     */
    public AttendanceCalculationResult calculateAttendanceTimes(
            LocalDateTime clockInTime, LocalDateTime clockOutTime) {
        
        int lateMinutes = calculateLateMinutes(clockInTime);
        int earlyLeaveMinutes = calculateEarlyLeaveMinutes(clockOutTime);
        int workingMinutes = calculateWorkingMinutes(clockInTime, clockOutTime);
        int overtimeMinutes = calculateOvertimeMinutes(workingMinutes);
        int nightShiftMinutes = calculateNightShiftMinutes(clockInTime, clockOutTime);
        
        return AttendanceCalculationResult.builder()
                .lateMinutes(lateMinutes)
                .earlyLeaveMinutes(earlyLeaveMinutes)
                .workingMinutes(workingMinutes)
                .overtimeMinutes(overtimeMinutes)
                .nightShiftMinutes(nightShiftMinutes)
                .build();
    }

    /**
     * 勤怠計算結果DTO
     */
    @Data
    @Builder
    public static class AttendanceCalculationResult {
        private int lateMinutes;
        private int earlyLeaveMinutes;
        private int workingMinutes;
        private int overtimeMinutes;
        private int nightShiftMinutes;
    }
}
