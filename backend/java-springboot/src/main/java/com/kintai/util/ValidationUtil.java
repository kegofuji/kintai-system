package com.kintai.util;

import com.kintai.exception.ValidationException;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.*;
import java.util.regex.Pattern;

@Component
public class ValidationUtil {
    
    private static final Pattern EMAIL_PATTERN = Pattern.compile(
        "^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"
    );
    
    private static final Pattern EMPLOYEE_CODE_PATTERN = Pattern.compile("^[a-zA-Z0-9]{3,10}$");
    
    private static final Set<String> WEAK_PASSWORDS = Set.of(
        "password", "123456", "password123", "admin", "qwerty", "abc123"
    );
    
    /**
     * 社員コードバリデーション
     */
    public void validateEmployeeCode(String employeeCode) {
        Map<String, List<String>> errors = new HashMap<>();
        
        if (employeeCode == null || employeeCode.trim().isEmpty()) {
            errors.put("employeeCode", List.of("社員コードは必須です"));
        } else if (!EMPLOYEE_CODE_PATTERN.matcher(employeeCode).matches()) {
            errors.put("employeeCode", List.of("社員コードは3-10文字の半角英数字で入力してください"));
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("社員コードが不正です", errors);
        }
    }
    
    /**
     * パスワードバリデーション
     */
    public void validatePassword(String password, String employeeCode) {
        Map<String, List<String>> errors = new HashMap<>();
        List<String> passwordErrors = new ArrayList<>();
        
        if (password == null || password.length() < 8 || password.length() > 20) {
            passwordErrors.add("パスワードは8-20文字で入力してください");
        } else {
            // 英字、数字、記号を各1文字以上含む
            boolean hasUpper = password.matches(".*[A-Z].*");
            boolean hasLower = password.matches(".*[a-z].*");
            boolean hasDigit = password.matches(".*\\d.*");
            boolean hasSymbol = password.matches(".*[!@#$%^&*()_+\\-=\\[\\]{};':\"\\\\|,.<>/?].*");
            
            if (!hasUpper || !hasLower || !hasDigit || !hasSymbol) {
                passwordErrors.add("パスワードは英字（大文字・小文字）、数字、記号を各1文字以上含めてください");
            }
            
            // 連続する同一文字3文字以上禁止
            for (int i = 0; i < password.length() - 2; i++) {
                if (password.charAt(i) == password.charAt(i + 1) && 
                    password.charAt(i + 1) == password.charAt(i + 2)) {
                    passwordErrors.add("連続する同一文字は3文字以上使用できません");
                    break;
                }
            }
            
            // 社員IDと同一文字列禁止
            if (employeeCode != null && password.equals(employeeCode)) {
                passwordErrors.add("パスワードは社員コードと異なる文字列にしてください");
            }
            
            // よくあるパスワード禁止
            if (WEAK_PASSWORDS.contains(password.toLowerCase())) {
                passwordErrors.add("より安全なパスワードを設定してください");
            }
        }
        
        if (!passwordErrors.isEmpty()) {
            errors.put("password", passwordErrors);
            throw new ValidationException("パスワードが不正です", errors);
        }
    }
    
    /**
     * メールアドレスバリデーション
     */
    public void validateEmail(String email) {
        Map<String, List<String>> errors = new HashMap<>();
        
        if (email == null || email.trim().isEmpty()) {
            errors.put("email", List.of("メールアドレスは必須です"));
        } else if (email.length() > 100) {
            errors.put("email", List.of("メールアドレスは100文字以内で入力してください"));
        } else if (!EMAIL_PATTERN.matcher(email).matches()) {
            errors.put("email", List.of("正しいメールアドレス形式で入力してください"));
        }
        
        if (!errors.isEmpty()) {
            throw new ValidationException("メールアドレスが不正です", errors);
        }
    }
    
    /**
     * 必須項目バリデーション
     */
    public void validateRequired(String value, String fieldName) {
        if (value == null || value.trim().isEmpty()) {
            Map<String, List<String>> errors = new HashMap<>();
            errors.put(fieldName, List.of(fieldName + "は必須です"));
            throw new ValidationException("必須項目が未入力です", errors);
        }
    }
    
    /**
     * 文字列長バリデーション
     */
    public void validateMaxLength(String value, int maxLength, String fieldName) {
        if (value != null && value.length() > maxLength) {
            Map<String, List<String>> errors = new HashMap<>();
            errors.put(fieldName, List.of(fieldName + "は" + maxLength + "文字以内で入力してください"));
            throw new ValidationException("文字数上限を超過しています", errors);
        }
    }
    
    /**
     * 未来日バリデーション
     */
    public void validateFutureDate(LocalDate date, String fieldName) {
        if (date == null) {
            Map<String, List<String>> errors = new HashMap<>();
            errors.put(fieldName, List.of(fieldName + "は必須です"));
            throw new ValidationException("日付が未入力です", errors);
        }
        
        if (!date.isAfter(LocalDate.now())) {
            Map<String, List<String>> errors = new HashMap<>();
            errors.put(fieldName, List.of(fieldName + "は明日以降の日付を選択してください"));
            throw new ValidationException("日付が不正です", errors);
        }
    }
    
    /**
     * 過去日または当日バリデーション
     */
    public void validatePastOrTodayDate(LocalDate date, String fieldName) {
        if (date == null) {
            Map<String, List<String>> errors = new HashMap<>();
            errors.put(fieldName, List.of(fieldName + "は必須です"));
            throw new ValidationException("日付が未入力です", errors);
        }
        
        if (date.isAfter(LocalDate.now())) {
            Map<String, List<String>> errors = new HashMap<>();
            errors.put(fieldName, List.of(fieldName + "は今日または過去の日付を選択してください"));
            throw new ValidationException("日付が不正です", errors);
        }
    }
    
    /**
     * 数値範囲バリデーション
     */
    public void validateRange(Integer value, int min, int max, String fieldName) {
        if (value == null) {
            Map<String, List<String>> errors = new HashMap<>();
            errors.put(fieldName, List.of(fieldName + "は必須です"));
            throw new ValidationException("数値が未入力です", errors);
        }
        
        if (value < min || value > max) {
            Map<String, List<String>> errors = new HashMap<>();
            errors.put(fieldName, List.of(fieldName + "は" + min + "～" + max + "の範囲で入力してください"));
            throw new ValidationException("数値範囲が不正です", errors);
        }
    }
}