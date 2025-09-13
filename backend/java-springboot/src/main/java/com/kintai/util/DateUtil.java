package com.kintai.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

/**
 * 日付ユーティリティ
 * 日付・時刻関連の共通処理
 */
@Component
public class DateUtil {
    
    /**
     * 現在日時取得（Asia/Tokyoタイムゾーン）
     * @return 現在日時
     */
    public static LocalDateTime getCurrentDateTime() {
        return LocalDateTime.now(ZoneId.of("Asia/Tokyo"));
    }
    
    /**
     * 現在日付取得
     * @return 現在日付
     */
    public static LocalDate getCurrentDate() {
        return LocalDate.now(ZoneId.of("Asia/Tokyo"));
    }
    
    /**
     * 分をHH:MM形式に変換
     * @param minutes 分
     * @return HH:MM形式の文字列
     */
    public static String formatTimeToHHMM(int minutes) {
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%02d:%02d", hours, mins);
    }
    
    /**
     * 営業日取得（土日祝除外）
     * @param yearMonth 年月（YYYY-MM形式）
     * @return 営業日一覧
     */
    public static List<LocalDate> getWorkingDays(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth);
        List<LocalDate> workingDays = new ArrayList<>();
        
        for (int day = 1; day <= ym.lengthOfMonth(); day++) {
            LocalDate date = ym.atDay(day);
            // 土日を除外（祝日判定は省略）
            if (!date.getDayOfWeek().equals(DayOfWeek.SATURDAY) && 
                !date.getDayOfWeek().equals(DayOfWeek.SUNDAY)) {
                workingDays.add(date);
            }
        }
        return workingDays;
    }
    
    /**
     * 日付をYYYY-MM-DD形式に変換
     * @param date 日付
     * @return YYYY-MM-DD形式の文字列
     */
    public static String formatDateToString(LocalDate date) {
        return date.toString();
    }
    
    /**
     * 日時をYYYY-MM-DD HH:MM:SS形式に変換
     * @param dateTime 日時
     * @return YYYY-MM-DD HH:MM:SS形式の文字列
     */
    public static String formatDateTimeToString(LocalDateTime dateTime) {
        return dateTime.toString().replace("T", " ");
    }
    
    /**
     * 時刻をHH:MM:SS形式に変換
     * @param dateTime 日時
     * @return HH:MM:SS形式の文字列
     */
    public static String formatTimeToString(LocalDateTime dateTime) {
        return dateTime.toLocalTime().toString();
    }
    
    /**
     * 年月文字列をYearMonthに変換
     * @param yearMonth 年月（YYYY-MM形式）
     * @return YearMonth
     */
    public static YearMonth parseYearMonth(String yearMonth) {
        return YearMonth.parse(yearMonth);
    }
    
    /**
     * 日付が営業日かどうか判定
     * @param date 日付
     * @return 営業日の場合true
     */
    public static boolean isWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return !dayOfWeek.equals(DayOfWeek.SATURDAY) && 
               !dayOfWeek.equals(DayOfWeek.SUNDAY);
    }
    
    /**
     * 指定月の営業日数を取得
     * @param yearMonth 年月（YYYY-MM形式）
     * @return 営業日数
     */
    public static int getWorkingDaysCount(String yearMonth) {
        return getWorkingDays(yearMonth).size();
    }
}
