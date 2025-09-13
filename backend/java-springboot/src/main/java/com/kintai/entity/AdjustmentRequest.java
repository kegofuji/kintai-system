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
 * 打刻修正申請エンティティ
 * 社員の打刻修正申請を管理するテーブル
 */
@Entity
@Table(name = "adjustment_requests", indexes = {
    @Index(name = "idx_employee_target_date", columnList = "employee_id, adjustment_target_date"),
    @Index(name = "idx_target_date", columnList = "adjustment_target_date"),
    @Index(name = "idx_status", columnList = "adjustment_status")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdjustmentRequest {

    /**
     * 修正申請ID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "adjustment_request_id", columnDefinition = "BIGINT")
    private Long adjustmentRequestId;

    /**
     * 申請者社員ID（外部キー）
     */
    @Column(name = "employee_id", nullable = false, columnDefinition = "BIGINT")
    private Long employeeId;

    /**
     * 対象日
     */
    @Column(name = "adjustment_target_date", nullable = false, columnDefinition = "DATE")
    private LocalDate adjustmentTargetDate;

    /**
     * 元出勤時刻
     */
    @Column(name = "original_clock_in_time", columnDefinition = "DATETIME")
    private LocalDateTime originalClockInTime;

    /**
     * 元退勤時刻
     */
    @Column(name = "original_clock_out_time", columnDefinition = "DATETIME")
    private LocalDateTime originalClockOutTime;

    /**
     * 申請：出勤時刻
     */
    @Column(name = "adjustment_requested_time_in", columnDefinition = "DATETIME")
    private LocalDateTime adjustmentRequestedTimeIn;

    /**
     * 申請：退勤時刻
     */
    @Column(name = "adjustment_requested_time_out", columnDefinition = "DATETIME")
    private LocalDateTime adjustmentRequestedTimeOut;

    /**
     * 修正理由
     */
    @Column(name = "adjustment_reason", length = 200, nullable = false)
    private String adjustmentReason;

    /**
     * 修正申請状態
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "adjustment_status", nullable = false, columnDefinition = "ENUM('未処理','承認','却下') DEFAULT '未処理'")
    private AdjustmentStatus adjustmentStatus;

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
     * 却下理由
     */
    @Column(name = "rejection_reason", length = 200)
    private String rejectionReason;

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
     * 修正申請ステータス列挙型
     */
    public enum AdjustmentStatus {
        PENDING("未処理"),
        APPROVED("承認"),
        REJECTED("却下");

        private final String value;

        AdjustmentStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
