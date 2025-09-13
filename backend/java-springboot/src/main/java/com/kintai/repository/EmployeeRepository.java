package com.kintai.repository;

import com.kintai.entity.Employee;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 社員リポジトリ
 * 社員情報のデータアクセス層
 */
@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    /**
     * 社員コードで社員を検索
     * @param employeeCode 社員コード
     * @return 社員情報（Optional）
     */
    Optional<Employee> findByEmployeeCode(String employeeCode);

    /**
     * 社員コードと雇用ステータスで社員を検索
     * @param employeeCode 社員コード
     * @param status 雇用ステータス
     * @return 社員情報（Optional）
     */
    Optional<Employee> findByEmployeeCodeAndEmploymentStatus(String employeeCode, String status);

    /**
     * 雇用ステータスで社員一覧を検索
     * @param status 雇用ステータス
     * @return 社員一覧
     */
    List<Employee> findByEmploymentStatus(String status);

    /**
     * 雇用ステータスで社員一覧を検索（社員コード順）
     * @param status 雇用ステータス
     * @return 社員一覧（社員コード順）
     */
    List<Employee> findByEmploymentStatusOrderByEmployeeCode(String status);

    /**
     * 社員名または社員コードで社員一覧を検索（部分一致）
     * @param name 社員名（部分一致）
     * @param code 社員コード（部分一致）
     * @return 社員一覧
     */
    List<Employee> findByEmployeeNameContainingOrEmployeeCodeContaining(String name, String code);
}
