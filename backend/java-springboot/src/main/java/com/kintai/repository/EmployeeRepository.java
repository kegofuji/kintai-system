package com.kintai.repository;

import com.kintai.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {
    
    // 社員コードでの検索
    Optional<Employee> findByEmployeeCode(String employeeCode);
    
    // メールアドレスでの検索
    Optional<Employee> findByEmail(String email);
    
    // 社員コードの重複チェック
    boolean existsByEmployeeCode(String employeeCode);
    
    // メールアドレスの重複チェック
    boolean existsByEmail(String email);
    
    // 在籍状況での絞り込み
    List<Employee> findByEmploymentStatus(Employee.EmploymentStatus employmentStatus);
    
    // 在籍社員一覧取得
    @Query("SELECT e FROM Employee e WHERE e.employmentStatus = 'ACTIVE' ORDER BY e.employeeCode")
    List<Employee> findActiveEmployees();
    
    // 社員検索（名前・社員コードで部分一致）
    @Query("SELECT e FROM Employee e WHERE " +
           "(e.employeeName LIKE %:keyword% OR e.employeeCode LIKE %:keyword%) " +
           "AND (:status IS NULL OR e.employmentStatus = :status) " +
           "ORDER BY e.employeeCode")
    List<Employee> searchEmployees(@Param("keyword") String keyword, 
                                  @Param("status") Employee.EmploymentStatus status);
    
    // 管理者一覧取得
    @Query("SELECT e FROM Employee e WHERE e.employeeRole = 'ADMIN' AND e.employmentStatus = 'ACTIVE'")
    List<Employee> findAdminEmployees();
    
    // 社員ID一覧での検索
    List<Employee> findByEmployeeIdIn(List<Long> employeeIds);
}