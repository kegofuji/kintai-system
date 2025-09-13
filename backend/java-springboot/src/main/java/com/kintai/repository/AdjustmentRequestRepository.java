package com.kintai.repository;

import com.kintai.entity.AdjustmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 打刻修正申請リポジトリ
 * 打刻修正申請のデータアクセス層
 */
@Repository
public interface AdjustmentRequestRepository extends JpaRepository<AdjustmentRequest, Long> {

    /**
     * 社員IDと対象日で打刻修正申請を検索
     * @param employeeId 社員ID
     * @param date 対象日
     * @return 打刻修正申請（Optional）
     */
    Optional<AdjustmentRequest> findByEmployeeIdAndAdjustmentTargetDate(Long employeeId, LocalDate date);

    /**
     * 修正申請ステータスで打刻修正申請一覧を検索
     * @param status 修正申請ステータス
     * @return 打刻修正申請一覧
     */
    List<AdjustmentRequest> findByAdjustmentStatus(String status);

    /**
     * 社員IDで打刻修正申請一覧を検索（作成日時降順）
     * @param employeeId 社員ID
     * @return 打刻修正申請一覧（作成日時降順）
     */
    List<AdjustmentRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    /**
     * 修正申請ステータスで打刻修正申請一覧を検索（作成日時昇順）
     * @param status 修正申請ステータス
     * @return 打刻修正申請一覧（作成日時昇順）
     */
    List<AdjustmentRequest> findByAdjustmentStatusOrderByCreatedAtAsc(String status);

    /**
     * 全打刻修正申請一覧を検索（作成日時降順）
     * @return 打刻修正申請一覧（作成日時降順）
     */
    List<AdjustmentRequest> findAllByOrderByCreatedAtDesc();
}
