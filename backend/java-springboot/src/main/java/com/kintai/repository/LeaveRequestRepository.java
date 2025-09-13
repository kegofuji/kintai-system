package com.kintai.repository;

import com.kintai.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 有給申請リポジトリ
 * 有給申請のデータアクセス層
 */
@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {

    /**
     * 社員IDと申請日で有給申請を検索
     * @param employeeId 社員ID
     * @param date 申請日
     * @return 有給申請（Optional）
     */
    Optional<LeaveRequest> findByEmployeeIdAndLeaveRequestDate(Long employeeId, LocalDate date);

    /**
     * 申請ステータスで有給申請一覧を検索
     * @param status 申請ステータス
     * @return 有給申請一覧
     */
    List<LeaveRequest> findByLeaveRequestStatus(String status);

    /**
     * 社員IDで有給申請一覧を検索（作成日時降順）
     * @param employeeId 社員ID
     * @return 有給申請一覧（作成日時降順）
     */
    List<LeaveRequest> findByEmployeeIdOrderByCreatedAtDesc(Long employeeId);

    /**
     * 申請ステータスで有給申請一覧を検索（作成日時昇順）
     * @param status 申請ステータス
     * @return 有給申請一覧（作成日時昇順）
     */
    List<LeaveRequest> findByLeaveRequestStatusOrderByCreatedAtAsc(String status);

    /**
     * 全有給申請一覧を検索（作成日時降順）
     * @return 有給申請一覧（作成日時降順）
     */
    List<LeaveRequest> findAllByOrderByCreatedAtDesc();
}
