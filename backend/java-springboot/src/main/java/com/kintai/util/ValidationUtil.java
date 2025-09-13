package com.kintai.util;

import org.springframework.stereotype.Component;

/**
 * バリデーションユーティリティ
 * 設計書のパスワードポリシー完全準拠
 */
@Component
public class ValidationUtil {
    
    /**
     * パスワード検証（設計書の詳細ルール完全準拠）
     * - 8-20文字
     * - 英字（大文字・小文字）、数字、記号を各1文字以上含む
     * - 連続する同一文字3文字以上禁止
     * - 社員IDと同一文字列禁止
     * 
     * @param password パスワード
     * @param employeeCode 社員コード
     * @return 有効な場合true
     */
    public static boolean validatePassword(String password, String employeeCode) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            return false;
        }
        
        // 英字（大文字・小文字）、数字、記号の各1文字以上チェック
        boolean hasLowerCase = password.matches(".*[a-z].*");
        boolean hasUpperCase = password.matches(".*[A-Z].*");
        boolean hasDigit = password.matches(".*\\d.*");
        boolean hasSpecialChar = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>\\?].*");
        
        if (!hasLowerCase || !hasUpperCase || !hasDigit || !hasSpecialChar) {
            return false;
        }
        
        // 連続する同一文字3文字以上禁止
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i + 1) == password.charAt(i + 2)) {
                return false;
            }
        }
        
        // 社員IDと同一文字列禁止
        if (password.equalsIgnoreCase(employeeCode)) {
            return false;
        }
        
        return true;
    }
    
    /**
     * 社員コード検証
     * @param code 社員コード
     * @return 有効な場合true
     */
    public static boolean isValidEmployeeCode(String code) {
        return code != null && code.matches("^[a-zA-Z0-9]{3,10}$");
    }
    
    /**
     * メールアドレス検証
     * @param email メールアドレス
     * @return 有効な場合true
     */
    public static boolean isValidEmail(String email) {
        return email != null && email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$");
    }
    
    /**
     * 年月形式検証（YYYY-MM）
     * @param yearMonth 年月文字列
     * @return 有効な場合true
     */
    public static boolean isValidYearMonth(String yearMonth) {
        return yearMonth != null && yearMonth.matches("^\\d{4}-\\d{2}$");
    }
    
    /**
     * 日付形式検証（YYYY-MM-DD）
     * @param date 日付文字列
     * @return 有効な場合true
     */
    public static boolean isValidDateFormat(String date) {
        return date != null && date.matches("^\\d{4}-\\d{2}-\\d{2}$");
    }
    
    /**
     * 時刻形式検証（HH:MM:SS）
     * @param time 時刻文字列
     * @return 有効な場合true
     */
    public static boolean isValidTimeFormat(String time) {
        return time != null && time.matches("^\\d{2}:\\d{2}:\\d{2}$");
    }
    
    /**
     * 文字列がnullまたは空でないかチェック
     * @param value チェック対象文字列
     * @return nullまたは空でない場合true
     */
    public static boolean isNotBlank(String value) {
        return value != null && !value.trim().isEmpty();
    }
    
    /**
     * 文字列がnullまたは空かチェック
     * @param value チェック対象文字列
     * @return nullまたは空の場合true
     */
    public static boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
    
    /**
     * 数値範囲チェック
     * @param value チェック対象数値
     * @param min 最小値
     * @param max 最大値
     * @return 範囲内の場合true
     */
    public static boolean isInRange(int value, int min, int max) {
        return value >= min && value <= max;
    }
    
    /**
     * 文字列長チェック
     * @param value チェック対象文字列
     * @param minLength 最小長
     * @param maxLength 最大長
     * @return 範囲内の場合true
     */
    public static boolean isLengthInRange(String value, int minLength, int maxLength) {
        if (value == null) {
            return false;
        }
        return value.length() >= minLength && value.length() <= maxLength;
    }
}
