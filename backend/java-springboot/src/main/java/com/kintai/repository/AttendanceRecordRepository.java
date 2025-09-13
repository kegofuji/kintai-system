package com.kintai.repository;

import com.kintai.entity.AttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

/**
 * 勤怠記録リポジトリ
 * 勤怠記録のデータアクセス層
 */
@Repository
public interface AttendanceRecordRepository extends JpaRepository<AttendanceRecord, Long> {

    /**
     * 社員IDと勤怠日で勤怠記録を検索
     * @param employeeId 社員ID
     * @param date 勤怠日
     * @return 勤怠記録（Optional）
     */
    Optional<AttendanceRecord> findByEmployeeIdAndAttendanceDate(Long employeeId, LocalDate date);

    /**
     * 社員IDと期間で勤怠記録一覧を検索
     * @param employeeId 社員ID
     * @param start 開始日
     * @param end 終了日
     * @return 勤怠記録一覧
     */
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateBetween(Long employeeId, LocalDate start, LocalDate end);

    /**
     * 社員IDと期間で勤怠記録一覧を検索（勤怠日順）
     * @param employeeId 社員ID
     * @param start 開始日
     * @param end 終了日
     * @return 勤怠記録一覧（勤怠日順）
     */
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(Long employeeId, LocalDate start, LocalDate end);

    /**
     * 社員IDと期間と確定フラグで勤怠記録一覧を検索
     * @param employeeId 社員ID
     * @param start 開始日
     * @param end 終了日
     * @param fixedFlag 確定フラグ
     * @return 勤怠記録一覧
     */
    List<AttendanceRecord> findByEmployeeIdAndAttendanceDateBetweenAndAttendanceFixedFlag(Long employeeId, LocalDate start, LocalDate end, Boolean fixedFlag);

    /**
     * 社員IDと年月と勤怠ステータスで勤怠記録一覧を検索
     * @param employeeId 社員ID
     * @param yearMonth 年月（YYYY-MM形式）
     * @param status 勤怠ステータス
     * @return 勤怠記録一覧
     */
    @Query("SELECT a FROM AttendanceRecord a WHERE a.employeeId = :employeeId AND FUNCTION('FORMATDATETIME', a.attendanceDate, 'yyyy-MM') = :yearMonth AND a.attendanceStatus = :status")
    List<AttendanceRecord> findByEmployeeIdAndYearMonthAndAttendanceStatus(@Param("employeeId") Long employeeId, @Param("yearMonth") String yearMonth, @Param("status") String status);

    /**
     * 月末申請ステータスと社員IDで勤怠記録一覧を検索
     * @param submissionStatus 月末申請ステータス
     * @param employeeId 社員ID
     * @return 勤怠記録一覧
     */
    List<AttendanceRecord> findBySubmissionStatusAndEmployeeId(String submissionStatus, Long employeeId);
}
