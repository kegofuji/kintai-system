package com.kintai.util;

import org.springframework.stereotype.Component;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Component
public class DateUtil {
    
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter MONTH_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM");
    
    /**
     * 指定月の営業日一覧を取得（土日祝日除外、祝日は簡易実装）
     * @param yearMonth YYYY-MM形式
     * @return 営業日リスト
     */
    public List<LocalDate> getWorkingDays(String yearMonth) {
        YearMonth ym = YearMonth.parse(yearMonth, MONTH_FORMATTER);
        LocalDate startDate = ym.atDay(1);
        LocalDate endDate = ym.atEndOfMonth();
        
        List<LocalDate> workingDays = new ArrayList<>();
        
        for (LocalDate date = startDate; !date.isAfter(endDate); date = date.plusDays(1)) {
            if (isWorkingDay(date)) {
                workingDays.add(date);
            }
        }
        
        return workingDays;
    }
    
    /**
     * 営業日判定（土日を除外）
     * @param date 対象日
     * @return 営業日かどうか
     */
    public boolean isWorkingDay(LocalDate date) {
        DayOfWeek dayOfWeek = date.getDayOfWeek();
        return dayOfWeek != DayOfWeek.SATURDAY && dayOfWeek != DayOfWeek.SUNDAY;
    }
    
    /**
     * 時間（分）をHH:MM形式に変換
     * @param minutes 分
     * @return HH:MM形式文字列
     */
    public String formatMinutesToTime(Integer minutes) {
        if (minutes == null || minutes == 0) {
            return "00:00";
        }
        int hours = minutes / 60;
        int mins = minutes % 60;
        return String.format("%02d:%02d", hours, mins);
    }
    
    /**
     * LocalDateを文字列に変換
     * @param date LocalDate
     * @return yyyy-MM-dd形式文字列
     */
    public String formatDate(LocalDate date) {
        return date != null ? date.format(FORMATTER) : null;
    }
    
    /**
     * 文字列をLocalDateに変換
     * @param dateStr yyyy-MM-dd形式文字列
     * @return LocalDate
     */
    public LocalDate parseDate(String dateStr) {
        return dateStr != null ? LocalDate.parse(dateStr, FORMATTER) : null;
    }
    
    /**
     * 年月文字列から年を取得
     * @param yearMonth yyyy-MM形式
     * @return 年
     */
    public int extractYear(String yearMonth) {
        return YearMonth.parse(yearMonth, MONTH_FORMATTER).getYear();
    }
    
    /**
     * 年月文字列から月を取得
     * @param yearMonth yyyy-MM形式
     * @return 月（1-12）
     */
    public int extractMonth(String yearMonth) {
        return YearMonth.parse(yearMonth, MONTH_FORMATTER).getMonthValue();
    }
    
    /**
     * 現在の年月を取得
     * @return yyyy-MM形式文字列
     */
    public String getCurrentYearMonth() {
        return YearMonth.now().format(MONTH_FORMATTER);
    }
    
    /**
     * 未来日判定
     * @param date 対象日
     * @return 未来日かどうか
     */
    public boolean isFutureDate(LocalDate date) {
        return date.isAfter(LocalDate.now());
    }
    
    /**
     * 過去日判定（当日を含む）
     * @param date 対象日
     * @return 過去日または当日かどうか
     */
    public boolean isPastOrToday(LocalDate date) {
        return !date.isAfter(LocalDate.now());
    }
}