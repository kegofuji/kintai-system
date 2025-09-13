package com.kintai.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;

/**
 * 月末申請レスポンスDTO
 * 月末勤怠申請結果のレスポンスデータ
 */
@Data
@Builder
public class MonthlySubmitResponse {

    /**
     * 申請成功フラグ
     */
    private boolean success;

    /**
     * 申請月
     */
    private String submissionMonth;

    /**
     * 勤務日数
     */
    private Integer workingDaysCount;

    /**
     * 完了日数
     */
    private Integer completedDaysCount;

    /**
     * 有給取得日数
     */
    private Integer paidLeaveDaysCount;

    /**
     * メッセージ
     */
    private String message;

    /**
     * 未入力日一覧（エラー時）
     */
    private List<String> missingDates;

    /**
     * 欠勤日一覧（エラー時）
     */
    private List<String> absentDates;
}
