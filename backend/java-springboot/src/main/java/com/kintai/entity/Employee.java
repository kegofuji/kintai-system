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
 * 社員エンティティ
 * 社員情報を管理するテーブル
 */
@Entity
@Table(name = "employees", indexes = {
    @Index(name = "idx_employee_code", columnList = "employee_code", unique = true),
    @Index(name = "idx_employee_email", columnList = "email", unique = true)
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Employee {

    /**
     * 社員ID（主キー）
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "employee_id", columnDefinition = "BIGINT")
    private Long employeeId;

    /**
     * 社員コード（ユニーク）
     */
    @Column(name = "employee_code", length = 10, nullable = false, unique = true)
    private String employeeCode;

    /**
     * 社員名
     */
    @Column(name = "employee_name", length = 50, nullable = false)
    private String employeeName;

    /**
     * メールアドレス（ユニーク）
     */
    @Column(name = "email", length = 100, nullable = false, unique = true)
    private String email;

    /**
     * パスワードハッシュ
     */
    @Column(name = "employee_password_hash", length = 255, nullable = false)
    private String employeePasswordHash;

    /**
     * 社員ロール
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "employee_role", nullable = false, columnDefinition = "ENUM('employee','admin') DEFAULT 'employee'")
    private EmployeeRole employeeRole;

    /**
     * 雇用ステータス
     */
    @Enumerated(EnumType.STRING)
    @Column(name = "employment_status", nullable = false, columnDefinition = "ENUM('active','retired') DEFAULT 'active'")
    private EmploymentStatus employmentStatus;

    /**
     * 入社日
     */
    @Column(name = "hired_at", nullable = false, columnDefinition = "DATE")
    private LocalDate hiredAt;

    /**
     * 退職日
     */
    @Column(name = "retired_at", columnDefinition = "DATE")
    private LocalDate retiredAt;

    /**
     * 有給残日数
     */
    @Column(name = "paid_leave_remaining_days", nullable = false, columnDefinition = "INT DEFAULT 10")
    private Integer paidLeaveRemainingDays;

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
     * 社員ロール列挙型
     */
    public enum EmployeeRole {
        EMPLOYEE("employee"),
        ADMIN("admin");

        private final String value;

        EmployeeRole(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }

    /**
     * 雇用ステータス列挙型
     */
    public enum EmploymentStatus {
        ACTIVE("active"),
        RETIRED("retired");

        private final String value;

        EmploymentStatus(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }
    }
}
