package com.kintai.repository;

import com.kintai.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {
    
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);
    
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateBetween(
        Long employeeId, LocalDate startDate, LocalDate endDate);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employeeId = :employeeId " +
           "AND YEAR(ar.attendanceDate) = :year AND MONTH(ar.attendanceDate) = :month " +
           "ORDER BY ar.attendanceDate")
    List<AttendanceRecord> findByEmployeeIdAndYearMonth(
        @Param("employeeId") Long employeeId, 
        @Param("year") int year, 
        @Param("month") int month);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.employeeId = :employeeId " +
           "AND YEAR(ar.attendanceDate) = :year AND MONTH(ar.attendanceDate) = :month " +
           "AND ar.attendanceStatus = :status")
    List<AttendanceRecord> findByEmployeeIdAndYearMonthAndAttendanceStatus(
        @Param("employeeId") Long employeeId, 
        @Param("year") int year, 
        @Param("month") int month,
        @Param("status") AttendanceRecord.AttendanceStatus status);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:employeeId IS NULL OR ar.employeeId = :employeeId) " +
           "ORDER BY ar.attendanceDate DESC, ar.employeeId")
    List<AttendanceRecord> findByDateRangeAndEmployee(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("employeeId") Long employeeId);
    
    List<AttendanceRecord> findBySubmissionStatus(String submissionStatus);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE ar.submissionStatus = :status " +
           "ORDER BY ar.createdAt DESC")
    List<AttendanceRecord> findBySubmissionStatusOrderByCreatedAtDesc(String status);
    
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE ar.employeeId = :employeeId " +
           "AND YEAR(ar.attendanceDate) = :year AND MONTH(ar.attendanceDate) = :month " +
           "AND (ar.clockInTime IS NULL OR ar.clockOutTime IS NULL) " +
           "AND ar.attendanceStatus != 'paid_leave'")
    long countIncompleteAttendanceByEmployeeIdAndYearMonth(
        @Param("employeeId") Long employeeId, 
        @Param("year") int year, 
        @Param("month") int month);
    
    @Query("SELECT ar FROM AttendanceRecord ar " +
           "JOIN Employee e ON ar.employeeId = e.employeeId " +
           "WHERE ar.attendanceDate BETWEEN :startDate AND :endDate " +
           "AND (:employeeCode IS NULL OR e.employeeCode LIKE %:employeeCode%) " +
           "AND (:employeeName IS NULL OR e.employeeName LIKE %:employeeName%) " +
           "ORDER BY ar.attendanceDate DESC, e.employeeCode")
    List<AttendanceRecord> findAttendanceRecordsWithEmployeeInfo(
        @Param("startDate") LocalDate startDate,
        @Param("endDate") LocalDate endDate,
        @Param("employeeCode") String employeeCode,
        @Param("employeeName") String employeeName);
}
