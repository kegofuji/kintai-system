package com.kintai.controller;

import com.kintai.dto.AdjustmentRequestDto;
import com.kintai.dto.LeaveRequestDto;
import com.kintai.dto.common.ApiResponse;
import com.kintai.entity.AdjustmentRequest;
import com.kintai.entity.LeaveRequest;
import com.kintai.exception.BusinessException;
import com.kintai.service.RequestService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 申請APIコントローラー
 * 有給申請・打刻修正申請・承認処理
 */
@RestController
@RequestMapping("/api/requests")
@RequiredArgsConstructor
@Slf4j
public class RequestController {
    
    private final RequestService requestService;
    
    /**
     * POST /api/requests/leave - 有給申請
     */
    @PostMapping("/leave")
    public ResponseEntity<ApiResponse<Object>> submitLeaveRequest(@Valid @RequestBody LeaveRequestDto request) {
        try {
            LeaveRequest result = requestService.submitLeaveRequest(request);
            
            Map<String, Object> data = new HashMap<>();
            data.put("leaveRequestId", result.getLeaveRequestId());
            // 残日数は別途取得が必要
            data.put("message", "有給申請が完了しました");
            
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/requests/adjustment - 打刻修正申請
     */
    @PostMapping("/adjustment")
    public ResponseEntity<ApiResponse<Object>> submitAdjustmentRequest(@Valid @RequestBody AdjustmentRequestDto request) {
        try {
            AdjustmentRequest result = requestService.submitAdjustmentRequest(request);
            
            Map<String, Object> data = new HashMap<>();
            data.put("adjustmentRequestId", result.getAdjustmentRequestId());
            data.put("message", "打刻修正申請が完了しました");
            
            return ResponseEntity.ok(ApiResponse.success(data));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * GET /api/requests/list - 申請一覧取得（管理者用）
     */
    @GetMapping("/list")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<List<Object>>> getRequestList(
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId) {
        
        try {
            List<Object> requests = requestService.getRequestList(requestType, status);
            return ResponseEntity.ok(ApiResponse.success(requests));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/requests/approve/{requestId} - 申請承認
     */
    @PostMapping("/approve/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> approveRequest(
            @PathVariable Long requestId,
            @RequestParam String requestType,
            @RequestParam Long approverId) {
        
        try {
            if ("leave".equals(requestType)) {
                requestService.approveLeaveRequest(requestId, approverId);
            } else if ("adjustment".equals(requestType)) {
                requestService.approveAdjustmentRequest(requestId, approverId);
            }
            
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "approved");
            data.put("approvedAt", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(ApiResponse.success(data, "申請を承認しました"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
    
    /**
     * POST /api/requests/reject/{requestId} - 申請却下
     */
    @PostMapping("/reject/{requestId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<Object>> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam String requestType,
            @RequestParam Long approverId,
            @RequestBody Map<String, String> body) {
        
        String rejectionReason = body.get("rejectionReason");
        
        try {
            requestService.rejectRequest(requestId, approverId, rejectionReason, requestType);
            
            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "rejected");
            data.put("rejectedAt", LocalDateTime.now().toString());
            
            return ResponseEntity.ok(ApiResponse.success(data, "申請を却下しました"));
        } catch (BusinessException e) {
            return ResponseEntity.badRequest().body(
                    ApiResponse.error(e.getErrorCode(), e.getMessage())
            );
        }
    }
}
