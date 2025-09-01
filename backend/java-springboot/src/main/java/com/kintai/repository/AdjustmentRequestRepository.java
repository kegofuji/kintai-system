package com.kintai.repository;

import com.kintai.entity.AdjustmentRequest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AdjustmentRequestRepository extends JpaRepository<AdjustmentRequest, Long> {
    
    List<AdjustmentRequest> findByEmployeeId(Long employeeId);
    
    @Query("SELECT ar FROM AdjustmentRequest ar WHERE ar.employeeId = :employeeId " +
           "ORDER BY ar.adjustmentTargetDate DESC, ar.createdAt DESC")
    List<AdjustmentRequest> findByEmployeeIdOrderByDateDesc(@Param("employeeId") Long employeeId);
    
    List<AdjustmentRequest> findByAdjustmentStatus(String adjustmentStatus);
    
    @Query("SELECT ar FROM AdjustmentRequest ar WHERE " +
           "(:status IS NULL OR ar.adjustmentStatus = :status) " +
           "AND (:employeeId IS NULL OR ar.employeeId = :employeeId) " +
           "ORDER BY ar.createdAt DESC")
    List<AdjustmentRequest> findByStatusAndEmployee(@Param("status") String status, 
                                                   @Param("employeeId") Long employeeId);
    
    boolean existsByEmployeeIdAndAdjustmentTargetDate(Long employeeId, LocalDate adjustmentTargetDate);
    
    @Query("SELECT ar FROM AdjustmentRequest ar " +
           "JOIN Employee e ON ar.employeeId = e.employeeId " +
           "WHERE (:status IS NULL OR ar.adjustmentStatus = :status) " +
           "AND (:employeeId IS NULL OR ar.employeeId = :employeeId) " +
           "ORDER BY ar.createdAt DESC")
    List<AdjustmentRequest> findRequestsWithEmployeeInfo(@Param("status") String status,
                                                        @Param("employeeId") Long employeeId);
    
    @Query("SELECT COUNT(ar) FROM AdjustmentRequest ar WHERE ar.adjustmentStatus = '未処理'")
    long countPendingRequests();
}
