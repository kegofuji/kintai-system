package com.kintai.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 社員情報DTO
 * 社員の基本情報
 */
@Data
@Builder
public class EmployeeInfo {

    /**
     * 社員ID
     */
    private Long employeeId;

    /**
     * 社員名
     */
    private String employeeName;

    /**
     * 社員コード
     */
    private String employeeCode;
}
