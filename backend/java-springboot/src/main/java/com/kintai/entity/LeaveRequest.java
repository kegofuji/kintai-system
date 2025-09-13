package com.kintai.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 有給申請エンティティ
 * 社員の有給休暇申請を管理するテーブル
 */
@Entity
@Table(name = "leave_requests", indexes = {
    @Index(name = "idx_employee_leave_date", columnList = "employee_id, leave_request_date"),
    @Index(name = "idx_leave_date", columnList = "leave_request_date"),
    @Index(name = "idx_status", columnList = "leave_request_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LeaveRequest {

    /**
     * 有給申請ID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "leave_request_id", columnDefinition = "BIGINT")
    private Long leaveRequestId;

    /**
     * 申請者社員ID（外部キー）
     */
    @Column(name = "employee_id", nullable = false, columnDefinition = "BIGINT")
    private Long employeeId;

    /**
     * 申請日（1日単位）
     */
    @Column(name = "leave_request_date", nullable = false, columnDefinition = "DATE")
    private LocalDate leaveRequestDate;

    /**
     * 申請理由
     */
    @Column(name = "leave_request_reason", length = 200, nullable = false)
    private String leaveRequestReason;

    /**
     * 申請状態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "leave_request_status", nullable = false, columnDefinition = "ENUM('未処理','承認','却下') DEFAULT '未処理'")
    private LeaveRequestStatus leaveRequestStatus;

    /**
     * 承認日時
     */
    @Column(name = "approved_at", columnDefinition = "DATETIME")
    private LocalDateTime approvedAt;

    /**
     * 承認者社員ID（外部キー）
     */
    @Column(name = "approved_by_employee_id", columnDefinition = "BIGINT")
    private Long approvedByEmployeeId;

    /**
     * 作成日時
     */
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP")
    private LocalDateTime createdAt;

    /**
     * 更新日時
     */
    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false, columnDefinition = "DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP")
    private LocalDateTime updatedAt;

    /**
     * 申請者との関連（外部キー制約）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    /**
     * 承認者との関連（外部キー制約）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by_employee_id", insertable = false, updatable = false)
    private Employee approvedBy;

    /**
     * 有給申請ステータス列挙型
     */
    public enum LeaveRequestStatus {
        PENDING("未処理"),
        APPROVED("承認"),
        REJECTED("却下");

        private final String value;

        LeaveRequestStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
