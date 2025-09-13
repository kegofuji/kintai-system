package com.kintai.service;

import com.kintai.util.TimeCalculator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * TimeCalculator 単体テスト
 * 設計書仕様に基づく時間計算ロジックのテスト
 */
@ExtendWith(MockitoExtension.class)
class TimeCalculatorTest {
    
    private TimeCalculator timeCalculator = new TimeCalculator();
    
    @Test
    @DisplayName("遅刻時間計算 - 定刻出勤")
    void calculateLateMinutes_OnTime() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 0, 0);
        int result = timeCalculator.calculateLateMinutes(clockIn);
        assertThat(result).isEqualTo(0);
    }
    
    @Test
    @DisplayName("遅刻時間計算 - 5分遅刻")
    void calculateLateMinutes_FiveMinutesLate() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 5, 0);
        int result = timeCalculator.calculateLateMinutes(clockIn);
        assertThat(result).isEqualTo(5);
    }
    
    @Test
    @DisplayName("遅刻時間計算 - 30分遅刻")
    void calculateLateMinutes_ThirtyMinutesLate() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 30, 0);
        int result = timeCalculator.calculateLateMinutes(clockIn);
        assertThat(result).isEqualTo(30);
    }
    
    @Test
    @DisplayName("早退時間計算 - 定時退勤")
    void calculateEarlyLeaveMinutes_OnTime() {
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 18, 0, 0);
        int result = timeCalculator.calculateEarlyLeaveMinutes(clockOut);
        assertThat(result).isEqualTo(0);
    }
    
    @Test
    @DisplayName("早退時間計算 - 30分早退")
    void calculateEarlyLeaveMinutes_ThirtyMinutesEarly() {
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 17, 30, 0);
        int result = timeCalculator.calculateEarlyLeaveMinutes(clockOut);
        assertThat(result).isEqualTo(30);
    }
    
    @Test
    @DisplayName("早退時間計算 - 1時間早退")
    void calculateEarlyLeaveMinutes_OneHourEarly() {
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 17, 0, 0);
        int result = timeCalculator.calculateEarlyLeaveMinutes(clockOut);
        assertThat(result).isEqualTo(60);
    }
    
    @Test
    @DisplayName("実働時間計算 - 定時勤務（昼休憩控除あり）")
    void calculateWorkingMinutes_StandardWork() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 18, 0, 0);
        int result = timeCalculator.calculateWorkingMinutes(clockIn, clockOut);
        assertThat(result).isEqualTo(480); // 9時間 - 1時間昼休憩 = 8時間
    }
    
    @Test
    @DisplayName("実働時間計算 - 昼休憩をまたがない場合")
    void calculateWorkingMinutes_NoLunchBreak() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 13, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 17, 0, 0);
        int result = timeCalculator.calculateWorkingMinutes(clockIn, clockOut);
        assertThat(result).isEqualTo(240); // 4時間（昼休憩控除なし）
    }
    
    @Test
    @DisplayName("実働時間計算 - 昼休憩をまたぐ場合")
    void calculateWorkingMinutes_WithLunchBreak() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 11, 30, 0);
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 14, 30, 0);
        int result = timeCalculator.calculateWorkingMinutes(clockIn, clockOut);
        assertThat(result).isEqualTo(120); // 3時間 - 1時間昼休憩 = 2時間
    }
    
    @Test
    @DisplayName("残業時間計算 - 定時退勤")
    void calculateOvertimeMinutes_OnTime() {
        int workingMinutes = 480; // 8時間
        int result = timeCalculator.calculateOvertimeMinutes(workingMinutes);
        assertThat(result).isEqualTo(0);
    }
    
    @Test
    @DisplayName("残業時間計算 - 1時間残業")
    void calculateOvertimeMinutes_OneHourOvertime() {
        int workingMinutes = 540; // 9時間
        int result = timeCalculator.calculateOvertimeMinutes(workingMinutes);
        assertThat(result).isEqualTo(60);
    }
    
    @Test
    @DisplayName("残業時間計算 - 2時間残業")
    void calculateOvertimeMinutes_TwoHoursOvertime() {
        int workingMinutes = 600; // 10時間
        int result = timeCalculator.calculateOvertimeMinutes(workingMinutes);
        assertThat(result).isEqualTo(120);
    }
    
    @Test
    @DisplayName("深夜勤務時間計算 - 通常勤務")
    void calculateNightShiftMinutes_NormalWork() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 18, 0, 0);
        int result = timeCalculator.calculateNightShiftMinutes(clockIn, clockOut);
        assertThat(result).isEqualTo(0);
    }
    
    @Test
    @DisplayName("深夜勤務時間計算 - 22時まで残業")
    void calculateNightShiftMinutes_UntilTenPM() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 23, 0, 0);
        int result = timeCalculator.calculateNightShiftMinutes(clockIn, clockOut);
        assertThat(result).isEqualTo(60); // 22:00-23:00 = 1時間
    }
    
    @Test
    @DisplayName("深夜勤務時間計算 - 翌日2時まで残業")
    void calculateNightShiftMinutes_UntilTwoAM() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 2, 2, 0, 0);
        int result = timeCalculator.calculateNightShiftMinutes(clockIn, clockOut);
        assertThat(result).isEqualTo(240); // 22:00-24:00 + 0:00-2:00 = 4時間
    }
    
    @Test
    @DisplayName("深夜勤務時間計算 - 翌日5時まで残業")
    void calculateNightShiftMinutes_UntilFiveAM() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 2, 5, 0, 0);
        int result = timeCalculator.calculateNightShiftMinutes(clockIn, clockOut);
        assertThat(result).isEqualTo(420); // 22:00-24:00 + 0:00-5:00 = 7時間
    }
    
    @Test
    @DisplayName("勤怠時間統合計算 - 通常勤務")
    void calculateAttendanceTimes_NormalWork() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 18, 0, 0);
        
        TimeCalculator.AttendanceCalculationResult result = 
            timeCalculator.calculateAttendanceTimes(clockIn, clockOut);
        
        assertThat(result.getLateMinutes()).isEqualTo(0);
        assertThat(result.getEarlyLeaveMinutes()).isEqualTo(0);
        assertThat(result.getWorkingMinutes()).isEqualTo(480);
        assertThat(result.getOvertimeMinutes()).isEqualTo(0);
        assertThat(result.getNightShiftMinutes()).isEqualTo(0);
    }
    
    @Test
    @DisplayName("勤怠時間統合計算 - 遅刻・残業・深夜勤務あり")
    void calculateAttendanceTimes_ComplexWork() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 15, 0); // 15分遅刻
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 2, 1, 0, 0); // 翌日1時退勤
        
        TimeCalculator.AttendanceCalculationResult result = 
            timeCalculator.calculateAttendanceTimes(clockIn, clockOut);
        
        assertThat(result.getLateMinutes()).isEqualTo(15);
        assertThat(result.getEarlyLeaveMinutes()).isEqualTo(1020); // 翌日1時退勤なので17時間早退
        assertThat(result.getWorkingMinutes()).isEqualTo(945); // 15時間45分 - 1時間昼休憩 = 14時間45分
        assertThat(result.getOvertimeMinutes()).isEqualTo(465); // 14時間45分 - 8時間 = 6時間45分
        assertThat(result.getNightShiftMinutes()).isEqualTo(180); // 22:00-24:00 + 0:00-1:00 = 3時間
    }
    
    @Test
    @DisplayName("勤怠時間統合計算 - 早退")
    void calculateAttendanceTimes_EarlyLeave() {
        LocalDateTime clockIn = LocalDateTime.of(2025, 8, 1, 9, 0, 0);
        LocalDateTime clockOut = LocalDateTime.of(2025, 8, 1, 16, 30, 0); // 1時間30分早退
        
        TimeCalculator.AttendanceCalculationResult result = 
            timeCalculator.calculateAttendanceTimes(clockIn, clockOut);
        
        assertThat(result.getLateMinutes()).isEqualTo(0);
        assertThat(result.getEarlyLeaveMinutes()).isEqualTo(90);
        assertThat(result.getWorkingMinutes()).isEqualTo(390); // 7時間30分 - 1時間昼休憩 = 6時間30分
        assertThat(result.getOvertimeMinutes()).isEqualTo(0);
        assertThat(result.getNightShiftMinutes()).isEqualTo(0);
    }
}
