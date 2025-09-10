package com.kintai.controller;

import com.kintai.dto.RequestDto;
import com.kintai.entity.AdjustmentRequest;
import com.kintai.entity.Employee;
import com.kintai.entity.LeaveRequest;
import com.kintai.exception.BusinessException;
import com.kintai.service.AuthService;
import com.kintai.service.EmployeeService;
import com.kintai.service.RequestService;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/requests")
public class RequestController {

    @Autowired
    private RequestService requestService;

    @Autowired
    private AuthService authService;

    @Autowired
    private EmployeeService employeeService;

    /**
     * 有給申請
     */
    @PostMapping("/leave")
    public ResponseEntity<Map<String, Object>> submitLeaveRequest(
            @Valid @RequestBody RequestDto.LeaveRequestDto request,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);

            // 権限チェック（本人のみ）
            if (!currentEmployee.getEmployeeId().equals(request.getEmployeeId())) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }

            LeaveRequest leaveRequest = requestService.submitLeaveRequest(
                    request.getEmployeeId(), request.getLeaveDate(), request.getReason());

            // 更新後の残日数取得
            Employee employee = employeeService.getEmployeeById(request.getEmployeeId());

            Map<String, Object> data = new HashMap<>();
            data.put("leaveRequestId", leaveRequest.getLeaveRequestId());
            data.put("remainingDays", employee.getPaidLeaveRemainingDays());

            response.put("success", true);
            response.put("data", data);
            response.put("message", String.format("有給申請が完了しました（残り%d日）",
                    employee.getPaidLeaveRemainingDays()));

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 打刻修正申請
     */
    @PostMapping("/adjustment")
    public ResponseEntity<Map<String, Object>> submitAdjustmentRequest(
            @Valid @RequestBody RequestDto.AdjustmentRequestDto request,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);

            // 権限チェック（本人のみ）
            if (!currentEmployee.getEmployeeId().equals(request.getEmployeeId())) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }

            AdjustmentRequest adjustmentRequest = requestService.submitAdjustmentRequest(
                    request.getEmployeeId(),
                    request.getTargetDate(),
                    request.getCorrectedClockInTime(),
                    request.getCorrectedClockOutTime(),
                    request.getReason());

            Map<String, Object> data = new HashMap<>();
            data.put("adjustmentRequestId", adjustmentRequest.getAdjustmentRequestId());

            response.put("success", true);
            response.put("data", data);
            response.put("message", "打刻修正申請が完了しました");

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 申請一覧取得（管理者用）
     */
    @GetMapping("/list")
    public ResponseEntity<Map<String, Object>> getRequestList(
            @RequestParam(required = false) String requestType,
            @RequestParam(required = false) String status,
            @RequestParam(required = false) Long employeeId,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);

            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }

            List<Map<String, Object>> requestList = new ArrayList<>();

            // 有給申請一覧
            if (requestType == null || "leave".equals(requestType)) {
                List<LeaveRequest> leaveRequests = requestService.getLeaveRequests(employeeId, status);
                for (LeaveRequest req : leaveRequests) {
                    Employee employee = employeeService.getEmployeeById(req.getEmployeeId());
                    Map<String, Object> item = new HashMap<>();
                    item.put("requestId", req.getLeaveRequestId());
                    item.put("requestType", "leave");
                    item.put("employeeId", req.getEmployeeId());
                    item.put("employeeName", employee.getEmployeeName());
                    item.put("requestDate", req.getLeaveRequestDate());
                    item.put("reason", req.getLeaveRequestReason());
                    item.put("status", req.getLeaveRequestStatus());
                    item.put("createdAt", req.getCreatedAt());
                    requestList.add(item);
                }
            }

            // 打刻修正申請一覧
            if (requestType == null || "adjustment".equals(requestType)) {
                List<AdjustmentRequest> adjustmentRequests = requestService.getAdjustmentRequests(employeeId, status);
                for (AdjustmentRequest req : adjustmentRequests) {
                    Employee employee = employeeService.getEmployeeById(req.getEmployeeId());
                    Map<String, Object> item = new HashMap<>();
                    item.put("requestId", req.getAdjustmentRequestId());
                    item.put("requestType", "adjustment");
                    item.put("employeeId", req.getEmployeeId());
                    item.put("employeeName", employee.getEmployeeName());
                    item.put("requestDate", req.getAdjustmentTargetDate());
                    item.put("reason", req.getAdjustmentReason());
                    item.put("status", req.getAdjustmentStatus());
                    item.put("createdAt", req.getCreatedAt());
                    requestList.add(item);
                }
            }

            // 作成日時でソート
            requestList.sort((a, b) ->
                    ((LocalDateTime) b.get("createdAt"))
                            .compareTo((LocalDateTime) a.get("createdAt")));

            response.put("success", true);
            response.put("data", requestList);

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 申請承認
     */
    @PostMapping("/approve/{requestId}")
    public ResponseEntity<Map<String, Object>> approveRequest(
            @PathVariable Long requestId,
            @RequestParam String requestType,
            @Valid @RequestBody RequestDto.ApprovalRequest request,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);

            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }

            if ("leave".equals(requestType)) {
                requestService.approveLeaveRequest(requestId, request.getApproverId());
            } else if ("adjustment".equals(requestType)) {
                requestService.approveAdjustmentRequest(requestId, request.getApproverId());
            } else {
                throw new BusinessException("INVALID_REQUEST_TYPE", "不正な申請タイプです");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "approved");
            data.put("approvedAt", LocalDateTime.now());

            response.put("success", true);
            response.put("data", data);
            response.put("message", "申請を承認しました");

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }

    /**
     * 申請却下
     */
    @PostMapping("/reject/{requestId}")
    public ResponseEntity<Map<String, Object>> rejectRequest(
            @PathVariable Long requestId,
            @RequestParam String requestType,
            @Valid @RequestBody RequestDto.RejectionRequest request,
            HttpSession session) {

        Map<String, Object> response = new HashMap<>();

        try {
            Employee currentEmployee = authService.getCurrentEmployee(session);

            // 管理者権限チェック
            if (currentEmployee.getEmployeeRole() != Employee.EmployeeRole.admin) {
                throw new BusinessException("ACCESS_DENIED", "アクセス権限がありません");
            }

            if ("leave".equals(requestType)) {
                requestService.rejectLeaveRequest(requestId, request.getApproverId());
            } else if ("adjustment".equals(requestType)) {
                requestService.rejectAdjustmentRequest(requestId, request.getApproverId(),
                        request.getRejectionReason());
            } else {
                throw new BusinessException("INVALID_REQUEST_TYPE", "不正な申請タイプです");
            }

            Map<String, Object> data = new HashMap<>();
            data.put("requestId", requestId);
            data.put("status", "rejected");
            data.put("rejectedAt", LocalDateTime.now());

            response.put("success", true);
            response.put("data", data);
            response.put("message", "申請を却下しました");

            return ResponseEntity.ok(response);

        } catch (BusinessException e) {
            response.put("success", false);
            response.put("errorCode", e.getErrorCode());
            response.put("message", e.getMessage());
            return ResponseEntity.badRequest().body(response);
        }
    }
}
