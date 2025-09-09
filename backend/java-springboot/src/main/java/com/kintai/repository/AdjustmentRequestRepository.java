package com.kintai.repository;

import com.kintai.entity.AdjustmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdjustmentRequestRepository extends JpaRepository<AdjustmentRequest, Long> {
    
    // 社員の打刻修正申請履歴取得
    List<AdjustmentRequest> findByEmployeeIdOrderByAdjustmentTargetDateDesc(Long employeeId);
    
    // 特定日の打刻修正申請取得
    Optional<AdjustmentRequest> findByEmployeeIdAndAdjustmentTargetDate(Long employeeId, LocalDate targetDate);
    
    // 申請状況での絞り込み
    List<AdjustmentRequest> findByEmployeeIdAndAdjustmentStatus(Long employeeId, AdjustmentRequest.AdjustmentStatus status);
    
    // 未処理の申請一覧取得
    @Query("SELECT ar FROM AdjustmentRequest ar JOIN ar.employee e WHERE " +
           "ar.adjustmentStatus = 'PENDING' " +
           "ORDER BY ar.createdAt ASC")
    List<AdjustmentRequest> findPendingRequests();
    
    // 管理者用：申請一覧取得（フィルタ付き）
    @Query("SELECT ar FROM AdjustmentRequest ar JOIN ar.employee e WHERE " +
           "(:status IS NULL OR ar.adjustmentStatus = :status) " +
           "AND (:employeeId IS NULL OR ar.employeeId = :employeeId) " +
           "AND (:employeeName IS NULL OR e.employeeName LIKE %:employeeName%) " +
           "ORDER BY ar.createdAt DESC")
    List<AdjustmentRequest> searchAdjustmentRequests(@Param("status") AdjustmentRequest.AdjustmentStatus status,
                                                    @Param("employeeId") Long employeeId,
                                                    @Param("employeeName") String employeeName);
    
    // 承認者による申請処理履歴
    List<AdjustmentRequest> findByApprovedByEmployeeIdOrderByApprovedAtDesc(Long approverId);
    
    // 期間内の申請取得
    @Query("SELECT ar FROM AdjustmentRequest ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND ar.adjustmentTargetDate >= :fromDate " +
           "AND ar.adjustmentTargetDate <= :toDate " +
           "ORDER BY ar.adjustmentTargetDate DESC")
    List<AdjustmentRequest> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                        @Param("fromDate") LocalDate fromDate,
                                                        @Param("toDate") LocalDate toDate);
    
    // 重複申請チェック（同一日で未処理の申請があるかチェック）
    @Query("SELECT COUNT(ar) > 0 FROM AdjustmentRequest ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND ar.adjustmentTargetDate = :targetDate " +
           "AND ar.adjustmentStatus = 'PENDING'")
    boolean existsPendingRequestByEmployeeIdAndTargetDate(@Param("employeeId") Long employeeId,
                                                         @Param("targetDate") LocalDate targetDate);
}