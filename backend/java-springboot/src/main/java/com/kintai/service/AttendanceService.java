package com.kintai.service;

import com.kintai.dto.AttendanceHistoryRequest;
import com.kintai.dto.AttendanceHistoryResponse;
import com.kintai.dto.AttendanceInfo;
import com.kintai.dto.AttendanceSummary;
import com.kintai.dto.ClockResponse;
import com.kintai.dto.EmployeeInfo;
import com.kintai.dto.PeriodInfo;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.repository.AttendanceRecordRepository;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.DateUtil;
import com.kintai.util.TimeCalculator;
import com.kintai.util.TimeCalculator.AttendanceCalculationResult;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 勤怠管理サービス
 * 設計書のビジネスロジック完全再現
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class AttendanceService {
    
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final TimeCalculator timeCalculator;
    
    /**
     * 出勤打刻
     */
    public ClockResponse clockIn(Long employeeId) {
        LocalDate today = DateUtil.getCurrentDate();
        LocalDateTime now = DateUtil.getCurrentDateTime();
        
        // 当日の既存打刻チェック
        Optional<AttendanceRecord> existing = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today);
        
        if (existing.isPresent() && existing.get().getClockInTime() != null) {
            throw new BusinessException("ALREADY_CLOCKED_IN", "既に出勤打刻済みです");
        }
        
        // 出勤打刻処理
        AttendanceRecord record = existing.orElse(
                AttendanceRecord.builder()
                        .employeeId(employeeId)
                        .attendanceDate(today)
                        .attendanceStatus(AttendanceRecord.AttendanceStatus.NORMAL)
                        .submissionStatus(AttendanceRecord.SubmissionStatus.NOT_SUBMITTED)
                        .attendanceFixedFlag(false)
                        .build()
        );
        
        record.setClockInTime(now);
        
        // 遅刻時間計算
        int lateMinutes = timeCalculator.calculateLateMinutes(now);
        record.setLateMinutes(lateMinutes);
        
        AttendanceRecord saved = attendanceRecordRepository.save(record);
        
        String message = lateMinutes > 0 ? 
                String.format("出勤打刻が完了しました（%d分遅刻）", lateMinutes) : 
                "出勤打刻が完了しました";
        
        return ClockResponse.builder()
                .success(true)
                .attendanceRecordId(saved.getAttendanceId())
                .clockInTime(now)
                .lateMinutes(lateMinutes)
                .message(message)
                .build();
    }
    
    /**
     * 退勤打刻
     */
    public ClockResponse clockOut(Long employeeId) {
        LocalDate today = DateUtil.getCurrentDate();
        LocalDateTime now = DateUtil.getCurrentDateTime();
        
        // 出勤打刻チェック
        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(employeeId, today)
                .orElseThrow(() -> new BusinessException("NOT_CLOCKED_IN", "出勤打刻が必要です"));
        
        if (record.getClockInTime() == null) {
            throw new BusinessException("NOT_CLOCKED_IN", "出勤打刻が必要です");
        }
        
        if (record.getClockOutTime() != null) {
            throw new BusinessException("ALREADY_CLOCKED_OUT", "既に退勤打刻済みです");
        }
        
        // 退勤打刻処理
        record.setClockOutTime(now);
        
        // 勤怠時間計算（設計書ロジック使用）
        AttendanceCalculationResult calculation = timeCalculator
                .calculateAttendanceTimes(record.getClockInTime(), now);
        
        record.setEarlyLeaveMinutes(calculation.getEarlyLeaveMinutes());
        record.setOvertimeMinutes(calculation.getOvertimeMinutes());
        record.setNightShiftMinutes(calculation.getNightShiftMinutes());
        
        AttendanceRecord saved = attendanceRecordRepository.save(record);
        
        String message = calculation.getOvertimeMinutes() > 0 ?
                String.format("退勤打刻が完了しました（%d分残業）", calculation.getOvertimeMinutes()) :
                "退勤打刻が完了しました";
        
        return ClockResponse.builder()
                .success(true)
                .attendanceRecordId(saved.getAttendanceId())
                .clockOutTime(now)
                .overtimeMinutes(calculation.getOvertimeMinutes())
                .nightShiftMinutes(calculation.getNightShiftMinutes())
                .workingMinutes(calculation.getWorkingMinutes())
                .message(message)
                .build();
    }
    
    /**
     * 月末申請（設計書のチェックロジック完全再現）
     */
    public void submitMonthlyAttendance(Long employeeId, String targetMonth) {
        validateMonthlySubmission(employeeId, targetMonth);
        
        // 当月の全勤怠記録を「申請済」に更新
        YearMonth ym = YearMonth.parse(targetMonth);
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        
        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateBetween(employeeId, start, end);
        
        records.forEach(record -> record.setSubmissionStatus(AttendanceRecord.SubmissionStatus.SUBMITTED));
        attendanceRecordRepository.saveAll(records);
    }
    
    /**
     * 月末申請前チェック（設計書のJavaコード完全再現）
     */
    public void validateMonthlySubmission(Long employeeId, String targetMonth) {
        // 1. 当月の営業日一覧を取得（土日祝日除外）
        List<LocalDate> workingDays = DateUtil.getWorkingDays(targetMonth);
        
        // 2. 各営業日について出勤・退勤記録の存在確認
        List<String> missingDates = new ArrayList<>();
        for (LocalDate workingDay : workingDays) {
            Optional<AttendanceRecord> recordOpt = attendanceRecordRepository
                    .findByEmployeeIdAndAttendanceDate(employeeId, workingDay);
            
            if (recordOpt.isEmpty()) {
                missingDates.add(workingDay.toString());
                continue;
            }
            
            AttendanceRecord record = recordOpt.get();
            
            // 3. 有給取得日は除外（attendance_status = 'paid_leave'）
            if (AttendanceRecord.AttendanceStatus.PAID_LEAVE.equals(record.getAttendanceStatus())) {
                continue;
            }
            
            // 4. 通常勤務日の場合、出勤・退勤時刻が必須
            if (record.getClockInTime() == null || record.getClockOutTime() == null) {
                missingDates.add(workingDay.toString());
            }
        }
        
        if (!missingDates.isEmpty()) {
            Map<String, Object> details = new HashMap<>();
            details.put("missingDates", missingDates);
            details.put("totalWorkingDays", workingDays.size());
            details.put("completedDays", workingDays.size() - missingDates.size());
            
            throw new BusinessException("INCOMPLETE_ATTENDANCE", "打刻漏れがあります");
        }
        
        // 5. 欠勤日がある場合は申請不可
        List<AttendanceRecord> absentRecords = attendanceRecordRepository
                .findByEmployeeIdAndYearMonthAndAttendanceStatus(employeeId, targetMonth, "absent");
        
        if (!absentRecords.isEmpty()) {
            List<String> absentDates = absentRecords.stream()
                    .map(record -> record.getAttendanceDate().toString())
                    .collect(Collectors.toList());
            
            Map<String, Object> details = new HashMap<>();
            details.put("absentDates", absentDates);
            
            throw new BusinessException("INCOMPLETE_ATTENDANCE", "欠勤日があるため申請できません");
        }
    }
    
    /**
     * 勤怠履歴取得
     */
    public AttendanceHistoryResponse getAttendanceHistory(AttendanceHistoryRequest request) {
        // 期間設定
        LocalDate startDate, endDate;
        if (request.getYearMonth() != null) {
            YearMonth ym = YearMonth.parse(request.getYearMonth());
            startDate = ym.atDay(1);
            endDate = ym.atEndOfMonth();
        } else {
            startDate = request.getDateFrom();
            endDate = request.getDateTo();
        }
        
        // 勤怠データ取得
        List<AttendanceRecord> records = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
                        request.getEmployeeId(), startDate, endDate);
        
        // 社員情報取得
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
        
        // レスポンス構築
        List<AttendanceInfo> attendanceList = records.stream().map(record -> 
                AttendanceInfo.builder()
                        .attendanceDate(record.getAttendanceDate().toString())
                        .clockInTime(record.getClockInTime() != null ? 
                                record.getClockInTime().toLocalTime().toString() : null)
                        .clockOutTime(record.getClockOutTime() != null ? 
                                record.getClockOutTime().toLocalTime().toString() : null)
                        .lateMinutes(record.getLateMinutes())
                        .earlyLeaveMinutes(record.getEarlyLeaveMinutes())
                        .overtimeMinutes(record.getOvertimeMinutes())
                        .nightShiftMinutes(record.getNightShiftMinutes())
                        .attendanceStatus(record.getAttendanceStatus().getValue())
                        .submissionStatus(record.getSubmissionStatus().getValue())
                        .attendanceFixedFlag(record.getAttendanceFixedFlag())
                        .build()
        ).collect(Collectors.toList());
        
        // 集計データ計算
        AttendanceSummary summary = calculateSummary(records);
        
        return AttendanceHistoryResponse.builder()
                .success(true)
                .employee(EmployeeInfo.builder()
                        .employeeId(employee.getEmployeeId())
                        .employeeName(employee.getEmployeeName())
                        .employeeCode(employee.getEmployeeCode())
                        .build())
                .period(PeriodInfo.builder()
                        .from(startDate.toString())
                        .to(endDate.toString())
                        .build())
                .attendanceList(attendanceList)
                .summary(summary)
                .build();
    }
    
    /**
     * 勤怠集計計算
     */
    private AttendanceSummary calculateSummary(List<AttendanceRecord> records) {
        int totalWorking = 0;
        int totalOvertime = 0;
        int totalNightShift = 0;
        int totalLate = 0;
        int totalEarlyLeave = 0;
        int paidLeaveDays = 0;
        int absentDays = 0;
        
        for (AttendanceRecord record : records) {
            if (record.getClockInTime() != null && record.getClockOutTime() != null) {
                AttendanceCalculationResult calc = timeCalculator
                        .calculateAttendanceTimes(record.getClockInTime(), record.getClockOutTime());
                totalWorking += calc.getWorkingMinutes();
            }
            
            totalOvertime += record.getOvertimeMinutes();
            totalNightShift += record.getNightShiftMinutes();
            totalLate += record.getLateMinutes();
            totalEarlyLeave += record.getEarlyLeaveMinutes();
            
            if (AttendanceRecord.AttendanceStatus.PAID_LEAVE.equals(record.getAttendanceStatus())) {
                paidLeaveDays++;
            } else if (AttendanceRecord.AttendanceStatus.ABSENT.equals(record.getAttendanceStatus())) {
                absentDays++;
            }
        }
        
        return AttendanceSummary.builder()
                .totalWorkingMinutes(totalWorking)
                .totalOvertimeMinutes(totalOvertime)
                .totalNightShiftMinutes(totalNightShift)
                .totalLateMinutes(totalLate)
                .totalEarlyLeaveMinutes(totalEarlyLeave)
                .paidLeaveDays(paidLeaveDays)
                .absentDays(absentDays)
                .build();
    }
}
