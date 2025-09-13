package com.kintai.dto;

import lombok.Builder;
import lombok.Data;

/**
 * 期間情報DTO
 * 勤怠履歴の期間情報
 */
@Data
@Builder
public class PeriodInfo {

    /**
     * 開始日（YYYY-MM-DD形式）
     */
    private String from;

    /**
     * 終了日（YYYY-MM-DD形式）
     */
    private String to;
}
