package com.kintai.util;

import java.util.regex.Pattern;

public class ValidationUtil {
    
    private static final Pattern EMPLOYEE_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,10}$");
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@$!%*?&])[A-Za-z\\d@$!%*?&]{8,20}$");
    
    /**
     * パスワード詳細バリデーション
     */
    public static boolean isValidPassword(String password, String employeeCode) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            return false;
        }
        
        // 英字（大文字・小文字）、数字、記号を各1文字以上含む
        if (!PASSWORD_PATTERN.matcher(password).matches()) {
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
        if (employeeCode != null && password.contains(employeeCode)) {
            return false;
        }
        
        // よくあるパスワード禁止
        String[] commonPasswords = {
            "password", "123456", "123456789", "qwerty", "abc123",
            "password123", "admin", "login", "welcome"
        };
        String lowerPassword = password.toLowerCase();
        for (String common : commonPasswords) {
            if (lowerPassword.contains(common)) {
                return false;
            }
        }
        
        return true;
    }
    
    /**
     * 社員コードバリデーション
     */
    public static boolean isValidEmployeeCode(String employeeCode) {
        return employeeCode != null && EMPLOYEE_CODE_PATTERN.matcher(employeeCode).matches();
    }
    
    /**
     * メールアドレスバリデーション
     */
    public static boolean isValidEmail(String email) {
        return email != null && EMAIL_PATTERN.matcher(email).matches();
    }
    
    /**
     * パスワード要件エラーメッセージ生成
     */
    public static String getPasswordValidationMessage(String password, String employeeCode) {
        if (password == null || password.length() < 8 || password.length() > 20) {
            return "パスワードは8-20文字で入力してください";
        }
        
        if (!password.matches(".*[a-z].*")) {
            return "パスワードには小文字を1文字以上含めてください";
        }
        
        if (!password.matches(".*[A-Z].*")) {
            return "パスワードには大文字を1文字以上含めてください";
        }
        
        if (!password.matches(".*\\d.*")) {
            return "パスワードには数字を1文字以上含めてください";
        }
        
        if (!password.matches(".*[@$!%*?&].*")) {
            return "パスワードには記号(@$!%*?&)を1文字以上含めてください";
        }
        
        for (int i = 0; i < password.length() - 2; i++) {
            if (password.charAt(i) == password.charAt(i + 1) && 
                password.charAt(i + 1) == password.charAt(i + 2)) {
                return "パスワードに同一文字を3文字以上連続で使用することはできません";
            }
        }
        
        if (employeeCode != null && password.contains(employeeCode)) {
            return "パスワードに社員IDを含めることはできません";
        }
        
        return null;
    }
}