package com.kintai.service;

import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.repository.AttendanceRecordRepository;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.DateUtil;
import com.kintai.util.TimeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AttendanceService {
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    /**
     * 出勤打刻
     */
    public AttendanceRecord clockIn(Long employeeId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        LocalDateTime clockInTime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        
        // 既存出勤チェック
        Optional<AttendanceRecord> existingRecord = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);
        
        if (existingRecord.isPresent() && existingRecord.get().getClockInTime() != null) {
            throw new BusinessException("ALREADY_CLOCKED_IN", "本日は既に出勤打刻済みです");
        }
        
        AttendanceRecord record;
        if (existingRecord.isPresent()) {
            record = existingRecord.get();
        } else {
            record = new AttendanceRecord(employeeId, today);
        }
        
        record.setClockInTime(clockInTime);
        
        // 遅刻時間計算
        int lateMinutes = TimeCalculator.calculateLateMinutes(clockInTime);
        record.setLateMinutes(lateMinutes);
        
        return attendanceRecordRepository.save(record);
    }
    
    /**
     * 退勤打刻
     */
    public AttendanceRecord clockOut(Long employeeId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        LocalDateTime clockOutTime = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        
        // 出勤記録チェック
        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new BusinessException("NOT_CLOCKED_IN", "出勤打刻が必要です"));
        
        if (record.getClockInTime() == null) {
            throw new BusinessException("NOT_CLOCKED_IN", "出勤打刻が必要です");
        }
        
        if (record.getClockOutTime() != null) {
            throw new BusinessException("ALREADY_CLOCKED_OUT", "本日は既に退勤打刻済みです");
        }
        
        record.setClockOutTime(clockOutTime);
        
        // 勤怠時間再計算
        TimeCalculator.AttendanceCalculationResult result = 
                TimeCalculator.calculateAttendanceTimes(record.getClockInTime(), clockOutTime);
        
        record.setEarlyLeaveMinutes(result.getEarlyLeaveMinutes());
        record.setOvertimeMinutes(result.getOvertimeMinutes());
        record.setNightShiftMinutes(result.getNightShiftMinutes());
        
        return attendanceRecordRepository.save(record);
    }
    
    /**
     * 勤怠履歴取得
     */
    public List<AttendanceRecord> getAttendanceHistory(Long employeeId, String yearMonth) {
        LocalDate[] range = DateUtil.getMonthRange(yearMonth);
        return attendanceRecordRepository.findByEmployeeIdAndAttendanceDateBetween(
                employeeId, range[0], range[1]);
    }
    
    /**
     * 勤怠履歴取得（期間指定）
     */
    public List<AttendanceRecord> getAttendanceHistory(Long employeeId, 
                                                     LocalDate dateFrom, LocalDate dateTo) {
        return attendanceRecordRepository.findByEmployeeIdAndAttendanceDateBetween(
                employeeId, dateFrom, dateTo);
    }
    
    /**
     * 月末申請
     */
    public void submitMonthlyAttendance(Long employeeId, String targetMonth) {
        // 打刻漏れチェック
        validateMonthlySubmission(employeeId, targetMonth);
        
        // 該当月の全勤怠記録を申請済みに更新
        LocalDate[] range = DateUtil.getMonthRange(targetMonth);
        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateBetween(employeeId, range[0], range[1]);
        
        for (AttendanceRecord record : records) {
            if (!record.getAttendanceFixedFlag()) {
                record.setSubmissionStatus("申請済");
                attendanceRecordRepository.save(record);
            }
        }
    }
    
    /**
     * 月末申請前チェック
     */
    private void validateMonthlySubmission(Long employeeId, String targetMonth) {
        // 営業日一覧を取得
        List<LocalDate> workingDays = DateUtil.getWorkingDays(targetMonth);
        
        for (LocalDate workingDay : workingDays) {
            AttendanceRecord record = attendanceRecordRepository
                    .findByEmployeeIdAndAttendanceDate(employeeId, workingDay)
                    .orElse(null);
            
            if (record == null) {
                throw new BusinessException("INCOMPLETE_ATTENDANCE", "打刻漏れがあります: " + workingDay);
            }
            
            // 有給取得日は除外
            if (record.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.paid_leave) {
                continue;
            }
            
            // 通常勤務日の場合、出勤・退勤時刻が必須
            if (record.getClockInTime() == null || record.getClockOutTime() == null) {
                throw new BusinessException("INCOMPLETE_ATTENDANCE", 
                    "出勤または退勤の打刻が不足しています: " + workingDay);
            }
        }
        
        // 欠勤日チェック
        LocalDate[] range = DateUtil.getMonthRange(targetMonth);
        List<AttendanceRecord> absentRecords = attendanceRecordRepository
                .findByEmployeeIdAndYearMonthAndAttendanceStatus(
                        employeeId, range[0].getYear(), range[0].getMonthValue(),
                        AttendanceRecord.AttendanceStatus.absent);
        
        if (!absentRecords.isEmpty()) {
            throw new BusinessException("INCOMPLETE_ATTENDANCE", "欠勤日があるため申請できません");
        }
    }
    
    /**
     * 月末申請承認
     */
    public void approveMonthlyAttendance(Long employeeId, String targetMonth, Long approverId) {
        LocalDate[] range = DateUtil.getMonthRange(targetMonth);
        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateBetween(employeeId, range[0], range[1]);
        
        for (AttendanceRecord record : records) {
            if ("申請済".equals(record.getSubmissionStatus())) {
                record.setSubmissionStatus("承認");
                record.setAttendanceFixedFlag(true);
                attendanceRecordRepository.save(record);
            }
        }
    }
    
    /**
     * 月末申請却下
     */
    public void rejectMonthlyAttendance(Long employeeId, String targetMonth, Long approverId) {
        LocalDate[] range = DateUtil.getMonthRange(targetMonth);
        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateBetween(employeeId, range[0], range[1]);
        
        for (AttendanceRecord record : records) {
            if ("申請済".equals(record.getSubmissionStatus())) {
                record.setSubmissionStatus("却下");
                attendanceRecordRepository.save(record);
            }
        }
    }
    
    /**
     * 勤怠データ直接編集（管理者用）
     */
    public AttendanceRecord updateAttendanceRecord(Long attendanceId, 
                                                 LocalDateTime clockInTime, 
                                                 LocalDateTime clockOutTime) {
        AttendanceRecord record = attendanceRecordRepository.findById(attendanceId)
                .orElseThrow(() -> new BusinessException("ATTENDANCE_NOT_FOUND", "勤怠記録が見つかりません"));
        
        // 確定済みチェック
        if (record.getAttendanceFixedFlag()) {
            throw new BusinessException("FIXED_ATTENDANCE", "確定済みのため変更できません");
        }
        
        record.setClockInTime(clockInTime);
        record.setClockOutTime(clockOutTime);
        
        // 勤怠時間再計算
        if (clockInTime != null && clockOutTime != null) {
            TimeCalculator.AttendanceCalculationResult result = 
                    TimeCalculator.calculateAttendanceTimes(clockInTime, clockOutTime);
            
            record.setLateMinutes(result.getLateMinutes());
            record.setEarlyLeaveMinutes(result.getEarlyLeaveMinutes());
            record.setOvertimeMinutes(result.getOvertimeMinutes());
            record.setNightShiftMinutes(result.getNightShiftMinutes());
        }
        
        return attendanceRecordRepository.save(record);
    }
    
    /**
     * 勤怠整合性チェック
     */
    public List<AttendanceRecord> checkAttendanceIntegrity(String yearMonth) {
        LocalDate[] range = DateUtil.getMonthRange(yearMonth);
        
        // 打刻漏れがある記録を検索
        return attendanceRecordRepository.findAll().stream()
                .filter(record -> record.getAttendanceDate().isAfter(range[0].minusDays(1)) &&
                                record.getAttendanceDate().isBefore(range[1].plusDays(1)))
                .filter(record -> record.getClockInTime() == null || record.getClockOutTime() == null)
                .filter(record -> record.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.normal)
                .toList();
    }
}