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
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE lr.employeeId = :employeeId " +
           "ORDER BY lr.leaveRequestDate DESC, lr.createdAt DESC")
    List<LeaveRequest> findByEmployeeIdOrderByDateDesc(@Param("employeeId") Long employeeId);
    
    List<LeaveRequest> findByLeaveRequestStatus(String leaveRequestStatus);
    
    Optional<LeaveRequest> findByEmployeeIdAndLeaveRequestDate(Long employeeId, LocalDate leaveRequestDate);
    
    @Query("SELECT lr FROM LeaveRequest lr WHERE " +
           "(:status IS NULL OR lr.leaveRequestStatus = :status) " +
           "AND (:employeeId IS NULL OR lr.employeeId = :employeeId) " +
           "ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findByStatusAndEmployee(@Param("status") String status, 
                                              @Param("employeeId") Long employeeId);
    
    boolean existsByEmployeeIdAndLeaveRequestDate(Long employeeId, LocalDate leaveRequestDate);
    
    @Query("SELECT lr FROM LeaveRequest lr " +
           "JOIN Employee e ON lr.employeeId = e.employeeId " +
           "WHERE (:status IS NULL OR lr.leaveRequestStatus = :status) " +
           "AND (:employeeId IS NULL OR lr.employeeId = :employeeId) " +
           "ORDER BY lr.createdAt DESC")
    List<LeaveRequest> findRequestsWithEmployeeInfo(@Param("status") String status,
                                                   @Param("employeeId") Long employeeId);
    
    @Query("SELECT COUNT(lr) FROM LeaveRequest lr WHERE lr.leaveRequestStatus = '未処理'")
    long countPendingRequests();
}
