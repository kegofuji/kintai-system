package com.kintai.service;

import com.kintai.entity.AdjustmentRequest;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.entity.LeaveRequest;
import com.kintai.exception.BusinessException;
import com.kintai.repository.AdjustmentRequestRepository;
import com.kintai.repository.AttendanceRecordRepository;
import com.kintai.repository.EmployeeRepository;
import com.kintai.repository.LeaveRequestRepository;
import com.kintai.util.DateUtil;
import com.kintai.util.TimeCalculator;
import com.kintai.util.ValidationUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

@Service
public class RequestService {
    
    private static final Logger logger = LoggerFactory.getLogger(RequestService.class);
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private AdjustmentRequestRepository adjustmentRequestRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private ValidationUtil validationUtil;
    
    @Autowired
    private DateUtil dateUtil;
    
    @Autowired
    private TimeCalculator timeCalculator;
    
    /**
     * 有給申請
     * @param employeeId 申請者ID
     * @param leaveDate 有給取得日
     * @param reason 理由
     * @return 申請ID
     */
    @Transactional
    public Long submitLeaveRequest(Long employeeId, LocalDate leaveDate, String reason) {
        try {
            // バリデーション
            validationUtil.validateFutureDate(leaveDate, "有給取得日");
            validationUtil.validateRequired(reason, "理由");
            validationUtil.validateMaxLength(reason, 200, "理由");
            
            // 社員存在確認
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                throw new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            Employee employee = employeeOpt.get();
            
            // 有給残日数チェック
            if (employee.getPaidLeaveRemainingDays() < 1) {
                throw new BusinessException("INSUFFICIENT_LEAVE_DAYS", "有給残日数が不足しています");
            }
            
            // 重複申請チェック
            if (leaveRequestRepository.existsByEmployeeIdAndLeaveRequestDate(employeeId, leaveDate)) {
                throw new BusinessException("DUPLICATE_REQUEST", "既に同じ日付で有給申請済みです");
            }
            
            // 既に出勤済みの日はNG
            Optional<AttendanceRecord> attendanceOpt = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, leaveDate);
            if (attendanceOpt.isPresent() && attendanceOpt.get().getClockInTime() != null) {
                throw new BusinessException("DUPLICATE_REQUEST", "既に出勤済みの日は有給申請できません");
            }
            
            // 申請作成
            LeaveRequest request = new LeaveRequest(employeeId, leaveDate, reason);
            request = leaveRequestRepository.save(request);
            
            logger.info("Leave request submitted. Employee: {}, Date: {}, Request ID: {}", 
                       employeeId, leaveDate, request.getLeaveRequestId());
            
            return request.getLeaveRequestId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Leave request submission error for employee: " + employeeId, e);
            throw new BusinessException("SYSTEM_ERROR", "有給申請中にエラーが発生しました");
        }
    }
    
    /**
     * 打刻修正申請
     * @param employeeId 申請者ID
     * @param targetDate 対象日
     * @param correctedClockInTime 修正後出勤時刻
     * @param correctedClockOutTime 修正後退勤時刻
     * @param reason 理由
     * @return 申請ID
     */
    @Transactional
    public Long submitAdjustmentRequest(Long employeeId, LocalDate targetDate,
                                       LocalTime correctedClockInTime, LocalTime correctedClockOutTime,
                                       String reason) {
        try {
            // バリデーション
            validationUtil.validatePastOrTodayDate(targetDate, "対象日");
            validationUtil.validateRequired(reason, "理由");
            validationUtil.validateMaxLength(reason, 200, "理由");
            
            // 出勤・退勤時刻のいずれか必須
            if (correctedClockInTime == null && correctedClockOutTime == null) {
                throw new BusinessException("VALIDATION_ERROR", "出勤時刻または退勤時刻のいずれかは必須です");
            }
            
            // 時刻の整合性チェック
            if (correctedClockInTime != null && correctedClockOutTime != null && 
                !correctedClockInTime.isBefore(correctedClockOutTime)) {
                throw new BusinessException("VALIDATION_ERROR", "退勤時刻は出勤時刻より後の時刻を入力してください");
            }
            
            // 既存の勤怠記録取得
            Optional<AttendanceRecord> attendanceOpt = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, targetDate);
            
            // 確定済みデータは修正不可
            if (attendanceOpt.isPresent() && attendanceOpt.get().getAttendanceFixedFlag()) {
                throw new BusinessException("FIXED_ATTENDANCE", "確定済みのため修正申請できません");
            }
            
            // 重複申請チェック
            if (adjustmentRequestRepository.existsByEmployeeIdAndAdjustmentTargetDate(employeeId, targetDate)) {
                throw new BusinessException("DUPLICATE_REQUEST", "既に同じ日付で修正申請済みです");
            }
            
            // 申請作成
            LocalDateTime correctedInDateTime = correctedClockInTime != null ? 
                targetDate.atTime(correctedClockInTime) : null;
            LocalDateTime correctedOutDateTime = correctedClockOutTime != null ? 
                targetDate.atTime(correctedClockOutTime) : null;
            
            AdjustmentRequest request = new AdjustmentRequest(employeeId, targetDate, 
                correctedInDateTime, correctedOutDateTime, reason);
            
            // 元の時刻を保存
            if (attendanceOpt.isPresent()) {
                AttendanceRecord record = attendanceOpt.get();
                request.setOriginalClockInTime(record.getClockInTime());
                request.setOriginalClockOutTime(record.getClockOutTime());
            }
            
            request = adjustmentRequestRepository.save(request);
            
            logger.info("Adjustment request submitted. Employee: {}, Date: {}, Request ID: {}", 
                       employeeId, targetDate, request.getAdjustmentRequestId());
            
            return request.getAdjustmentRequestId();
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Adjustment request submission error for employee: " + employeeId, e);
            throw new BusinessException("SYSTEM_ERROR", "打刻修正申請中にエラーが発生しました");
        }
    }
    
    /**
     * 有給申請承認
     * @param requestId 申請ID
     * @param approverId 承認者ID
     */
    @Transactional
    public void approveLeaveRequest(Long requestId, Long approverId) {
        try {
            Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                throw new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
            }
            
            LeaveRequest request = requestOpt.get();
            if (!"未処理".equals(request.getLeaveRequestStatus())) {
                throw new BusinessException("REQUEST_ALREADY_PROCESSED", "既に処理済みの申請です");
            }
            
            // 社員の有給残日数チェック
            Optional<Employee> employeeOpt = employeeRepository.findById(request.getEmployeeId());
            if (employeeOpt.isEmpty()) {
                throw new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            Employee employee = employeeOpt.get();
            if (employee.getPaidLeaveRemainingDays() < 1) {
                throw new BusinessException("INSUFFICIENT_LEAVE_DAYS", "有給残日数が不足しています");
            }
            
            // 承認処理
            request.setLeaveRequestStatus("承認");
            request.setApprovedByEmployeeId(approverId);
            request.setApprovedAt(LocalDateTime.now());
            leaveRequestRepository.save(request);
            
            // 有給残日数を減算
            employee.setPaidLeaveRemainingDays(employee.getPaidLeaveRemainingDays() - 1);
            employeeRepository.save(employee);
            
            // 勤怠記録を有給として作成/更新
            createOrUpdatePaidLeaveRecord(request.getEmployeeId(), request.getLeaveRequestDate());
            
            logger.info("Leave request approved. Request ID: {}, Employee: {}, Approver: {}", 
                       requestId, request.getEmployeeId(), approverId);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Leave request approval error for request: " + requestId, e);
            throw new BusinessException("SYSTEM_ERROR", "有給申請承認中にエラーが発生しました");
        }
    }
    
    /**
     * 有給申請却下
     * @param requestId 申請ID
     * @param approverId 承認者ID
     */
    @Transactional
    public void rejectLeaveRequest(Long requestId, Long approverId) {
        try {
            Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                throw new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
            }
            
            LeaveRequest request = requestOpt.get();
            if (!"未処理".equals(request.getLeaveRequestStatus())) {
                throw new BusinessException("REQUEST_ALREADY_PROCESSED", "既に処理済みの申請です");
            }
            
            // 却下処理
            request.setLeaveRequestStatus("却下");
            request.setApprovedByEmployeeId(approverId);
            request.setApprovedAt(LocalDateTime.now());
            leaveRequestRepository.save(request);
            
            logger.info("Leave request rejected. Request ID: {}, Employee: {}, Approver: {}", 
                       requestId, request.getEmployeeId(), approverId);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Leave request rejection error for request: " + requestId, e);
            throw new BusinessException("SYSTEM_ERROR", "有給申請却下中にエラーが発生しました");
        }
    }
    
    /**
     * 打刻修正申請承認
     * @param requestId 申請ID
     * @param approverId 承認者ID
     */
    @Transactional
    public void approveAdjustmentRequest(Long requestId, Long approverId) {
        try {
            Optional<AdjustmentRequest> requestOpt = adjustmentRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                throw new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
            }
            
            AdjustmentRequest request = requestOpt.get();
            if (!"未処理".equals(request.getAdjustmentStatus())) {
                throw new BusinessException("REQUEST_ALREADY_PROCESSED", "既に処理済みの申請です");
            }
            
            // 勤怠記録の修正
            updateAttendanceRecord(request);
            
            // 承認処理
            request.setAdjustmentStatus("承認");
            request.setApprovedByEmployeeId(approverId);
            request.setApprovedAt(LocalDateTime.now());
            adjustmentRequestRepository.save(request);
            
            logger.info("Adjustment request approved. Request ID: {}, Employee: {}, Approver: {}", 
                       requestId, request.getEmployeeId(), approverId);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Adjustment request approval error for request: " + requestId, e);
            throw new BusinessException("SYSTEM_ERROR", "打刻修正申請承認中にエラーが発生しました");
        }
    }
    
    /**
     * 打刻修正申請却下
     * @param requestId 申請ID
     * @param approverId 承認者ID
     * @param rejectionReason 却下理由
     */
    @Transactional
    public void rejectAdjustmentRequest(Long requestId, Long approverId, String rejectionReason) {
        try {
            Optional<AdjustmentRequest> requestOpt = adjustmentRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                throw new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
            }
            
            AdjustmentRequest request = requestOpt.get();
            if (!"未処理".equals(request.getAdjustmentStatus())) {
                throw new BusinessException("REQUEST_ALREADY_PROCESSED", "既に処理済みの申請です");
            }
            
            // 却下処理
            request.setAdjustmentStatus("却下");
            request.setApprovedByEmployeeId(approverId);
            request.setApprovedAt(LocalDateTime.now());
            request.setRejectionReason(rejectionReason);
            adjustmentRequestRepository.save(request);
            
            logger.info("Adjustment request rejected. Request ID: {}, Employee: {}, Approver: {}", 
                       requestId, request.getEmployeeId(), approverId);
            
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Adjustment request rejection error for request: " + requestId, e);
            throw new BusinessException("SYSTEM_ERROR", "打刻修正申請却下中にエラーが発生しました");
        }
    }
    
    /**
     * 申請一覧取得（管理者用）
     */
    public List<LeaveRequest> getLeaveRequests(String status, Long employeeId) {
        return leaveRequestRepository.findRequestsWithEmployeeInfo(status, employeeId);
    }
    
    public List<AdjustmentRequest> getAdjustmentRequests(String status, Long employeeId) {
        return adjustmentRequestRepository.findRequestsWithEmployeeInfo(status, employeeId);
    }
    
    /**
     * 申請一覧取得（社員用）
     */
    public List<LeaveRequest> getLeaveRequestsByEmployee(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByDateDesc(employeeId);
    }
    
    public List<AdjustmentRequest> getAdjustmentRequestsByEmployee(Long employeeId) {
        return adjustmentRequestRepository.findByEmployeeIdOrderByDateDesc(employeeId);
    }
    
    /**
     * 有給記録の作成/更新
     */
    private void createOrUpdatePaidLeaveRecord(Long employeeId, LocalDate leaveDate) {
        Optional<AttendanceRecord> recordOpt = 
            attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, leaveDate);
        
        AttendanceRecord record;
        if (recordOpt.isPresent()) {
            record = recordOpt.get();
        } else {
            record = new AttendanceRecord(employeeId, leaveDate);
        }
        
        record.setAttendanceStatus(AttendanceRecord.AttendanceStatus.paid_leave);
        record.setClockInTime(null);
        record.setClockOutTime(null);
        record.setLateMinutes(0);
        record.setEarlyLeaveMinutes(0);
        record.setOvertimeMinutes(0);
        record.setNightShiftMinutes(0);
        
        attendanceRecordRepository.save(record);
    }
    
    /**
     * 勤怠記録の修正
     */
    private void updateAttendanceRecord(AdjustmentRequest request) {
        Optional<AttendanceRecord> recordOpt = 
            attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(
                request.getEmployeeId(), request.getAdjustmentTargetDate());
        
        AttendanceRecord record;
        if (recordOpt.isPresent()) {
            record = recordOpt.get();
        } else {
            record = new AttendanceRecord(request.getEmployeeId(), request.getAdjustmentTargetDate());
        }
        
        // 確定済みは修正不可
        if (record.getAttendanceFixedFlag()) {
            throw new BusinessException("FIXED_ATTENDANCE", "確定済みのため変更できません");
        }
        
        // 修正時刻を適用
        if (request.getAdjustmentRequestedTimeIn() != null) {
            record.setClockInTime(request.getAdjustmentRequestedTimeIn());
        }
        if (request.getAdjustmentRequestedTimeOut() != null) {
            record.setClockOutTime(request.getAdjustmentRequestedTimeOut());
        }
        
        // 時間を再計算
        if (record.getClockInTime() != null && record.getClockOutTime() != null) {
            TimeCalculator.AttendanceCalculationResult result = 
                timeCalculator.calculateAttendanceTimes(record.getClockInTime(), record.getClockOutTime());
            
            record.setLateMinutes(result.getLateMinutes());
            record.setEarlyLeaveMinutes(result.getEarlyLeaveMinutes());
            record.setOvertimeMinutes(result.getOvertimeMinutes());
            record.setNightShiftMinutes(result.getNightShiftMinutes());
        }
        
        attendanceRecordRepository.save(record);
    }
}