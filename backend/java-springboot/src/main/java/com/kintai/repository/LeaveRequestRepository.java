package com.kintai.repository;

import com.kintai.entity.LeaveRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface LeaveRequestRepository extends JpaRepository<LeaveRequest, Long> {
    
    // 社員の有給申請履歴取得
    List<LeaveRequest> findByEmployeeIdOrderByLeaveRequestDateDesc(Long employeeId);
    
    // 特定日の有給申請取得
    Optional<LeaveRequest> findByEmployeeIdAndLeaveRequestDate(Long employeeId, LocalDate leaveRequestDate);
    
    // 重複申請チェック
    boolean existsByEmployeeIdAndLeaveRequestDate(Long employeeId, LocalDate leaveRequestDate);
    
    // 申請状況での絞り込み
    List<LeaveRequest> findByEmployeeIdAndLeaveRequestStatus(Long employeeId, LeaveRequest.LeaveRequestStatus status);
    
    // 未処理の申請一覧取得
    @Query("SELECT lr FROM LeaveRequest lr JOIN lr.employee e WHERE " +
           "lr.leaveRequestStatus = 'PENDING' " +
           "ORDER BY lr.createdAt ASC")
    List<LeaveRequest> findPendingRequests();
    
    // 管理者用：申請一覧取得（フィルタ付き）
    @Query("SELECT lr FROM LeaveRequest lr JOIN lr.employee e WHERE " +
           "(:status IS NULL OR lr.leaveRequestStatus = :status) " +
           "AND (:employeeId IS NULL OR lr.employeeId = :employeeId) " +
           "AND (:employeeName IS NULL OR e.employeeName LIKE %:employeeName%) " +
           "ORDER BY lr.createdAt DESC")
    List<LeaveRequest> searchLeaveRequests(@Param("status") LeaveRequest.LeaveRequestStatus status,
                                          @Param("employeeId") Long employeeId,
                                          @Param("employeeName") String employeeName);
    
    // 期間内の承認済み有給取得
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.employeeId = :employeeId " +
           "AND lr.leaveRequestStatus = '承認' " +
           "AND lr.leaveRequestDate >= :fromDate " +
           "AND lr.leaveRequestDate <= :toDate")
    List<LeaveRequest> findApprovedLeavesByEmployeeIdAndDateRange(
            @Param("employeeId") Long employeeId,
            @Param("fromDate") LocalDate fromDate,
            @Param("toDate") LocalDate toDate);
    
    // 月次の有給取得日数カウント
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE " +
           "lr.employeeId = :employeeId " +
           "AND lr.leaveRequestStatus = '承認' " +
           "AND YEAR(lr.leaveRequestDate) = :year " +
           "AND MONTH(lr.leaveRequestDate) = :month")
    long countApprovedLeavesByEmployeeIdAndYearMonth(@Param("employeeId") Long employeeId,
                                                    @Param("year") int year,
                                                    @Param("month") int month);
    
    // 承認者による申請処理履歴
    List<LeaveRequest> findByApprovedByEmployeeIdOrderByApprovedAtDesc(Long approverId);
    
    // 申請日範囲での検索
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.employeeId = :employeeId " +
           "AND lr.leaveRequestDate >= :fromDate " +
           "AND lr.leaveRequestDate <= :toDate " +
           "ORDER BY lr.leaveRequestDate")
    List<LeaveRequest> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                   @Param("fromDate") LocalDate fromDate,
                                                   @Param("toDate") LocalDate toDate);
}