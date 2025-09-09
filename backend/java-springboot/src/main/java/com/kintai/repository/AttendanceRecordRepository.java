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
    
    // 社員ID + 勤務日での検索
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);
    
    // 社員の勤怠履歴取得（期間指定）
    @Query("SELECT ar FROM AttendanceRecord ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND ar.attendanceDate >= :fromDate " +
           "AND ar.attendanceDate <= :toDate " +
           "ORDER BY ar.attendanceDate DESC")
    List<AttendanceRecord> findByEmployeeIdAndDateRange(@Param("employeeId") Long employeeId,
                                                        @Param("fromDate") LocalDate fromDate,
                                                        @Param("toDate") LocalDate toDate);
    
    // 社員の月次勤怠データ取得
    @Query("SELECT ar FROM AttendanceRecord ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND YEAR(ar.attendanceDate) = :year " +
           "AND MONTH(ar.attendanceDate) = :month " +
           "ORDER BY ar.attendanceDate")
    List<AttendanceRecord> findByEmployeeIdAndYearMonth(@Param("employeeId") Long employeeId,
                                                        @Param("year") int year,
                                                        @Param("month") int month);
    
    // 特定ステータスでの検索
    @Query("SELECT ar FROM AttendanceRecord ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND YEAR(ar.attendanceDate) = :year " +
           "AND MONTH(ar.attendanceDate) = :month " +
           "AND ar.attendanceStatus = :status")
    List<AttendanceRecord> findByEmployeeIdAndYearMonthAndAttendanceStatus(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("status") AttendanceRecord.AttendanceStatus status);
    
    // 申請状況での検索
    @Query("SELECT ar FROM AttendanceRecord ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND YEAR(ar.attendanceDate) = :year " +
           "AND MONTH(ar.attendanceDate) = :month " +
           "AND ar.submissionStatus = :status")
    List<AttendanceRecord> findByEmployeeIdAndYearMonthAndSubmissionStatus(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month,
            @Param("status") AttendanceRecord.SubmissionStatus status);
    
    // 打刻漏れチェック用
    @Query("SELECT ar FROM AttendanceRecord ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND YEAR(ar.attendanceDate) = :year " +
           "AND MONTH(ar.attendanceDate) = :month " +
           "AND ar.attendanceStatus = 'NORMAL' " +
           "AND (ar.clockInTime IS NULL OR ar.clockOutTime IS NULL)")
    List<AttendanceRecord> findIncompleteAttendanceByEmployeeIdAndYearMonth(
            @Param("employeeId") Long employeeId,
            @Param("year") int year,
            @Param("month") int month);
    
    // 管理者用：全社員の勤怠データ検索
    @Query("SELECT ar FROM AttendanceRecord ar JOIN ar.employee e WHERE " +
           "(:employeeId IS NULL OR ar.employeeId = :employeeId) " +
           "AND (:employeeName IS NULL OR e.employeeName LIKE %:employeeName%) " +
           "AND (:fromDate IS NULL OR ar.attendanceDate >= :fromDate) " +
           "AND (:toDate IS NULL OR ar.attendanceDate <= :toDate) " +
           "ORDER BY ar.attendanceDate DESC, e.employeeCode")
    List<AttendanceRecord> searchAttendanceRecords(@Param("employeeId") Long employeeId,
                                                  @Param("employeeName") String employeeName,
                                                  @Param("fromDate") LocalDate fromDate,
                                                  @Param("toDate") LocalDate toDate);
    
    // 月末申請済みデータの確認
    @Query("SELECT COUNT(ar) FROM AttendanceRecord ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND YEAR(ar.attendanceDate) = :year " +
           "AND MONTH(ar.attendanceDate) = :month " +
           "AND ar.submissionStatus = '申請済'")
    long countSubmittedRecordsByEmployeeIdAndYearMonth(@Param("employeeId") Long employeeId,
                                                      @Param("year") int year,
                                                      @Param("month") int month);
    
    // 勤怠集計用
    @Query("SELECT " +
           "COALESCE(SUM(ar.lateMinutes), 0), " +
           "COALESCE(SUM(ar.earlyLeaveMinutes), 0), " +
           "COALESCE(SUM(ar.overtimeMinutes), 0), " +
           "COALESCE(SUM(ar.nightShiftMinutes), 0), " +
           "COUNT(CASE WHEN ar.attendanceStatus = 'PAID_LEAVE' THEN 1 END), " +
           "COUNT(CASE WHEN ar.attendanceStatus = 'ABSENT' THEN 1 END) " +
           "FROM AttendanceRecord ar WHERE " +
           "ar.employeeId = :employeeId " +
           "AND ar.attendanceDate >= :fromDate " +
           "AND ar.attendanceDate <= :toDate")
    Object[] getAttendanceSummary(@Param("employeeId") Long employeeId,
                                  @Param("fromDate") LocalDate fromDate,
                                  @Param("toDate") LocalDate toDate);
    
    // 確定済みデータの取得
    List<AttendanceRecord> findByEmployeeIdAndAttendanceFixedFlagTrue(Long employeeId);
    
    // 当日の勤怠記録存在チェック
    boolean existsByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate attendanceDate);
}