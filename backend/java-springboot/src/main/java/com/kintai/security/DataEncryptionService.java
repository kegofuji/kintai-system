package com.kintai.security;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * データ暗号化サービス
 * 個人情報の暗号化・復号化を提供
 */
@Service
@RequiredArgsConstructor
public class DataEncryptionService {
    
    private final AESUtil aesUtil;
    
    @Value("${app.encryption.key:default-encryption-key-change-in-production}")
    private String encryptionKey;
    
    /**
     * 個人情報暗号化
     */
    public String encryptPersonalData(String plainText) {
        if (plainText == null) return null;
        try {
            return aesUtil.encrypt(plainText, encryptionKey);
        } catch (Exception e) {
            throw new RuntimeException("暗号化に失敗しました", e);
        }
    }
    
    /**
     * 個人情報復号化
     */
    public String decryptPersonalData(String encryptedText) {
        if (encryptedText == null) return null;
        try {
            return aesUtil.decrypt(encryptedText, encryptionKey);
        } catch (Exception e) {
            throw new RuntimeException("復号化に失敗しました", e);
        }
    }
}
