package com.kintai.util;

import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Component
public class TimeCalculator {
    
    public static final LocalTime STANDARD_START_TIME = LocalTime.of(9, 0);
    public static final LocalTime STANDARD_END_TIME = LocalTime.of(18, 0);
    public static final LocalTime LUNCH_START_TIME = LocalTime.of(12, 0);
    public static final LocalTime LUNCH_END_TIME = LocalTime.of(13, 0);
    public static final LocalTime NIGHT_START_TIME = LocalTime.of(22, 0);
    public static final LocalTime NIGHT_END_TIME = LocalTime.of(5, 0);
    public static final int LUNCH_BREAK_MINUTES = 60;
    public static final int STANDARD_WORKING_MINUTES = 480; // 8時間
    
    /**
     * 遅刻時間計算
     * @param clockInTime 出勤時刻
     * @return 遅刻時間（分）
     */
    public int calculateLateMinutes(LocalDateTime clockInTime) {
        if (clockInTime == null) return 0;
        
        LocalTime clockInTimeOnly = clockInTime.toLocalTime();
        if (clockInTimeOnly.isAfter(STANDARD_START_TIME)) {
            return (int) Duration.between(STANDARD_START_TIME, clockInTimeOnly).toMinutes();
        }
        return 0;
    }
    
    /**
     * 早退時間計算
     * @param clockOutTime 退勤時刻
     * @return 早退時間（分）
     */
    public int calculateEarlyLeaveMinutes(LocalDateTime clockOutTime) {
        if (clockOutTime == null) return 0;
        
        LocalTime clockOutTimeOnly = clockOutTime.toLocalTime();
        if (clockOutTimeOnly.isBefore(STANDARD_END_TIME)) {
            return (int) Duration.between(clockOutTimeOnly, STANDARD_END_TIME).toMinutes();
        }
        return 0;
    }
    
    /**
     * 実働時間計算（昼休憩自動控除）
     * @param clockInTime 出勤時刻
     * @param clockOutTime 退勤時刻
     * @return 実働時間（分）
     */
    public int calculateWorkingMinutes(LocalDateTime clockInTime, LocalDateTime clockOutTime) {
        if (clockInTime == null || clockOutTime == null) return 0;
        
        long totalMinutes = Duration.between(clockInTime, clockOutTime).toMinutes();
        
        LocalTime clockInTimeOnly = clockInTime.toLocalTime();
        LocalTime clockOutTimeOnly = clockOutTime.toLocalTime();
        
        // 昼休憩をまたぐ場合は60分控除
        if (clockInTimeOnly.isBefore(LUNCH_END_TIME) && clockOutTimeOnly.isAfter(LUNCH_START_TIME)) {
            totalMinutes -= LUNCH_BREAK_MINUTES;
        }
        
        return Math.max(0, (int) totalMinutes);
    }
    
    /**
     * 残業時間計算
     * @param workingMinutes 実働時間（分）
     * @return 残業時間（分）
     */
    public int calculateOvertimeMinutes(int workingMinutes) {
        int overtime = workingMinutes - STANDARD_WORKING_MINUTES;
        return Math.max(0, overtime);
    }
    
    /**
     * 深夜勤務時間計算（22:00-翌5:00）
     * @param clockInTime 出勤時刻
     * @param clockOutTime 退勤時刻
     * @return 深夜勤務時間（分）
     */
    public int calculateNightShiftMinutes(LocalDateTime clockInTime, LocalDateTime clockOutTime) {
        if (clockInTime == null || clockOutTime == null) return 0;
        
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
     * 勤怠時間統合計算
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
        
        return new AttendanceCalculationResult(
            lateMinutes, earlyLeaveMinutes, workingMinutes, overtimeMinutes, nightShiftMinutes);
    }
    
    /**
     * 勤怠計算結果クラス
     */
    public static class AttendanceCalculationResult {
        private final int lateMinutes;
        private final int earlyLeaveMinutes;
        private final int workingMinutes;
        private final int overtimeMinutes;
        private final int nightShiftMinutes;
        
        public AttendanceCalculationResult(int lateMinutes, int earlyLeaveMinutes, 
                                         int workingMinutes, int overtimeMinutes, int nightShiftMinutes) {
            this.lateMinutes = lateMinutes;
            this.earlyLeaveMinutes = earlyLeaveMinutes;
            this.workingMinutes = workingMinutes;
            this.overtimeMinutes = overtimeMinutes;
            this.nightShiftMinutes = nightShiftMinutes;
        }
        
        // Getters
        public int getLateMinutes() { return lateMinutes; }
        public int getEarlyLeaveMinutes() { return earlyLeaveMinutes; }
        public int getWorkingMinutes() { return workingMinutes; }
        public int getOvertimeMinutes() { return overtimeMinutes; }
        public int getNightShiftMinutes() { return nightShiftMinutes; }
    }
}