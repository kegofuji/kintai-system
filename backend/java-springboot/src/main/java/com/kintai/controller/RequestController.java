package com.kintai.controller;

import com.kintai.dto.AttendanceResponse;
import com.kintai.dto.RequestDto;
import com.kintai.entity.AdjustmentRequest;
import com.kintai.entity.Employee;
import com.kintai.entity.LeaveRequest;
import com.kintai.service.EmployeeService;
import com.kintai.service.RequestService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/requests")
@CrossOrigin(origins = "*", allowCredentials = "true")
public class RequestController {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestController.class);
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private EmployeeService employeeService;
    
    @PostMapping("/leave")
    public AttendanceResponse submitLeaveRequest(@Valid @RequestBody RequestDto.LeaveRequestDto requestDto, 
                                               HttpSession session) {
        Long employeeId = (Long) session.getAttribute("employeeId");
        
        if (employeeId == null) {
            return AttendanceResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        try {
            Long requestId = requestService.submitLeaveRequest(employeeId, requestDto.getLeaveDate(), requestDto.getReason());
            
            // 残有給日数取得
            Employee employee = employeeService.getEmployee(employeeId);
            
            Map<String, Object> data = new HashMap<>();
            data.put("leaveRequestId", requestId);
            data.put("remainingDays", employee.getPaidLeaveRemainingDays());
            
            return AttendanceResponse.success(data, "有給申請が完了しました");
            
        } catch (Exception e) {
            logger.error("Leave request submission error for employee: " + employeeId, e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @PostMapping("/adjustment")
    public AttendanceResponse submitAdjustmentRequest(@Valid @RequestBody RequestDto.AdjustmentRequestDto requestDto, 
                                                    HttpSession session) {
        Long employeeId = (Long) session.getAttribute("employeeId");
        
        if (employeeId == null) {
            return AttendanceResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        try {
            Long requestId = requestService.submitAdjustmentRequest(
                employeeId, 
                requestDto.getTargetDate(), 
                requestDto.getCorrectedClockInTime(),
                requestDto.getCorrectedClockOutTime(), 
                requestDto.getReason()
            );
            
            Map<String, Object> data = new HashMap<>();
            data.put("adjustmentRequestId", requestId);
            
            return AttendanceResponse.success(data, "打刻修正申請が完了しました");
            
        } catch (Exception e) {
            logger.error("Adjustment request submission error for employee: " + employeeId, e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @GetMapping("/list")
    public AttendanceResponse getRequestList(@RequestParam(required = false) String requestType,
                                           @RequestParam(required = false) String status,
                                           @RequestParam(required = false) Long employeeId,
                                           HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long sessionEmployeeId = (Long) session.getAttribute("employeeId");
        
        if (sessionEmployeeId == null) {
            return AttendanceResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました");
        }
        
        try {
            List<Map<String, Object>> requests = List.of();
            
            // 管理者は全社員、一般社員は自分の申請のみ
            Long targetEmployeeId = "admin".equals(role) ? employeeId : sessionEmployeeId;
            
            if ("leave".equals(requestType) || requestType == null) {
                List<LeaveRequest> leaveRequests = "admin".equals(role) ?
                    requestService.getLeaveRequests(status, targetEmployeeId) :
                    requestService.getLeaveRequestsByEmployee(sessionEmployeeId);
                
                List<Map<String, Object>> leaveRequestMaps = leaveRequests.stream()
                    .map(this::convertLeaveRequestToMap)
                    .collect(Collectors.toList());
                requests.addAll(leaveRequestMaps);
            }
            
            if ("adjustment".equals(requestType) || requestType == null) {
                List<AdjustmentRequest> adjustmentRequests = "admin".equals(role) ?
                    requestService.getAdjustmentRequests(status, targetEmployeeId) :
                    requestService.getAdjustmentRequestsByEmployee(sessionEmployeeId);
                
                List<Map<String, Object>> adjustmentRequestMaps = adjustmentRequests.stream()
                    .map(this::convertAdjustmentRequestToMap)
                    .collect(Collectors.toList());
                requests.addAll(adjustmentRequestMaps);
            }
            
            return AttendanceResponse.success(requests);
            
        } catch (Exception e) {
            logger.error("Get request list error", e);
            return AttendanceResponse.error("SYSTEM_ERROR", e.getMessage());
        }
    }
    
    @PostMapping("/approve/{requestId}")
    public AttendanceResponse approveRequest(@PathVariable Long requestId,
                                           @RequestParam String requestType,
                                           @Valid @RequestBody RequestDto.ApprovalDto approvalDto,
                                           HttpSession session) {
        String role = (String) session.getAttribute("role");
        Long approverId = (Long) session.getAttribute("employeeId");
        
        if (!"admin".equals(role)) {
            return Attendance        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Clock in error for employee: " + employeeId, e);
            throw new BusinessException("SYSTEM_ERROR", "出勤打刻中にエラーが発生しました");
        }
    }
    
    /**
     * 退勤打刻
     * @param employeeId 社員ID
     * @return 退勤記録
     */
    @Transactional
    public AttendanceRecord clockOut(Long employeeId) {
        LocalDate today = LocalDate.now(ZoneId.of("Asia/Tokyo"));
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
        
        try {
            Optional<AttendanceRecord> recordOpt = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, today);
            
            if (recordOpt.isEmpty() || recordOpt.get().getClockInTime() == null) {
                throw new BusinessException("NOT_CLOCKED_IN", "出勤打刻が必要です");
            }
            
            AttendanceRecord record = recordOpt.get();
            if (record.getClockOutTime() != null) {
                throw new BusinessException("ALREADY_CLOCKED_OUT", "本日は既に退勤打刻済みです");
            }
            
            // 確定済みデータのチェック
            if (record.getAttendanceFixedFlag()) {
                throw new BusinessException("FIXED_ATTENDANCE", "確定済みのため変更できません");
            }
            
            record.setClockOutTime(now);
            
            // 時間計算
            TimeCalculator.AttendanceCalculationResult result = 
                timeCalculator.calculateAttendanceTimes(record.getClockInTime(), now);
            
            record.setLateMinutes(result.getLateMinutes());
            record.setEarlyLeaveMinutes(result.getEarlyLeaveMinutes());
            record.setOvertimeMinutes(result.getOvertimeMinutes());
            record.setNightShiftMinutes(result.getNightShiftMinutes());
            
            record = attendanceRecordRepository.save(record);
            logger.info("Clock out successful. Employee: {}, Time: {}, Overtime: {} min", 
                       employeeId, now, result.getOvertimeMinutes());
            
            return record;
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Clock out error for employee: " + employeeId, e);
            throw new BusinessException("SYSTEM_ERROR", "退勤打刻中にエラーが発生しました");
        }
    }
    
    /**
     * 勤怠履歴取得
     * @param employeeId 社員ID
     * @param yearMonth 年月（YYYY-MM形式）
     * @return 勤怠記録リスト
     */
    public List<AttendanceRecord> getAttendanceHistory(Long employeeId, String yearMonth) {
        try {
            int year = dateUtil.extractYear(yearMonth);
            int month = dateUtil.extractMonth(yearMonth);
            
            return attendanceRecordRepository.findByEmployeeIdAndYearMonth(employeeId, year, month);
            
        } catch (Exception e) {
            logger.error("Get attendance history error for employee: " + employeeId + ", month: " + yearMonth, e);
            throw new BusinessException("SYSTEM_ERROR", "勤怠履歴の取得中にエラーが発生しました");
        }
    }
    
    /**
     * 月末申請
     * @param employeeId 社員ID
     * @param targetMonth 対象月（YYYY-MM形式）
     */
    @Transactional
    public void submitMonthlyAttendance(Long employeeId, String targetMonth) {
        try {
            // 打刻漏れチェック
            validateMonthlySubmission(employeeId, targetMonth);
            
            int year = dateUtil.extractYear(targetMonth);
            int month = dateUtil.extractMonth(targetMonth);
            
            List<AttendanceRecord> records = 
                attendanceRecordRepository.findByEmployeeIdAndYearMonth(employeeId, year, month);
            
            for (AttendanceRecord record : records) {
                if (!record.getAttendanceFixedFlag()) {
                    record.setSubmissionStatus("申請済");
                }
            }
            
            attendanceRecordRepository.saveAll(records);
            logger.info("Monthly attendance submitted. Employee: {}, Month: {}", employeeId, targetMonth);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Monthly submission error for employee: " + employeeId + ", month: " + targetMonth, e);
            throw new BusinessException("SYSTEM_ERROR", "月末申請中にエラーが発生しました");
        }
    }
    
    /**
     * 月末申請承認
     * @param employeeId 社員ID
     * @param targetMonth 対象月
     * @param approverId 承認者ID
     */
    @Transactional
    public void approveMonthlyAttendance(Long employeeId, String targetMonth, Long approverId) {
        try {
            int year = dateUtil.extractYear(targetMonth);
            int month = dateUtil.extractMonth(targetMonth);
            
            List<AttendanceRecord> records = 
                attendanceRecordRepository.findByEmployeeIdAndYearMonth(employeeId, year, month);
            
            for (AttendanceRecord record : records) {
                if ("申請済".equals(record.getSubmissionStatus())) {
                    record.setSubmissionStatus("承認");
                    record.setAttendanceFixedFlag(true);
                }
            }
            
            attendanceRecordRepository.saveAll(records);
            logger.info("Monthly attendance approved. Employee: {}, Month: {}, Approver: {}", 
                       employeeId, targetMonth, approverId);
            
        } catch (Exception e) {
            logger.error("Monthly approval error for employee: " + employeeId, e);
            throw new BusinessException("SYSTEM_ERROR", "月末申請承認中にエラーが発生しました");
        }
    }
    
    /**
     * 月末申請却下
     * @param employeeId 社員ID
     * @param targetMonth 対象月
     * @param approverId 承認者ID
     */
    @Transactional
    public void rejectMonthlyAttendance(Long employeeId, String targetMonth, Long approverId) {
        try {
            int year = dateUtil.extractYear(targetMonth);
            int month = dateUtil.extractMonth(targetMonth);
            
            List<AttendanceRecord> records = 
                attendanceRecordRepository.findByEmployeeIdAndYearMonth(employeeId, year, month);
            
            for (AttendanceRecord record : records) {
                if ("申請済".equals(record.getSubmissionStatus())) {
                    record.setSubmissionStatus("却下");
                }
            }
            
            attendanceRecordRepository.saveAll(records);
            logger.info("Monthly attendance rejected. Employee: {}, Month: {}, Approver: {}", 
                       employeeId, targetMonth, approverId);
            
        } catch (Exception e) {
            logger.error("Monthly rejection error for employee: " + employeeId, e);
            throw new BusinessException("SYSTEM_ERROR", "月末申請却下中にエラーが発生しました");
        }
    }
    
    /**
     * 月末申請前チェック
     */
    private void validateMonthlySubmission(Long employeeId, String targetMonth) {
        List<LocalDate> workingDays = dateUtil.getWorkingDays(targetMonth);
        
        for (LocalDate workingDay : workingDays) {
            Optional<AttendanceRecord> recordOpt = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, workingDay);
            
            if (recordOpt.isEmpty()) {
                throw new BusinessException("INCOMPLETE_ATTENDANCE", 
                    "打刻漏れがあります: " + dateUtil.formatDate(workingDay));
            }
            
            AttendanceRecord record = recordOpt.get();
            
            // 有給取得日は除外
            if (record.getAttendanceStatus() == AttendanceRecord.AttendanceStatus.paid_leave) {
                continue;
            }
            
            // 通常勤務日の場合、出勤・退勤時刻が必須
            if (record.getClockInTime() == null || record.getClockOutTime() == null) {
                throw new BusinessException("INCOMPLETE_ATTENDANCE", 
                    "出勤または退勤の打刻が不足しています: " + dateUtil.formatDate(workingDay));
            }
        }
        
        // 欠勤日がある場合は申請不可
        int year = dateUtil.extractYear(targetMonth);
        int month = dateUtil.extractMonth(targetMonth);
        
        List<AttendanceRecord> absentRecords = 
            attendanceRecordRepository.findByEmployeeIdAndYearMonthAndAttendanceStatus(
                employeeId, year, month, AttendanceRecord.AttendanceStatus.absent);
        
        if (!absentRecords.isEmpty()) {
            throw new BusinessException("INCOMPLETE_ATTENDANCE", "欠勤日があるため申請できません");
        }
    }
    
    /**
     * 勤怠データ検索（管理者用）
     */
    public List<AttendanceRecord> searchAttendanceRecords(LocalDate startDate, LocalDate endDate,
                                                         String employeeCode, String employeeName) {
        try {
            return attendanceRecordRepository.findAttendanceRecordsWithEmployeeInfo(
                startDate, endDate, employeeCode, employeeName);
        } catch (Exception e) {
            logger.error("Search attendance records error", e);
            throw new BusinessException("SYSTEM_ERROR", "勤怠データの検索中にエラーが発生しました");
        }
    }
}