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
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE " +
           "ar.submissionStatus = :status")
    List<AttendanceRecord> findBySubmissionStatus(@Param("status") String status);
    
    @Query("SELECT ar FROM AttendanceRecord ar WHERE " +
           "ar.attendanceFixedFlag = :fixed")
    List<AttendanceRecord> findByAttendanceFixedFlag(@Param("fixed") Boolean fixed);
    
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND YEAR(ar.attendanceDate) = :year " +
           "AND MONTH(ar.attendanceDate) = :month " +
           "AND (ar.clockInTime IS NULL OR ar.clockOutTime IS NULL) " +
           "AND ar.attendanceStatus = 'normal'")
    long countIncompleteAttendanceByEmployeeIdAndYearMonth(
        @Param("employeeId") Long employeeId,
        @Param("year") int year,
        @Param("month") int month);
}