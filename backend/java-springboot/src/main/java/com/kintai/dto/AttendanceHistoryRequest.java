package com.kintai.dto;

import lombok.Data;

import java.time.LocalDate;

/**
 * 勤怠履歴リクエストDTO
 * 勤怠履歴取得時のリクエストデータ
 */
@Data
public class AttendanceHistoryRequest {

    /**
     * 社員ID
     */
    private Long employeeId;

    /**
     * 年月（YYYY-MM形式）
     */
    private String yearMonth;

    /**
     * 開始日
     */
    private LocalDate dateFrom;

    /**
     * 終了日
     */
    private LocalDate dateTo;
}
