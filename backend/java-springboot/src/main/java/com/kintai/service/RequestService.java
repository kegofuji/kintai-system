package com.kintai.service;

import com.kintai.entity.*;
import com.kintai.repository.*;
import com.kintai.util.TimeCalculator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class RequestService {
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private AdjustmentRequestRepository adjustmentRequestRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    @Autowired
    private TimeCalculator timeCalculator;
    
    /**
     * 有給申請
     */
    public RequestResult submitLeaveRequest(Long employeeId, LocalDate leaveDate, String reason) {
        try {
            // 社員存在・有給残日数チェック
            Optional<Employee> employeeOpt = employeeRepository.findById(employeeId);
            if (employeeOpt.isEmpty()) {
                return RequestResult.failure("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            Employee employee = employeeOpt.get();
            if (employee.getPaidLeaveRemainingDays() <= 0) {
                return RequestResult.failure("INSUFFICIENT_LEAVE_DAYS", "有給残日数が不足しています");
            }
            
            // 重複申請チェック
            if (leaveRequestRepository.existsByEmployeeIdAndLeaveRequestDate(employeeId, leaveDate)) {
                return RequestResult.failure("DUPLICATE_REQUEST", "既に申請済みです");
            }
            
            // 過去日・当日チェック
            if (!leaveDate.isAfter(LocalDate.now())) {
                return RequestResult.failure("INVALID_DATE", "申請日は明日以降を選択してください");
            }
            
            // 出勤済みチェック
            Optional<AttendanceRecord> attendanceOpt = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, leaveDate);
            if (attendanceOpt.isPresent() && attendanceOpt.get().hasClockInTime()) {
                return RequestResult.failure("ALREADY_WORKED", "既に出勤済みの日は申請できません");
            }
            
            // 申請作成
            LeaveRequest request = new LeaveRequest(employeeId, leaveDate, reason);
            LeaveRequest saved = leaveRequestRepository.save(request);
            
            return RequestResult.success(saved.getLeaveRequestId(), 
                employee.getPaidLeaveRemainingDays() - 1, "有給申請が完了しました");
            
        } catch (Exception e) {
            return RequestResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 打刻修正申請
     */
    public RequestResult submitAdjustmentRequest(Long employeeId, LocalDate targetDate, 
                                               LocalDateTime correctedClockIn, LocalDateTime correctedClockOut, 
                                               String reason) {
        try {
            // 社員存在チェック
            if (!employeeRepository.existsById(employeeId)) {
                return RequestResult.failure("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            // 未来日チェック
            if (targetDate.isAfter(LocalDate.now())) {
                return RequestResult.failure("INVALID_DATE", "対象日は当日または過去日を選択してください");
            }
            
            // 重複申請チェック（未処理のもの）
            if (adjustmentRequestRepository.existsPendingRequestByEmployeeIdAndTargetDate(employeeId, targetDate)) {
                return RequestResult.failure("DUPLICATE_REQUEST", "既に申請済みです");
            }
            
            // 勤怠データの確定状態チェック
            Optional<AttendanceRecord> recordOpt = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, targetDate);
            if (recordOpt.isPresent() && recordOpt.get().isFixed()) {
                return RequestResult.failure("FIXED_ATTENDANCE", "確定済みのため変更できません");
            }
            
            // 申請作成
            AdjustmentRequest request = new AdjustmentRequest(employeeId, targetDate, reason);
            
            // 元データを保存
            if (recordOpt.isPresent()) {
                AttendanceRecord record = recordOpt.get();
                request.setOriginalClockInTime(record.getClockInTime());
                request.setOriginalClockOutTime(record.getClockOutTime());
            }
            
            request.setAdjustmentRequestedTimeIn(correctedClockIn);
            request.setAdjustmentRequestedTimeOut(correctedClockOut);
            
            // 時刻の妥当性チェック
            if (correctedClockIn != null && correctedClockOut != null) {
                if (!correctedClockOut.isAfter(correctedClockIn)) {
                    return RequestResult.failure("INVALID_TIME", "退勤時刻は出勤時刻より後を指定してください");
                }
            }
            
            AdjustmentRequest saved = adjustmentRequestRepository.save(request);
            
            return RequestResult.success(saved.getAdjustmentRequestId(), null, "打刻修正申請が完了しました");
            
        } catch (Exception e) {
            return RequestResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 有給申請承認
     */
    public ApprovalResult approveLeaveRequest(Long requestId, Long approverId) {
        try {
            Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ApprovalResult.failure("REQUEST_NOT_FOUND", "申請が見つかりません");
            }
            
            LeaveRequest request = requestOpt.get();
            if (!request.isPending()) {
                return ApprovalResult.failure("INVALID_STATUS", "処理済みの申請です");
            }
            
            // 社員の有給残日数チェック
            Optional<Employee> employeeOpt = employeeRepository.findById(request.getEmployeeId());
            if (employeeOpt.isEmpty()) {
                return ApprovalResult.failure("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
            }
            
            Employee employee = employeeOpt.get();
            if (employee.getPaidLeaveRemainingDays() <= 0) {
                return ApprovalResult.failure("INSUFFICIENT_LEAVE_DAYS", "有給残日数が不足しています");
            }
            
            // 承認処理
            request.approve(approverId);
            leaveRequestRepository.save(request);
            
            // 有給残日数減算
            employee.setPaidLeaveRemainingDays(employee.getPaidLeaveRemainingDays() - 1);
            employeeRepository.save(employee);
            
            // 勤怠レコードに有給記録作成
            AttendanceRecord attendanceRecord = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(employee.getEmployeeId(), request.getLeaveRequestDate())
                .orElse(new AttendanceRecord(employee.getEmployeeId(), request.getLeaveRequestDate()));
            
            attendanceRecord.setAttendanceStatus(AttendanceRecord.AttendanceStatus.PAID_LEAVE);
            attendanceRecordRepository.save(attendanceRecord);
            
            return ApprovalResult.success("有給申請を承認しました");
            
        } catch (Exception e) {
            return ApprovalResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 有給申請却下
     */
    public ApprovalResult rejectLeaveRequest(Long requestId, Long approverId, String reason) {
        try {
            Optional<LeaveRequest> requestOpt = leaveRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ApprovalResult.failure("REQUEST_NOT_FOUND", "申請が見つかりません");
            }
            
            LeaveRequest request = requestOpt.get();
            if (!request.isPending()) {
                return ApprovalResult.failure("INVALID_STATUS", "処理済みの申請です");
            }
            
            request.reject(approverId);
            leaveRequestRepository.save(request);
            
            return ApprovalResult.success("有給申請を却下しました");
            
        } catch (Exception e) {
            return ApprovalResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 打刻修正申請承認
     */
    public ApprovalResult approveAdjustmentRequest(Long requestId, Long approverId) {
        try {
            Optional<AdjustmentRequest> requestOpt = adjustmentRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ApprovalResult.failure("REQUEST_NOT_FOUND", "申請が見つかりません");
            }
            
            AdjustmentRequest request = requestOpt.get();
            if (!request.isPending()) {
                return ApprovalResult.failure("INVALID_STATUS", "処理済みの申請です");
            }
            
            // 勤怠データの確定状態チェック
            Optional<AttendanceRecord> recordOpt = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), request.getAdjustmentTargetDate());
            
            if (recordOpt.isPresent() && recordOpt.get().isFixed()) {
                return ApprovalResult.failure("FIXED_ATTENDANCE", "確定済みのため変更できません");
            }
            
            // 承認処理
            request.approve(approverId);
            adjustmentRequestRepository.save(request);
            
            // 勤怠レコード更新
            AttendanceRecord record = recordOpt.orElse(
                new AttendanceRecord(request.getEmployeeId(), request.getAdjustmentTargetDate()));
            
            if (request.getAdjustmentRequestedTimeIn() != null) {
                record.setClockInTime(request.getAdjustmentRequestedTimeIn());
            }
            if (request.getAdjustmentRequestedTimeOut() != null) {
                record.setClockOutTime(request.getAdjustmentRequestedTimeOut());
            }
            
            // 時間の再計算
            if (record.hasClockInTime() && record.hasClockOutTime()) {
                TimeCalculator.AttendanceCalculationResult calculation = 
                    timeCalculator.calculateAttendanceTimes(record.getClockInTime(), record.getClockOutTime());
                
                record.setLateMinutes(calculation.getLateMinutes());
                record.setEarlyLeaveMinutes(calculation.getEarlyLeaveMinutes());
                record.setOvertimeMinutes(calculation.getOvertimeMinutes());
                record.setNightShiftMinutes(calculation.getNightShiftMinutes());
            }
            
            attendanceRecordRepository.save(record);
            
            return ApprovalResult.success("打刻修正申請を承認しました");
            
        } catch (Exception e) {
            return ApprovalResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 打刻修正申請却下
     */
    public ApprovalResult rejectAdjustmentRequest(Long requestId, Long approverId, String reason) {
        try {
            Optional<AdjustmentRequest> requestOpt = adjustmentRequestRepository.findById(requestId);
            if (requestOpt.isEmpty()) {
                return ApprovalResult.failure("REQUEST_NOT_FOUND", "申請が見つかりません");
            }
            
            AdjustmentRequest request = requestOpt.get();
            if (!request.isPending()) {
                return ApprovalResult.failure("INVALID_STATUS", "処理済みの申請です");
            }
            
            request.reject(approverId, reason);
            adjustmentRequestRepository.save(request);
            
            return ApprovalResult.success("打刻修正申請を却下しました");
            
        } catch (Exception e) {
            return ApprovalResult.failure("SYSTEM_ERROR", "システムエラーが発生しました");
        }
    }
    
    /**
     * 申請一覧取得（管理者用）
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getLeaveRequestList(LeaveRequest.LeaveRequestStatus status, Long employeeId, String employeeName) {
        return leaveRequestRepository.searchLeaveRequests(status, employeeId, employeeName);
    }
    
    @Transactional(readOnly = true)
    public List<AdjustmentRequest> getAdjustmentRequestList(AdjustmentRequest.AdjustmentStatus status, Long employeeId, String employeeName) {
        return adjustmentRequestRepository.searchAdjustmentRequests(status, employeeId, employeeName);
    }
    
    /**
     * 社員の申請履歴取得
     */
    @Transactional(readOnly = true)
    public List<LeaveRequest> getEmployeeLeaveHistory(Long employeeId) {
        return leaveRequestRepository.findByEmployeeIdOrderByLeaveRequestDateDesc(employeeId);
    }
    
    @Transactional(readOnly = true)
    public List<AdjustmentRequest> getEmployeeAdjustmentHistory(Long employeeId) {
        return adjustmentRequestRepository.findByEmployeeIdOrderByAdjustmentTargetDateDesc(employeeId);
    }
    
    /**
     * 申請結果クラス
     */
    public static class RequestResult {
        private final boolean success;
        private final Long requestId;
        private final Integer remainingDays;
        private final String message;
        private final String errorCode;
        
        private RequestResult(boolean success, Long requestId, Integer remainingDays, 
                            String message, String errorCode) {
            this.success = success;
            this.requestId = requestId;
            this.remainingDays = remainingDays;
            this.message = message;
            this.errorCode = errorCode;
        }
        
        public static RequestResult success(Long requestId, Integer remainingDays, String message) {
            return new RequestResult(true, requestId, remainingDays, message, null);
        }
        
        public static RequestResult failure(String errorCode, String message) {
            return new RequestResult(false, null, null, message, errorCode);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public Long getRequestId() { return requestId; }
        public Integer getRemainingDays() { return remainingDays; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
    }
    
    /**
     * 承認結果クラス
     */
    public static class ApprovalResult {
        private final boolean success;
        private final String message;
        private final String errorCode;
        
        private ApprovalResult(boolean success, String message, String errorCode) {
            this.success = success;
            this.message = message;
            this.errorCode = errorCode;
        }
        
        public static ApprovalResult success(String message) {
            return new ApprovalResult(true, message, null);
        }
        
        public static ApprovalResult failure(String errorCode, String message) {
            return new ApprovalResult(false, message, errorCode);
        }
        
        // Getters
        public boolean isSuccess() { return success; }
        public String getMessage() { return message; }
        public String getErrorCode() { return errorCode; }
    }
}