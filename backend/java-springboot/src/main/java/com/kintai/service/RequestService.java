package com.kintai.service;

import com.kintai.dto.AdjustmentRequestDto;
import com.kintai.dto.LeaveRequestDto;
import com.kintai.entity.AdjustmentRequest;
import com.kintai.entity.AttendanceRecord;
import com.kintai.entity.Employee;
import com.kintai.entity.LeaveRequest;
import com.kintai.exception.BusinessException;
import com.kintai.repository.AdjustmentRequestRepository;
import com.kintai.repository.AttendanceRecordRepository;
import com.kintai.repository.EmployeeRepository;
import com.kintai.repository.LeaveRequestRepository;
import com.kintai.util.TimeCalculator.AttendanceCalculationResult;
import com.kintai.util.TimeCalculator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * 申請処理サービス
 */
@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class RequestService {
    
    private final LeaveRequestRepository leaveRequestRepository;
    private final AdjustmentRequestRepository adjustmentRequestRepository;
    private final AttendanceRecordRepository attendanceRecordRepository;
    private final EmployeeRepository employeeRepository;
    private final TimeCalculator timeCalculator;
    
    /**
     * 有給申請
     */
    public LeaveRequest submitLeaveRequest(LeaveRequestDto dto) {
        // 残有給日数チェック
        Employee employee = employeeRepository.findById(dto.getEmployeeId())
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
        
        if (employee.getPaidLeaveRemainingDays() <= 0) {
            throw new BusinessException("INSUFFICIENT_LEAVE_DAYS", "有給残日数が不足しています");
        }
        
        // 重複申請チェック
        Optional<LeaveRequest> existing = leaveRequestRepository
                .findByEmployeeIdAndLeaveRequestDate(dto.getEmployeeId(), dto.getLeaveDate());
        
        if (existing.isPresent()) {
            throw new BusinessException("DUPLICATE_REQUEST", "既に申請済みです");
        }
        
        // 既に出勤済みの日はNG
        Optional<AttendanceRecord> attendanceRecord = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(dto.getEmployeeId(), dto.getLeaveDate());
        
        if (attendanceRecord.isPresent() && attendanceRecord.get().getClockInTime() != null) {
            throw new BusinessException("DUPLICATE_REQUEST", "既に出勤済みの日は申請できません");
        }
        
        // 申請作成
        LeaveRequest request = LeaveRequest.builder()
                .employeeId(dto.getEmployeeId())
                .leaveRequestDate(dto.getLeaveDate())
                .leaveRequestReason(dto.getReason())
                .leaveRequestStatus(LeaveRequest.LeaveRequestStatus.PENDING)
                .build();
        
        return leaveRequestRepository.save(request);
    }
    
    /**
     * 打刻修正申請
     */
    public AdjustmentRequest submitAdjustmentRequest(AdjustmentRequestDto dto) {
        // 確定済み勤怠データチェック
        Optional<AttendanceRecord> existingRecord = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(dto.getEmployeeId(), dto.getTargetDate());
        
        if (existingRecord.isPresent() && existingRecord.get().getAttendanceFixedFlag() == true) {
            throw new BusinessException("FIXED_ATTENDANCE", "確定済みのため変更できません");
        }
        
        // 重複申請チェック
        Optional<AdjustmentRequest> existing = adjustmentRequestRepository
                .findByEmployeeIdAndAdjustmentTargetDate(dto.getEmployeeId(), dto.getTargetDate());
        
        if (existing.isPresent() && existing.get().getAdjustmentStatus() == AdjustmentRequest.AdjustmentStatus.PENDING) {
            throw new BusinessException("DUPLICATE_REQUEST", "既に申請済みです");
        }
        
        // 元の打刻時刻取得
        LocalDateTime originalClockIn = null;
        LocalDateTime originalClockOut = null;
        if (existingRecord.isPresent()) {
            originalClockIn = existingRecord.get().getClockInTime();
            originalClockOut = existingRecord.get().getClockOutTime();
        }
        
        // 申請作成
        AdjustmentRequest request = AdjustmentRequest.builder()
                .employeeId(dto.getEmployeeId())
                .adjustmentTargetDate(dto.getTargetDate())
                .originalClockInTime(originalClockIn)
                .originalClockOutTime(originalClockOut)
                .adjustmentRequestedTimeIn(dto.getCorrectedClockInTime() != null ? 
                        dto.getTargetDate().atTime(dto.getCorrectedClockInTime()) : null)
                .adjustmentRequestedTimeOut(dto.getCorrectedClockOutTime() != null ? 
                        dto.getTargetDate().atTime(dto.getCorrectedClockOutTime()) : null)
                .adjustmentReason(dto.getReason())
                .adjustmentStatus(AdjustmentRequest.AdjustmentStatus.PENDING)
                .build();
        
        return adjustmentRequestRepository.save(request);
    }
    
    /**
     * 有給申請承認
     */
    public void approveLeaveRequest(Long requestId, Long approverId) {
        LeaveRequest request = leaveRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません"));
        
        if (request.getLeaveRequestStatus() != LeaveRequest.LeaveRequestStatus.PENDING) {
            throw new BusinessException("REQUEST_NOT_FOUND", "処理済みの申請です");
        }
        
        // 有給残日数減算
        Employee employee = employeeRepository.findById(request.getEmployeeId())
                .orElseThrow(() -> new BusinessException("EMPLOYEE_NOT_FOUND", "社員が見つかりません"));
        
        employee.setPaidLeaveRemainingDays(employee.getPaidLeaveRemainingDays() - 1);
        employeeRepository.save(employee);
        
        // 申請承認
        request.setLeaveRequestStatus(LeaveRequest.LeaveRequestStatus.APPROVED);
        request.setApprovedByEmployeeId(approverId);
        request.setApprovedAt(LocalDateTime.now());
        leaveRequestRepository.save(request);
        
        // attendance_records に有給記録作成
        AttendanceRecord attendanceRecord = AttendanceRecord.builder()
                .employeeId(request.getEmployeeId())
                .attendanceDate(request.getLeaveRequestDate())
                .attendanceStatus(AttendanceRecord.AttendanceStatus.PAID_LEAVE)
                .submissionStatus(AttendanceRecord.SubmissionStatus.NOT_SUBMITTED)
                .attendanceFixedFlag(false)
                .build();
        
        attendanceRecordRepository.save(attendanceRecord);
    }
    
    /**
     * 打刻修正申請承認
     */
    public void approveAdjustmentRequest(Long requestId, Long approverId) {
        AdjustmentRequest request = adjustmentRequestRepository.findById(requestId)
                .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません"));
        
        if (request.getAdjustmentStatus() != AdjustmentRequest.AdjustmentStatus.PENDING) {
            throw new BusinessException("REQUEST_NOT_FOUND", "処理済みの申請です");
        }
        
        // attendance_records 更新
        AttendanceRecord record = attendanceRecordRepository
                .findByEmployeeIdAndAttendanceDate(request.getEmployeeId(), request.getAdjustmentTargetDate())
                .orElse(AttendanceRecord.builder()
                        .employeeId(request.getEmployeeId())
                        .attendanceDate(request.getAdjustmentTargetDate())
                        .attendanceStatus(AttendanceRecord.AttendanceStatus.NORMAL)
                        .submissionStatus(AttendanceRecord.SubmissionStatus.NOT_SUBMITTED)
                        .attendanceFixedFlag(false)
                        .build());
        
        // 修正された時刻を適用
        if (request.getAdjustmentRequestedTimeIn() != null) {
            record.setClockInTime(request.getAdjustmentRequestedTimeIn());
        }
        if (request.getAdjustmentRequestedTimeOut() != null) {
            record.setClockOutTime(request.getAdjustmentRequestedTimeOut());
        }
        
        // 時間再計算（両方の時刻がある場合のみ）
        if (record.getClockInTime() != null && record.getClockOutTime() != null) {
            AttendanceCalculationResult calc = timeCalculator
                    .calculateAttendanceTimes(record.getClockInTime(), record.getClockOutTime());
            
            record.setLateMinutes(calc.getLateMinutes());
            record.setEarlyLeaveMinutes(calc.getEarlyLeaveMinutes());
            record.setOvertimeMinutes(calc.getOvertimeMinutes());
            record.setNightShiftMinutes(calc.getNightShiftMinutes());
        }
        
        attendanceRecordRepository.save(record);
        
        // 申請承認
        request.setAdjustmentStatus(AdjustmentRequest.AdjustmentStatus.APPROVED);
        request.setApprovedByEmployeeId(approverId);
        request.setApprovedAt(LocalDateTime.now());
        adjustmentRequestRepository.save(request);
    }
    
    /**
     * 申請却下
     */
    public void rejectRequest(Long requestId, Long approverId, String rejectionReason, String requestType) {
        if ("leave".equals(requestType)) {
            LeaveRequest request = leaveRequestRepository.findById(requestId)
                    .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません"));
            
            request.setLeaveRequestStatus(LeaveRequest.LeaveRequestStatus.REJECTED);
            leaveRequestRepository.save(request);
            
        } else if ("adjustment".equals(requestType)) {
            AdjustmentRequest request = adjustmentRequestRepository.findById(requestId)
                    .orElseThrow(() -> new BusinessException("REQUEST_NOT_FOUND", "申請が見つかりません"));
            
            request.setAdjustmentStatus(AdjustmentRequest.AdjustmentStatus.REJECTED);
            request.setRejectionReason(rejectionReason);
            adjustmentRequestRepository.save(request);
        }
    }
    
    /**
     * 申請一覧取得
     */
    public List<Object> getRequestList(String requestType, String status) {
        List<Object> result = new ArrayList<>();
        
        if ("leave".equals(requestType) || requestType == null) {
            List<LeaveRequest> leaveRequests = status != null ? 
                    leaveRequestRepository.findByLeaveRequestStatusOrderByCreatedAtAsc(convertToLeaveStatus(status).getValue()) :
                    leaveRequestRepository.findAllByOrderByCreatedAtDesc();
            result.addAll(leaveRequests);
        }
        
        if ("adjustment".equals(requestType) || requestType == null) {
            List<AdjustmentRequest> adjustmentRequests = status != null ?
                    adjustmentRequestRepository.findByAdjustmentStatusOrderByCreatedAtAsc(convertToAdjustmentStatus(status).getValue()) :
                    adjustmentRequestRepository.findAllByOrderByCreatedAtDesc();
            result.addAll(adjustmentRequests);
        }
        
        return result;
    }
    
    /**
     * 文字列をLeaveRequestStatusに変換
     */
    private LeaveRequest.LeaveRequestStatus convertToLeaveStatus(String status) {
        return switch (status) {
            case "未処理" -> LeaveRequest.LeaveRequestStatus.PENDING;
            case "承認" -> LeaveRequest.LeaveRequestStatus.APPROVED;
            case "却下" -> LeaveRequest.LeaveRequestStatus.REJECTED;
            default -> LeaveRequest.LeaveRequestStatus.PENDING;
        };
    }
    
    /**
     * 文字列をAdjustmentStatusに変換
     */
    private AdjustmentRequest.AdjustmentStatus convertToAdjustmentStatus(String status) {
        return switch (status) {
            case "未処理" -> AdjustmentRequest.AdjustmentStatus.PENDING;
            case "承認" -> AdjustmentRequest.AdjustmentStatus.APPROVED;
            case "却下" -> AdjustmentRequest.AdjustmentStatus.REJECTED;
            default -> AdjustmentRequest.AdjustmentStatus.PENDING;
        };
    }
}
