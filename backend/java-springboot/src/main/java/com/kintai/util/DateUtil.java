package com.kintai.util;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DateUtil {
    
    private static final DateTimeFormatter YEAR_MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    
    /**
     * 営業日一覧を取得（土日祝日除外）
     * ※祝日は簡略化のため未実装
     */
    public static List<LocalDate> getWorkingDays(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER);
        List<LocalDate> workingDays = new ArrayList<>();
        
        LocalDate start = ym.atDay(1);
        LocalDate end = ym.atEndOfMonth();
        
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workingDays.add(current);
            }
            current = current.plusDays(1);
        }
        
        return workingDays;
    }
    
    /**
     * 営業日一覧を取得（期間指定）
     */
    public static List<LocalDate> getWorkingDaysBetween(LocalDate start, LocalDate end) {
        List<LocalDate> workingDays = new ArrayList<>();
        
        LocalDate current = start;
        while (!current.isAfter(end)) {
            DayOfWeek dayOfWeek = current.getDayOfWeek();
            if (dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY) {
                workingDays.add(current);
            }
            current = current.plusDays(1);
        }
        
        return workingDays;
    }
    
    /**
     * 年月文字列をLocalDateの範囲に変換
     */
    public static LocalDate[] getMonthRange(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, YEAR_MONTH_FORMATTER);
        return new LocalDate[]{ym.atDay(1), ym.atEndOfMonth()};
    }
    
    /**
     * 営業日かどうかを判定
     */
    public static boolean isWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
    
    /**
     * 日付文字列をLocalDateに変換
     */
    public static LocalDate parseDate(String dateString) {
        return LocalDate.parse(dateString, DATE_FORMATTER);
    }
    
    /**
     * LocalDateを文字列に変換
     */
    public static String formatDate(LocalDate date) {
        return date.format(DATE_FORMATTER);
    }
    
    /**
     * 年月を文字列に変換
     */
    public static String formatYearMonth(LocalDate date) {
        return date.format(YEAR_MONTH_FORMATTER);
    }
    
    /**
     * 現在日付が指定された年月に含まれるかチェック
     */
    public static boolean isCurrentMonth(String yearMonth) {
        LocalDate today = LocalDate.now();
        String currentYearMonth = formatYearMonth(today);
        return currentYearMonth.equals(yearMonth);
    }
}