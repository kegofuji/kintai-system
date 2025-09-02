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
    
    List<LeaveRequest> findByEmployeeId(Long employeeId);
    
    List<LeaveRequest> findByLeaveRequestStatus(String status);
    
    Optional<LeaveRequest> findByEmployeeIdAndLeaveRequestDate(Long employeeId, LocalDate leaveDate);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "(:employeeId IS NULL OR lr.employeeId = :employeeId) " +
           "AND (:status IS NULL OR lr.leaveRequestStatus = :status)")
    List<LeaveRequest> findLeaveRequestsWithFilters(
        @Param("employeeId") Long employeeId, 
        @Param("status") String status);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "lr.leaveRequestDate BETWEEN :startDate AND :endDate")
    List<LeaveRequest> findByLeaveRequestDateBetween(
        @Param("startDate") LocalDate startDate, 
        @Param("endDate") LocalDate endDate);
    
    boolean existsByEmployeeIdAndLeaveRequestDate(Long employeeId, LocalDate leaveDate);
}