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
 * 勤怠記録エンティティ
 * 社員の勤怠記録を管理するテーブル
 */
@Entity
@Table(name = "attendance_records", indexes = {
    @Index(name = "idx_employee_date", columnList = "employee_id, attendance_date"),
    @Index(name = "idx_attendance_date", columnList = "attendance_date")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttendanceRecord {

    /**
     * 勤怠ID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "attendance_id", columnDefinition = "BIGINT")
    private Long attendanceId;

    /**
     * 社員ID（外部キー）
     */
    @Column(name = "employee_id", nullable = false, columnDefinition = "BIGINT")
    private Long employeeId;

    /**
     * 勤怠日
     */
    @Column(name = "attendance_date", nullable = false, columnDefinition = "DATE")
    private LocalDate attendanceDate;

    /**
     * 出勤打刻
     */
    @Column(name = "clock_in_time", columnDefinition = "DATETIME")
    private LocalDateTime clockInTime;

    /**
     * 退勤打刻
     */
    @Column(name = "clock_out_time", columnDefinition = "DATETIME")
    private LocalDateTime clockOutTime;

    /**
     * 遅刻分（分単位）
     */
    @Column(name = "late_minutes", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer lateMinutes;

    /**
     * 早退分（分単位）
     */
    @Column(name = "early_leave_minutes", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer earlyLeaveMinutes;

    /**
     * 残業分（分単位）
     */
    @Column(name = "overtime_minutes", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer overtimeMinutes;

    /**
     * 深夜分（分単位）（22:00～翌5:00）
     */
    @Column(name = "night_shift_minutes", nullable = false, columnDefinition = "INT DEFAULT 0")
    private Integer nightShiftMinutes;

    /**
     * 勤怠ステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "attendance_status", nullable = false, columnDefinition = "ENUM('normal','paid_leave','absent') DEFAULT 'normal'")
    private AttendanceStatus attendanceStatus;

    /**
     * 月末申請ステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "submission_status", nullable = false, columnDefinition = "ENUM('未提出','申請済','承認','却下') DEFAULT '未提出'")
    private SubmissionStatus submissionStatus;

    /**
     * 勤怠確定フラグ（0：未確定、1：確定済）
     */
    @Column(name = "attendance_fixed_flag", nullable = false, columnDefinition = "TINYINT(1) DEFAULT 0")
    private Boolean attendanceFixedFlag;

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
     * 社員との関連（外部キー制約）
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "employee_id", insertable = false, updatable = false)
    private Employee employee;

    /**
     * 勤怠ステータス列挙型
     */
    public enum AttendanceStatus {
        NORMAL("normal"),
        PAID_LEAVE("paid_leave"),
        ABSENT("absent");

        private final String value;

        AttendanceStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 月末申請ステータス列挙型
     */
    public enum SubmissionStatus {
        NOT_SUBMITTED("未提出"),
        SUBMITTED("申請済"),
        APPROVED("承認"),
        REJECTED("却下");

        private final String value;

        SubmissionStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
