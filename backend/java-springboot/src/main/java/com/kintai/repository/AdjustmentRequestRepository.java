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
    
    List<AdjustmentRequest> findByEmployeeId(Long employeeId);
    
    List<AdjustmentRequest> findByAdjustmentStatus(String status);
    
    Optional<AdjustmentRequest> findByEmployeeIdAndAdjustmentTargetDate(
        Long employeeId, LocalDate targetDate);
    
    @Query("SELECT ar FROM AdjustmentRequest ar WHERE " +
           "(:employeeId IS NULL OR ar.employeeId = :employeeId) " +
           "AND (:status IS NULL OR ar.adjustmentStatus = :status)")
    List<AdjustmentRequest> findAdjustmentRequestsWithFilters(
        @Param("employeeId") Long employeeId, 
        @Param("status") String status);
    
    @Query("SELECT ar FROM AdjustmentRequest ar WHERE " +
           "ar.adjustmentTargetDate BETWEEN :startDate AND :endDate")
    List<AdjustmentRequest> findByAdjustmentTargetDateBetween(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
}