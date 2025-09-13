package com.kintai.util;

import org.springframework.stereotype.Component;

/**
 * データマスキングユーティリティ
 * 個人情報保護のためのデータマスキング機能
 */
@Component
public class DataMaskingUtil {
    
    /**
     * メールアドレスマスキング
     */
    public String maskEmail(String email) {
        if (email == null || !email.contains("@")) {
            return "***";
        }
        
        String[] parts = email.split("@");
        String localPart = parts[0];
        String domainPart = parts[1];
        
        if (localPart.length() <= 2) {
            return "***@" + domainPart;
        }
        
        return localPart.charAt(0) + "***" + localPart.charAt(localPart.length() - 1) + "@" + domainPart;
    }
    
    /**
     * 氏名マスキング
     */
    public String maskName(String name) {
        if (name == null || name.length() <= 1) {
            return "***";
        }
        
        if (name.length() == 2) {
            return name.charAt(0) + "*";
        }
        
        StringBuilder masked = new StringBuilder();
        masked.append(name.charAt(0));
        for (int i = 1; i < name.length() - 1; i++) {
            masked.append("*");
        }
        masked.append(name.charAt(name.length() - 1));
        
        return masked.toString();
    }
    
    /**
     * 社員コード部分マスキング
     */
    public String maskEmployeeCode(String code) {
        if (code == null || code.length() <= 2) {
            return "***";
        }
        
        return code.substring(0, 2) + "***";
    }
    
    /**
     * 電話番号マスキング
     */
    public String maskPhoneNumber(String phoneNumber) {
        if (phoneNumber == null || phoneNumber.length() < 4) {
            return "***";
        }
        
        return phoneNumber.substring(0, 3) + "****" + phoneNumber.substring(phoneNumber.length() - 1);
    }
    
    /**
     * 住所部分マスキング
     */
    public String maskAddress(String address) {
        if (address == null || address.length() <= 3) {
            return "***";
        }
        
        return address.substring(0, 3) + "***";
    }
}
