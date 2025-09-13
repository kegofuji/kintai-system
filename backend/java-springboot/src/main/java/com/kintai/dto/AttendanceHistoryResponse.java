package com.kintai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 勤怠履歴レスポンスDTO
 * 勤怠履歴取得結果のレスポンスデータ
 */
@Data
@Builder
public class AttendanceHistoryResponse {

    /**
     * 取得成功フラグ
     */
    private boolean success;

    /**
     * 社員情報
     */
    private EmployeeInfo employee;

    /**
     * 期間情報
     */
    private PeriodInfo period;

    /**
     * 勤怠情報一覧
     */
    private List<AttendanceInfo> attendanceList;

    /**
     * 勤怠サマリー
     */
    private AttendanceSummary summary;
}
