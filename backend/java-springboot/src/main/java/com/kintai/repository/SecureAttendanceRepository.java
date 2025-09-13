package com.kintai.repository;

import com.kintai.entity.AttendanceRecord;
import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 安全な勤怠データアクセスリポジトリ
 * SQLインジェクション対策を実装
 */
@Repository
public class SecureAttendanceRepository {
    
    @PersistenceContext
    private EntityManager entityManager;
    
    /**
     * 安全な動的クエリ実行
     */
    public List<AttendanceRecord> findByConditions(Long employeeId, LocalDate startDate, LocalDate endDate) {
        StringBuilder jpql = new StringBuilder();
        jpql.append("SELECT a FROM AttendanceRecord a WHERE 1=1");
        
        Map<String, Object> parameters = new HashMap<>();
        
        if (employeeId != null) {
            jpql.append(" AND a.employeeId = :employeeId");
            parameters.put("employeeId", employeeId);
        }
        
        if (startDate != null) {
            jpql.append(" AND a.attendanceDate >= :startDate");
            parameters.put("startDate", startDate);
        }
        
        if (endDate != null) {
            jpql.append(" AND a.attendanceDate <= :endDate");
            parameters.put("endDate", endDate);
        }
        
        jpql.append(" ORDER BY a.attendanceDate DESC");
        
        TypedQuery<AttendanceRecord> query = entityManager.createQuery(jpql.toString(), AttendanceRecord.class);
        parameters.forEach(query::setParameter);
        
        return query.getResultList();
    }
    
    /**
     * 安全な社員別勤怠統計取得
     */
    public List<Object[]> getAttendanceStatistics(Long employeeId, LocalDate startDate, LocalDate endDate) {
        String jpql = "SELECT a.attendanceDate, " +
                     "COUNT(a.attendanceRecordId) as recordCount, " +
                     "SUM(a.overtimeMinutes) as totalOvertime " +
                     "FROM AttendanceRecord a " +
                     "WHERE a.employeeId = :employeeId " +
                     "AND a.attendanceDate >= :startDate " +
                     "AND a.attendanceDate <= :endDate " +
                     "GROUP BY a.attendanceDate " +
                     "ORDER BY a.attendanceDate DESC";
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("employeeId", employeeId);
        query.setParameter("startDate", startDate);
        query.setParameter("endDate", endDate);
        
        return query.getResultList();
    }
    
    /**
     * 安全な月次勤怠集計
     */
    public List<Object[]> getMonthlyAttendanceSummary(Long employeeId, int year, int month) {
        String jpql = "SELECT a.attendanceDate, " +
                     "a.clockInTime, " +
                     "a.clockOutTime, " +
                     "a.overtimeMinutes, " +
                     "a.attendanceStatus " +
                     "FROM AttendanceRecord a " +
                     "WHERE a.employeeId = :employeeId " +
                     "AND YEAR(a.attendanceDate) = :year " +
                     "AND MONTH(a.attendanceDate) = :month " +
                     "ORDER BY a.attendanceDate";
        
        TypedQuery<Object[]> query = entityManager.createQuery(jpql, Object[].class);
        query.setParameter("employeeId", employeeId);
        query.setParameter("year", year);
        query.setParameter("month", month);
        
        return query.getResultList();
    }
}
