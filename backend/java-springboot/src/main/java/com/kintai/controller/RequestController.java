package com.kintai.controller;

import com.kintai.dto.*;
import com.kintai.entity.AdjustmentRequest;
import com.kintai.entity.LeaveRequest;
import com.kintai.service.AuthService;
import com.kintai.service.RequestService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/requests")
public class RequestController {
    
    @Autowired
    private RequestService requestService;
    
    @Autowired
    private AuthService authService;
    
    /**
     * 有給申請
     */
    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitLeaveRequest(
            @Valid @RequestBody LeaveRequestDto request,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        // 権限チェック（自分の申請のみ or 管理者）
        if (!sessionInfo.getEmployeeId().equals(request.getEmployeeId()) && 
            !"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "アクセス権限がありません"));
        }
        
        RequestService.RequestResult result = requestService.submitLeaveRequest(
            request.getEmployeeId(), request.getLeaveDate(), request.getReason());
        
        if (result.isSuccess()) {
            Map<String, Object> data = Map.of(
                "leaveRequestId", result.getRequestId(),
                "remainingDays", result.getRemainingDays()
            );
            
            return ResponseEntity.ok(ApiResponse.success(data, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * 打刻修正申請
     */
    @PostMapping("/adjustment")
    public ResponseEntity<ApiResponse<Map<String, Object>>> submitAdjustmentRequest(
            @Valid @RequestBody AdjustmentRequestDto request,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        // 権限チェック
        if (!sessionInfo.getEmployeeId().equals(request.getEmployeeId()) && 
            !"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "アクセス権限がありません"));
        }
        
        // 時刻をLocalDateTimeに変換
        LocalDateTime correctedClockIn = null;
        LocalDateTime correctedClockOut = null;
        
        if (request.getCorrectedClockInTime() != null) {
            correctedClockIn = request.getTargetDate().atTime(request.getCorrectedClockInTime());
        }
        if (request.getCorrectedClockOutTime() != null) {
            correctedClockOut = request.getTargetDate().atTime(request.getCorrectedClockOutTime());
        }
        
        RequestService.RequestResult result = requestService.submitAdjustmentRequest(
            request.getEmployeeId(), request.getTargetDate(), 
            correctedClockIn, correctedClockOut, request.getReason());
        
        if (result.isSuccess()) {
            Map<String, Object> data = Map.of(
                "adjustmentRequestId", result.getRequestId()
            );
            
            return ResponseEntity.ok(ApiResponse.success(data, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * 申請一覧取得（管理者用）
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<Object>>> getRequestList(
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId,
            @RequestParam(required = false) String employeeName,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        // 管理者権限チェック
        if (!"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "管理者権限が必要です"));
        }
        
        List<Object> requests = null;
        
        if ("leave".equals(requestType) || requestType == null) {
            LeaveRequest.LeaveRequestStatus leaveStatus = null;
            if (status != null) {
                try {
                    leaveStatus = LeaveRequest.LeaveRequestStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                    // 無効なステータスは無視
                }
            }
            
            List<LeaveRequest> leaveRequests = requestService.getLeaveRequestList(leaveStatus, employeeId, employeeName);
            requests = List.of(leaveRequests.toArray());
        } else if ("adjustment".equals(requestType)) {
            AdjustmentRequest.AdjustmentStatus adjStatus = null;
            if (status != null) {
                try {
                    adjStatus = AdjustmentRequest.AdjustmentStatus.valueOf(status);
                } catch (IllegalArgumentException e) {
                    // 無効なステータスは無視
                }
            }
            
            List<AdjustmentRequest> adjRequests = requestService.getAdjustmentRequestList(adjStatus, employeeId, employeeName);
            requests = List.of(adjRequests.toArray());
        }
        
        return ResponseEntity.ok(ApiResponse.success(requests));
    }
    
    /**
     * 申請承認
     */
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> approveRequest(
            @PathVariable Long requestId,
            @RequestParam String requestType,
            @Valid @RequestBody ApprovalRequestDto request,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        // 管理者権限チェック
        if (!"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "管理者権限が必要です"));
        }
        
        RequestService.ApprovalResult result;
        
        if ("leave".equals(requestType)) {
            result = requestService.approveLeaveRequest(requestId, request.getApproverId());
        } else if ("adjustment".equals(requestType)) {
            result = requestService.approveAdjustmentRequest(requestId, request.getApproverId());
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("INVALID_REQUEST_TYPE", "無効な申請種別です"));
        }
        
        if (result.isSuccess()) {
            Map<String, Object> data = Map.of(
                "requestId", requestId,
                "status", "approved",
                "approvedAt", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(ApiResponse.success(data, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * 申請却下
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<ApiResponse<Map<String, Object>>> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam String requestType,
            @Valid @RequestBody RejectionRequestDto request,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        // 管理者権限チェック
        if (!"admin".equals(sessionInfo.getRole())) {
            return ResponseEntity.status(403).body(
                ApiResponse.error("ACCESS_DENIED", "管理者権限が必要です"));
        }
        
        RequestService.ApprovalResult result;
        
        if ("leave".equals(requestType)) {
            result = requestService.rejectLeaveRequest(requestId, request.getApproverId(), request.getRejectionReason());
        } else if ("adjustment".equals(requestType)) {
            result = requestService.rejectAdjustmentRequest(requestId, request.getApproverId(), request.getRejectionReason());
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error("INVALID_REQUEST_TYPE", "無効な申請種別です"));
        }
        
        if (result.isSuccess()) {
            Map<String, Object> data = Map.of(
                "requestId", requestId,
                "status", "rejected",
                "rejectedAt", java.time.LocalDateTime.now()
            );
            
            return ResponseEntity.ok(ApiResponse.success(data, result.getMessage()));
        } else {
            return ResponseEntity.badRequest().body(
                ApiResponse.error(result.getErrorCode(), result.getMessage()));
        }
    }
    
    /**
     * 社員の申請履歴取得
     */
    @GetMapping("/my-history")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getMyRequestHistory(
            @RequestParam(required = false) String requestType,
            HttpSession session) {
        
        AuthService.SessionInfo sessionInfo = authService.getSessionInfo(session);
        if (sessionInfo == null) {
            return ResponseEntity.status(401).body(
                ApiResponse.error("SESSION_TIMEOUT", "セッションがタイムアウトしました"));
        }
        
        Map<String, Object> history = Map.of(
            "leaveRequests", requestService.getEmployeeLeaveHistory(sessionInfo.getEmployeeId()),
            "adjustmentRequests", requestService.getEmployeeAdjustmentHistory(sessionInfo.getEmployeeId())
        );
        
        return ResponseEntity.ok(ApiResponse.success(history));
    }
}