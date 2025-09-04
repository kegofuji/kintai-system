package com.kintai.service;

import com.kintai.entity.AdjustmentRequest;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.LeaveRequest;
import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.repository.AdjustmentRequestRepository;
import com.kintai.repository.AttendanceRecordRepository;
import com.kintai.repository.LeaveRequestRepository;
import com.kintai.repository.EmployeeRepository;
import com.kintai.util.TimeCalculator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
    
    private static final Logger log = LoggerFactory.getLogger(RequestService.class);
    
    @Autowired
    private LeaveRequestRepository leaveRequestRepository;
    
    @Autowired
    private AdjustmentRequestRepository adjustmentRequestRepository;
    
    @Autowired
    private AttendanceRecordRepository attendanceRecordRepository;
    
    @Autowired
    private EmployeeRepository employeeRepository;
    
    /**
     * 有給申請
     * 
     * @param employeeId 申請者ID
     * @param leaveDate 有給取得日（未来日）
     * @param reason 申請理由
     * @return 作成された有給申請エンティティ
     * @throws BusinessException 残日数不足、重複申請、既出勤日
     */
    public LeaveRequest submitLeaveRequest(Long employeeId, LocalDate leaveDate, String reason) {
        log.info("有給申請開始 - employeeId: {}, leaveDate: {}, reason: {}", 
                employeeId, leaveDate, reason);
        
        // 残日数チェック
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> {
                    log.error("申請者が見つからない - employeeId: {}", employeeId);
                    return new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
                });
        
        if (employee.getPaidLeaveRemainingDays() <= 0) {
            log.warn("有給残日数不足 - employeeId: {}, remainingDays: {}", 
                    employeeId, employee.getPaidLeaveRemainingDays());
            throw new BusinessException("INSUFFICIENT_LEAVE_DAYS", "有給残日数が不足しています");
        }
        
        // 重複申請チェック
        if (leaveRequestRepository.existsByEmployeeIdAndLeaveRequestDate(employeeId, leaveDate)) {
            log.warn("有給申請重複 - employeeId: {}, leaveDate: {}", employeeId, leaveDate);
            throw new BusinessException("DUPLICATE_REQUEST", "既に申請済みです");
        }
        
        // 既に出勤済みの日はチェック
        Optional<AttendanceRecord> existingAttendance = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, leaveDate);
        
        if (existingAttendance.isPresent() && existingAttendance.get().getClockInTime() != null) {
            log.warn("既出勤日への有給申請 - employeeId: {}, leaveDate: {}", employeeId, leaveDate);
            throw new BusinessException("ALREADY_WORKED", "既に出勤済みの日は申請できません");
        }
        
        LeaveRequest request = new LeaveRequest(employeeId, leaveDate, reason);
        LeaveRequest savedRequest = leaveRequestRepository.save(request);
        
        log.info("有給申請完了 - requestId: {}, employeeId: {}, leaveDate: {}", 
                savedRequest.getLeaveRequestId(), employeeId, leaveDate);
        
        return savedRequest;
    }
    
    /**
     * 有給申請承認
     * 
     * @param requestId 申請ID
     * @param approverId 承認者ID
     * @throws BusinessException 申請が見つからない、処理済み、社員が見つからない
     */
    public void approveLeaveRequest(Long requestId, Long approverId) {
        log.info("有給申請承認開始 - requestId: {}, approverId: {}", requestId, approverId);
        
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("有給申請が見つからない - requestId: {}", requestId);
                    return new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
                });
        
        if (!"未処理".equals(request.getLeaveRequestStatus())) {
            log.warn("有給申請既処理済み - requestId: {}, status: {}", 
                    requestId, request.getLeaveRequestStatus());
            throw new BusinessException("REQUEST_ALREADY_PROCESSED", "既に処理済みです");
        }
        
        // 有給残日数減算
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> {
                    log.error("申請者が見つからない（承認時） - employeeId: {}", request.getEmployeeId());
                    return new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません");
                });
        
        int beforeDays = employee.getPaidLeaveRemainingDays();
        employee.setPaidLeaveRemainingDays(beforeDays - 1);
        employeeRepository.save(employee);
        
        // 勤怠記録を有給として作成
        AttendanceRecord attendanceRecord = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), request.getLeaveRequestDate())
                .orElse(new AttendanceRecord(request.getEmployeeId(), request.getLeaveRequestDate()));
        
        attendanceRecord.setAttendanceStatus(AttendanceRecord.AttendanceStatus.paid_leave);
        attendanceRecordRepository.save(attendanceRecord);
        
        // 申請を承認済みに更新
        request.setLeaveRequestStatus("承認");
        request.setApprovedByEmployeeId(approverId);
        request.setApprovedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
        
        log.info("有給申請承認完了 - requestId: {}, employeeId: {}, 残日数: {}→{}", 
                requestId, request.getEmployeeId(), beforeDays, employee.getPaidLeaveRemainingDays());
    }
    
    /**
     * 有給申請却下
     * 
     * @param requestId 申請ID
     * @param approverId 承認者ID
     * @throws BusinessException 申請が見つからない、処理済み
     */
    public void rejectLeaveRequest(Long requestId, Long approverId) {
        log.info("有給申請却下開始 - requestId: {}, approverId: {}", requestId, approverId);
        
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("有給申請が見つからない（却下） - requestId: {}", requestId);
                    return new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
                });
        
        if (!"未処理".equals(request.getLeaveRequestStatus())) {
            log.warn("有給申請既処理済み（却下） - requestId: {}, status: {}", 
                    requestId, request.getLeaveRequestStatus());
            throw new BusinessException("REQUEST_ALREADY_PROCESSED", "既に処理済みです");
        }
        
        request.setLeaveRequestStatus("却下");
        request.setApprovedByEmployeeId(approverId);
        request.setApprovedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
        
        log.info("有給申請却下完了 - requestId: {}, employeeId: {}", 
                requestId, request.getEmployeeId());
    }
    
    /**
     * 打刻修正申請
     * 
     * @param employeeId 申請者ID
     * @param targetDate 修正対象日（当日または過去日）
     * @param correctedClockInTime 修正後出勤時刻
     * @param correctedClockOutTime 修正後退勤時刻
     * @param reason 修正理由
     * @return 作成された打刻修正申請エンティティ
     * @throws BusinessException 確定済み、重複申請
     */
    public AdjustmentRequest submitAdjustmentRequest(Long employeeId, LocalDate targetDate, 
                                                   LocalDateTime correctedClockInTime,
                                                   LocalDateTime correctedClockOutTime,
                                                   String reason) {
        log.info("打刻修正申請開始 - employeeId: {}, targetDate: {}, reason: {}", 
                employeeId, targetDate, reason);
        
        // 確定済み勤怠データはチェック
        Optional<AttendanceRecord> attendanceOpt = 
                attendanceRecordRepository.findByEmployeeIdAndAttendanceDate(employeeId, targetDate);
        
        if (attendanceOpt.isPresent() && attendanceOpt.get().getAttendanceFixedFlag()) {
            log.warn("確定済み勤怠データへの修正申請 - employeeId: {}, targetDate: {}", 
                    employeeId, targetDate);
            throw new BusinessException("FIXED_ATTENDANCE", "確定済みのため変更できません");
        }
        
        // 既存の修正申請チェック
        Optional<AdjustmentRequest> existingRequest = 
                adjustmentRequestRepository.findByEmployeeIdAndAdjustmentTargetDate(employeeId, targetDate);
        
        if (existingRequest.isPresent() && "未処理".equals(existingRequest.get().getAdjustmentStatus())) {
            log.warn("打刻修正申請重複 - employeeId: {}, targetDate: {}", employeeId, targetDate);
            throw new BusinessException("DUPLICATE_REQUEST", "既に申請済みです");
        }
        
        // 元の打刻時刻を保存
        LocalDateTime originalClockIn = null;
        LocalDateTime originalClockOut = null;
        if (attendanceOpt.isPresent()) {
            originalClockIn = attendanceOpt.get().getClockInTime();
            originalClockOut = attendanceOpt.get().getClockOutTime();
        }
        
        AdjustmentRequest request = new AdjustmentRequest(
                employeeId, targetDate, correctedClockInTime, correctedClockOutTime, reason);
        request.setOriginalClockInTime(originalClockIn);
        request.setOriginalClockOutTime(originalClockOut);
        
        AdjustmentRequest savedRequest = adjustmentRequestRepository.save(request);
        
        log.info("打刻修正申請完了 - requestId: {}, employeeId: {}, targetDate: {}", 
                savedRequest.getAdjustmentRequestId(), employeeId, targetDate);
        
        return savedRequest;
    }
    
    /**
     * 打刻修正申請承認
     * 
     * @param requestId 申請ID
     * @param approverId 承認者ID
     * @throws BusinessException 申請が見つからない、処理済み、確定済み
     */
    public void approveAdjustmentRequest(Long requestId, Long approverId) {
        log.info("打刻修正申請承認開始 - requestId: {}, approverId: {}", requestId, approverId);
        
        AdjustmentRequest request = adjustmentRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("打刻修正申請が見つからない - requestId: {}", requestId);
                    return new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
                });
        
        if (!"未処理".equals(request.getAdjustmentStatus())) {
            log.warn("打刻修正申請既処理済み - requestId: {}, status: {}", 
                    requestId, request.getAdjustmentStatus());
            throw new BusinessException("REQUEST_ALREADY_PROCESSED", "既に処理済みです");
        }
        
        // 勤怠記録を更新
        AttendanceRecord attendanceRecord = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(
                        request.getEmployeeId(), request.getAdjustmentTargetDate())
                .orElse(new AttendanceRecord(request.getEmployeeId(), request.getAdjustmentTargetDate()));
        
        // 確定済みチェック
        if (attendanceRecord.getAttendanceFixedFlag()) {
            log.error("確定済み勤怠データの修正申請承認試行 - requestId: {}, targetDate: {}", 
                    requestId, request.getAdjustmentTargetDate());
            throw new BusinessException("FIXED_ATTENDANCE", "確定済みのため変更できません");
        }
        
        // 修正時刻を設定
        LocalDateTime beforeClockIn = attendanceRecord.getClockInTime();
        LocalDateTime beforeClockOut = attendanceRecord.getClockOutTime();
        
        attendanceRecord.setClockInTime(request.getAdjustmentRequestedTimeIn());
        attendanceRecord.setClockOutTime(request.getAdjustmentRequestedTimeOut());
        
        // 勤怠時間再計算
        if (request.getAdjustmentRequestedTimeIn() != null && 
            request.getAdjustmentRequestedTimeOut() != null) {
            TimeCalculator.AttendanceCalculationResult result = 
                    TimeCalculator.calculateAttendanceTimes(
                            request.getAdjustmentRequestedTimeIn(), 
                            request.getAdjustmentRequestedTimeOut());
            
            attendanceRecord.setLateMinutes(result.getLateMinutes());
            attendanceRecord.setEarlyLeaveMinutes(result.getEarlyLeaveMinutes());
            attendanceRecord.setOvertimeMinutes(result.getOvertimeMinutes());
            attendanceRecord.setNightShiftMinutes(result.getNightShiftMinutes());
        } else {
            // 部分修正の場合は個別計算
            if (request.getAdjustmentRequestedTimeIn() != null) {
                int lateMinutes = TimeCalculator.calculateLateMinutes(request.getAdjustmentRequestedTimeIn());
                attendanceRecord.setLateMinutes(lateMinutes);
            }
            if (request.getAdjustmentRequestedTimeOut() != null && attendanceRecord.getClockInTime() != null) {
                int earlyLeaveMinutes = TimeCalculator.calculateEarlyLeaveMinutes(request.getAdjustmentRequestedTimeOut());
                attendanceRecord.setEarlyLeaveMinutes(earlyLeaveMinutes);
                
                int workingMinutes = TimeCalculator.calculateWorkingMinutes(
                        attendanceRecord.getClockInTime(), request.getAdjustmentRequestedTimeOut());
                int overtimeMinutes = TimeCalculator.calculateOvertimeMinutes(workingMinutes);
                attendanceRecord.setOvertimeMinutes(overtimeMinutes);
                
                int nightShiftMinutes = TimeCalculator.calculateNightShiftMinutes(
                        attendanceRecord.getClockInTime(), request.getAdjustmentRequestedTimeOut());
                attendanceRecord.setNightShiftMinutes(nightShiftMinutes);
            }
        }
        
        attendanceRecordRepository.save(attendanceRecord);
        
        // 申請を承認済みに更新
        request.setAdjustmentStatus("承認");
        request.setApprovedByEmployeeId(approverId);
        request.setApprovedAt(LocalDateTime.now());
        adjustmentRequestRepository.save(request);
        
        log.info("打刻修正申請承認完了 - requestId: {}, employeeId: {}, 修正前: [{},{}] → 修正後: [{},{}]", 
                requestId, request.getEmployeeId(), 
                beforeClockIn, beforeClockOut,
                attendanceRecord.getClockInTime(), attendanceRecord.getClockOutTime());
    }
    
    /**
     * 打刻修正申請却下
     * 
     * @param requestId 申請ID
     * @param approverId 承認者ID
     * @param rejectionReason 却下理由
     * @throws BusinessException 申請が見つからない、処理済み
     */
    public void rejectAdjustmentRequest(Long requestId, Long approverId, String rejectionReason) {
        log.info("打刻修正申請却下開始 - requestId: {}, approverId: {}, reason: {}", 
                requestId, approverId, rejectionReason);
        
        AdjustmentRequest request = adjustmentRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("打刻修正申請が見つからない（却下） - requestId: {}", requestId);
                    return new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
                });
        
        if (!"未処理".equals(request.getAdjustmentStatus())) {
            log.warn("打刻修正申請既処理済み（却下） - requestId: {}, status: {}", 
                    requestId, request.getAdjustmentStatus());
            throw new BusinessException("REQUEST_ALREADY_PROCESSED", "既に処理済みです");
        }
        
        request.setAdjustmentStatus("却下");
        request.setApprovedByEmployeeId(approverId);
        request.setApprovedAt(LocalDateTime.now());
        request.setRejectionReason(rejectionReason);
        adjustmentRequestRepository.save(request);
        
        log.info("打刻修正申請却下完了 - requestId: {}, employeeId: {}", 
                requestId, request.getEmployeeId());
    }
    
    /**
     * 有給申請一覧取得（フィルター付き）
     * 
     * @param employeeId 社員IDフィルター（null=全て）
     * @param status ステータスフィルター（null=全て）
     * @return 有給申請リスト
     */
    public List<LeaveRequest> getLeaveRequests(Long employeeId, String status) {
        log.debug("有給申請一覧取得 - employeeId: {}, status: {}", employeeId, status);
        return leaveRequestRepository.findLeaveRequestsWithFilters(employeeId, status);
    }
    
    /**
     * 打刻修正申請一覧取得（フィルター付き）
     * 
     * @param employeeId 社員IDフィルター（null=全て）
     * @param status ステータスフィルター（null=全て）
     * @return 打刻修正申請リスト
     */
    public List<AdjustmentRequest> getAdjustmentRequests(Long employeeId, String status) {
        log.debug("打刻修正申請一覧取得 - employeeId: {}, status: {}", employeeId, status);
        return adjustmentRequestRepository.findAdjustmentRequestsWithFilters(employeeId, status);
    }
    
    /**
     * 申請統計情報取得
     * 
     * @param employeeId 対象社員ID（null=全体）
     * @return 統計情報マップ
     */
    public Map<String, Object> getRequestStatistics(Long employeeId) {
        log.debug("申請統計情報取得 - employeeId: {}", employeeId);
        
        Map<String, Object> statistics = new HashMap<>();
        
        // 有給申請統計
        List<LeaveRequest> leaveRequests = getLeaveRequests(employeeId, null);
        long pendingLeaveRequests = leaveRequests.stream()
                .filter(req -> "未処理".equals(req.getLeaveRequestStatus()))
                .count();
        long approvedLeaveRequests = leaveRequests.stream()
                .filter(req -> "承認".equals(req.getLeaveRequestStatus()))
                .count();
        long rejectedLeaveRequests = leaveRequests.stream()
                .filter(req -> "却下".equals(req.getLeaveRequestStatus()))
                .count();
        
        Map<String, Object> leaveStats = new HashMap<>();
        leaveStats.put("total", leaveRequests.size());
        leaveStats.put("pending", pendingLeaveRequests);
        leaveStats.put("approved", approvedLeaveRequests);
        leaveStats.put("rejected", rejectedLeaveRequests);
        statistics.put("leaveRequests", leaveStats);
        
        // 打刻修正申請統計
        List<AdjustmentRequest> adjustmentRequests = getAdjustmentRequests(employeeId, null);
        long pendingAdjustmentRequests = adjustmentRequests.stream()
                .filter(req -> "未処理".equals(req.getAdjustmentStatus()))
                .count();
        long approvedAdjustmentRequests = adjustmentRequests.stream()
                .filter(req -> "承認".equals(req.getAdjustmentStatus()))
                .count();
        long rejectedAdjustmentRequests = adjustmentRequests.stream()
                .filter(req -> "却下".equals(req.getAdjustmentStatus()))
                .count();
        
        Map<String, Object> adjustmentStats = new HashMap<>();
        adjustmentStats.put("total", adjustmentRequests.size());
        adjustmentStats.put("pending", pendingAdjustmentRequests);
        adjustmentStats.put("approved", approvedAdjustmentRequests);
        adjustmentStats.put("rejected", rejectedAdjustmentRequests);
        statistics.put("adjustmentRequests", adjustmentStats);
        
        return statistics;
    }
    
    /**
     * 申請の詳細情報取得（有給申請）
     * 
     * @param requestId 申請ID
     * @return 有給申請の詳細情報
     * @throws BusinessException 申請が見つからない
     */
    public Map<String, Object> getLeaveRequestDetail(Long requestId) {
        log.debug("有給申請詳細取得 - requestId: {}", requestId);
        
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("有給申請が見つからない（詳細取得） - requestId: {}", requestId);
                    return new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
                });
        
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("requestId", request.getLeaveRequestId());
        detail.put("employeeId", request.getEmployeeId());
        detail.put("employeeName", employee.getEmployeeName());
        detail.put("employeeCode", employee.getEmployeeCode());
        detail.put("leaveDate", request.getLeaveRequestDate());
        detail.put("reason", request.getLeaveRequestReason());
        detail.put("status", request.getLeaveRequestStatus());
        detail.put("createdAt", request.getCreatedAt());
        
        if (request.getApprovedByEmployeeId() != null) {
            Employee approver = employeeRepository.findById(request.getApprovedByEmployeeId())
                    .orElse(null);
            if (approver != null) {
                detail.put("approverName", approver.getEmployeeName());
                detail.put("approvedAt", request.getApprovedAt());
            }
        }
        
        return detail;
    }
    
    /**
     * 申請の詳細情報取得（打刻修正申請）
     * 
     * @param requestId 申請ID
     * @return 打刻修正申請の詳細情報
     * @throws BusinessException 申請が見つからない
     */
    public Map<String, Object> getAdjustmentRequestDetail(Long requestId) {
        log.debug("打刻修正申請詳細取得 - requestId: {}", requestId);
        
        AdjustmentRequest request = adjustmentRequestRepository.findById(requestId)
                .orElseThrow(() -> {
                    log.error("打刻修正申請が見つからない（詳細取得） - requestId: {}", requestId);
                    return new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません");
                });
        
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
        
        Map<String, Object> detail = new HashMap<>();
        detail.put("requestId", request.getAdjustmentRequestId());
        detail.put("employeeId", request.getEmployeeId());
        detail.put("employeeName", employee.getEmployeeName());
        detail.put("employeeCode", employee.getEmployeeCode());
        detail.put("targetDate", request.getAdjustmentTargetDate());
        detail.put("originalClockInTime", request.getOriginalClockInTime());
        detail.put("originalClockOutTime", request.getOriginalClockOutTime());
        detail.put("requestedClockInTime", request.getAdjustmentRequestedTimeIn());
        detail.put("requestedClockOutTime", request.getAdjustmentRequestedTimeOut());
        detail.put("reason", request.getAdjustmentReason());
        detail.put("status", request.getAdjustmentStatus());
        detail.put("createdAt", request.getCreatedAt());
        
        if (request.getApprovedByEmployeeId() != null) {
            Employee approver = employeeRepository.findById(request.getApprovedByEmployeeId())
                    .orElse(null);
            if (approver != null) {
                detail.put("approverName", approver.getEmployeeName());
                detail.put("approvedAt", request.getApprovedAt());
            }
        }
        
        if (request.getRejectionReason() != null) {
            detail.put("rejectionReason", request.getRejectionReason());
        }
        
        return detail;
    }
    
    /**
     * 申請のキャンセル（未処理のみ）
     * 
     * @param requestId 申請ID
     * @param requestType 申請タイプ（"leave" or "adjustment"）
     * @param employeeId キャンセル実行者ID（本人チェック用）
     * @throws BusinessException 申請が見つからない、処理済み、権限なし
     */
    public void cancelRequest(Long requestId, String requestType, Long employeeId) {
        log.info("申請キャンセル開始 - requestId: {}, type: {}, employeeId: {}", 
                requestId, requestType, employeeId);
        
        if ("leave".equals(requestType)) {
            LeaveRequest request = leaveRequestRepository.findById(requestId)
                    .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません"));
            
            if (!request.getEmployeeId().equals(employeeId)) {
                throw new BusinessException("ACCESS_DENIED", "自分の申請のみキャンセルできます");
            }
            
            if (!"未処理".equals(request.getLeaveRequestStatus())) {
                throw new BusinessException("REQUEST_ALREADY_PROCESSED", "処理済みの申請はキャンセルできません");
            }
            
            leaveRequestRepository.delete(request);
            log.info("有給申請キャンセル完了 - requestId: {}", requestId);
            
        } else if ("adjustment".equals(requestType)) {
            AdjustmentRequest request = adjustmentRequestRepository.findById(requestId)
                    .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません"));
            
            if (!request.getEmployeeId().equals(employeeId)) {
                throw new BusinessException("ACCESS_DENIED", "自分の申請のみキャンセルできます");
            }
            
            if (!"未処理".equals(request.getAdjustmentStatus())) {
                throw new BusinessException("REQUEST_ALREADY_PROCESSED", "処理済みの申請はキャンセルできません");
            }
            
            adjustmentRequestRepository.delete(request);
            log.info("打刻修正申請キャンセル完了 - requestId: {}", requestId);
            
        } else {
            throw new BusinessException("INVALID_REQUEST_TYPE", "不正な申請タイプです");
        }
    }
}