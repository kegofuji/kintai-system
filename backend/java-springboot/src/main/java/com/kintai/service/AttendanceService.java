package com.kintai.service;

import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.repository.AttendanceRecordRepository;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.DateUtil;
import com.kintai.util.TimeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceService {
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private TimeCalculator timeCalculator;
    
    @Autowired
    private DateUtil dateUtil;
    
    /**
     * 出勤打刻
     */
    public ClockResult clockIn(Long employeeId) {
        try {
            LocalDateTime now = dateUtil.nowInJapan();
            LocalDate today = now.toLocalDate();
            
            // 社員存在チェック
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                return ClockResult.failure("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            // 既存レコードチェック
            Optional<AttendanceRecord> existingRecord = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);
            
            if (existingRecord.isPresent() && existingRecord.get().hasClockInTime()) {
                return ClockResult.failure("ALREADY_CLOCKED_IN", "本日は既に出勤打刻済みです");
            }
            
            // レコード作成または更新
            AttendanceRecord record = existingRecord.orElse(new AttendanceRecord(employeeId, today));
            record.setClockInTime(now);
            
            // 遅刻計算
            int lateMinutes = timeCalculator.calculateLateMinutes(now);
            record.setLateMinutes(lateMinutes);
            
            AttendanceRecord saved = attendanceRecordRepository.save(record);
            
            String message = lateMinutes > 0 
                ? String.format("出勤打刻が完了しました（%d分遅刻）", lateMinutes)
                : "出勤打刻が完了しました";
            
            return ClockResult.success(saved, message);
            
        } catch (Exception e) {
            return ClockResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 退勤打刻
     */
    public ClockResult clockOut(Long employeeId) {
        try {
            LocalDateTime now = dateUtil.nowInJapan();
            LocalDate today = now.toLocalDate();
            
            // 出勤記録チェック
            Optional<AttendanceRecord> recordOpt = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);
            
            if (recordOpt.isEmpty() || !recordOpt.get().hasClockInTime()) {
                return ClockResult.failure("NOT_CLOCKED_IN", "出勤打刻が必要です");
            }
            
            AttendanceRecord record = recordOpt.get();
            
            if (record.hasClockOutTime()) {
                return ClockResult.failure("ALREADY_CLOCKED_OUT", "本日は既に退勤打刻済みです");
            }
            
            // 退勤時刻設定
            record.setClockOutTime(now);
            
            // 各種時間計算
            TimeCalculator.AttendanceCalculationResult calculation = 
                timeCalculator.calculateAttendanceTimes(record.getClockInTime(), now);
            
            record.setEarlyLeaveMinutes(calculation.getEarlyLeaveMinutes());
            record.setOvertimeMinutes(calculation.getOvertimeMinutes());
            record.setNightShiftMinutes(calculation.getNightShiftMinutes());
            
            AttendanceRecord saved = attendanceRecordRepository.save(record);
            
            String message = buildClockOutMessage(calculation);
            
            return ClockResult.success(saved, message);
            
        } catch (Exception e) {
            return ClockResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 勤怠履歴取得
     */
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getAttendanceHistory(Long employeeId, LocalDate fromDate, LocalDate toDate) {
        return attendanceRecordRepository.findByEmployeeIdAndDateRange(employeeId, fromDate, toDate);
    }
    
    /**
     * 月次勤怠データ取得
     */
    @Transactional(readOnly = true)
    public List<AttendanceRecord> getMonthlyAttendance(Long employeeId, YearMonth yearMonth) {
        return attendanceRecordRepository.findByEmployeeIdAndYearMonth(
            employeeId, yearMonth.getYear(), yearMonth.getMonthValue());
    }
    
    /**
     * 月末申請
     */
    public SubmissionResult submitMonthlyAttendance(Long employeeId, YearMonth yearMonth) {
        try {
            // 打刻漏れチェック
            List<AttendanceRecord> incompleteRecords = attendanceRecordRepository
                .findIncompleteAttendanceByEmployeeIdAndYearMonth(
                    employeeId, yearMonth.getYear(), yearMonth.getMonthValue());
            
            if (!incompleteRecords.isEmpty()) {
                List<LocalDate> missingDates = incompleteRecords.stream()
                    .map(AttendanceRecord::getAttendanceDate)
                    .toList();
                return SubmissionResult.failure("INCOMPLETE_ATTENDANCE", 
                    "打刻漏れがあります", missingDates);
            }
            
            // 該当月のレコード取得
            List<AttendanceRecord> monthlyRecords = getMonthlyAttendance(employeeId, yearMonth);
            
            // 営業日数チェック
            List<LocalDate> workingDays = dateUtil.getWorkingDaysInMonth(
                yearMonth.getYear(), yearMonth.getMonthValue());
            
            long workingDaysCount = workingDays.size();
            long completedDaysCount = monthlyRecords.stream()
                .filter(AttendanceRecord::isCompleteAttendance)
                .count();
            long paidLeaveDaysCount = monthlyRecords.stream()
                .filter(r -> r.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.PAID_LEAVE)
                .count();
            
            if (completedDaysCount + paidLeaveDaysCount < workingDaysCount) {
                return SubmissionResult.failure("INCOMPLETE_ATTENDANCE", "必要な勤務日数が不足しています", null);
            }
            
            // 申請済みに更新
            for (AttendanceRecord record : monthlyRecords) {
                if (record.getSubmissionStatus() == AttendanceRecord.SubmissionStatus.未提出) {
                    record.setSubmissionStatus(AttendanceRecord.SubmissionStatus.申請済);
                    attendanceRecordRepository.save(record);
                }
            }
            
            return SubmissionResult.success(workingDaysCount, completedDaysCount, paidLeaveDaysCount);
            
        } catch (Exception e) {
            return SubmissionResult.failure("SYSTEM_ERROR", "システムエラーが発生しました", null);
        }
    }
    
    /**
     * 勤怠集計
     */
    @Transactional(readOnly = true)
    public AttendanceSummary getAttendanceSummary(Long employeeId, LocalDate fromDate, LocalDate toDate) {
        Object[] result = attendanceRecordRepository.getAttendanceSummary(employeeId, fromDate, toDate);
        
        return new AttendanceSummary(
            ((Number) result[0]).intValue(),  // totalLateMinutes
            ((Number) result[1]).intValue(),  // totalEarlyLeaveMinutes
            ((Number) result[2]).intValue(),  // totalOvertimeMinutes
            ((Number) result[3]).intValue(),  // totalNightShiftMinutes
            ((Number) result[4]).intValue(),  // paidLeaveDays
            ((Number) result[5]).intValue()   // absentDays
        );
    }
    
    /**
     * 退勤メッセージ生成
     */
    private String buildClockOutMessage(TimeCalculator.AttendanceCalculationResult calculation) {
        StringBuilder message = new StringBuilder("退勤打刻が完了しました");
        
        if (calculation.getEarlyLeaveMinutes() > 0) {
            message.append(String.format("（%d分早退）", calculation.getEarlyLeaveMinutes()));
        } else if (calculation.getOvertimeMinutes() > 0) {
            message.append(String.format("（%d分残業）", calculation.getOvertimeMinutes()));
        }
        
        return message.toString();
    }
    
    /**
     * 打刻結果クラス
     */
    public static class ClockResult {
        private final boolean success;
        private final AttendanceRecord record;
        private final String message;
        private final String errorCode;
        
        private ClockResult(boolean success, AttendanceRecord record, String message, String errorCode) {
            this.success = success;
            this.record = record;
            this.message = message;
            this.errorCode = errorCode;
        }
        
        public static ClockResult success(AttendanceRecord record, String message) {
            return new ClockResult(true, record, message, null);
        }
        
        public static ClockResult failure(String errorCode, String message) {
            return new ClockResult(false, null, message, errorCode);
        }
        
        public boolean isSuccess() { return success; }
        public AttendanceRecord getRecord() { return record; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
    }
    
    /**
     * 月末申請結果クラス
     */
    public static class SubmissionResult {
        private final boolean success;
        private final String errorCode;
        private final String message;
        private final List<LocalDate> missingDates;
        private final Long workingDaysCount;
        private final Long completedDaysCount;
        private final Long paidLeaveDaysCount;
        
        private SubmissionResult(boolean success, String errorCode, String message,
                               List<LocalDate> missingDates, Long workingDaysCount,
                               Long completedDaysCount, Long paidLeaveDaysCount) {
            this.success = success;
            this.errorCode = errorCode;
            this.message = message;
            this.missingDates = missingDates;
            this.workingDaysCount = workingDaysCount;
            this.completedDaysCount = completedDaysCount;
            this.paidLeaveDaysCount = paidLeaveDaysCount;
        }
        
        public static SubmissionResult success(Long workingDaysCount, Long completedDaysCount, Long paidLeaveDaysCount) {
            return new SubmissionResult(true, null, "月末申請が完了しました", 
                null, workingDaysCount, completedDaysCount, paidLeaveDaysCount);
        }
        
        public static SubmissionResult failure(String errorCode, String message, List<LocalDate> missingDates) {
            return new SubmissionResult(false, errorCode, message, missingDates, null, null, null);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getErrorCode() { return errorCode; }
        public String getMessage() { return message; }
        public List<LocalDate> getMissingDates() { return missingDates; }
        public Long getWorkingDaysCount() { return workingDaysCount; }
        public Long getCompletedDaysCount() { return completedDaysCount; }
        public Long getPaidLeaveDaysCount() { return paidLeaveDaysCount; }
    }
    
    /**
     * 勤怠集計クラス
     */
    public static class AttendanceSummary {
        private final int totalLateMinutes;
        private final int totalEarlyLeaveMinutes;
        private final int totalOvertimeMinutes;
        private final int totalNightShiftMinutes;
        private final int paidLeaveDays;
        private final int absentDays;
        
        public AttendanceSummary(int totalLateMinutes, int totalEarlyLeaveMinutes,
                               int totalOvertimeMinutes, int totalNightShiftMinutes,
                               int paidLeaveDays, int absentDays) {
            this.totalLateMinutes = totalLateMinutes;
            this.totalEarlyLeaveMinutes = totalEarlyLeaveMinutes;
            this.totalOvertimeMinutes = totalOvertimeMinutes;
            this.totalNightShiftMinutes = totalNightShiftMinutes;
            this.paidLeaveDays = paidLeaveDays;
            this.absentDays = absentDays;
        }
        
        // Getters
        public int getTotalLateMinutes() { return totalLateMinutes; }
        public int getTotalEarlyLeaveMinutes() { return totalEarlyLeaveMinutes; }
        public int getTotalOvertimeMinutes() { return totalOvertimeMinutes; }
        public int getTotalNightShiftMinutes() { return totalNightShiftMinutes; }
        public int getPaidLeaveDays() { return paidLeaveDays; }
        public int getAbsentDays() { return absentDays; }
    }
}