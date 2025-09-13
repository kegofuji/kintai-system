package com.kintai.util;

import org.owasp.encoder.Encode;
import org.springframework.stereotype.Component;

/**
 * 入力サニタイゼーション
 */
@Component
public class InputSanitizer {
    
    /**
     * HTMLタグを除去
     */
    public String sanitizeHtml(String input) {
        if (input == null) return null;
        return input.replaceAll("<[^>]*>", "");
    }
    
    /**
     * SQLインジェクション対策
     */
    public String sanitizeSql(String input) {
        if (input == null) return null;
        return input.replaceAll("['\"\\\\;]", "");
    }
    
    /**
     * XSS対策
     */
    public String sanitizeXss(String input) {
        if (input == null) return null;
        return input.replace("<", "&lt;")
                   .replace(">", "&gt;")
                   .replace("\"", "&quot;")
                   .replace("'", "&#x27;")
                   .replace("&", "&amp;");
    }
    
    /**
     * ファイルパス対策
     */
    public String sanitizeFilePath(String input) {
        if (input == null) return null;
        return input.replaceAll("[^a-zA-Z0-9._-]", "");
    }
    
    /**
     * URLエンコード
     */
    public String sanitizeUrl(String input) {
        if (input == null) return null;
        return Encode.forUriComponent(input);
    }
    
    /**
     * JavaScriptエンコード
     */
    public String sanitizeJavaScript(String input) {
        if (input == null) return null;
        return Encode.forJavaScript(input);
    }
    
    /**
     * 総合サニタイゼーション（すべての対策を適用）
     */
    public String sanitizeAll(String input) {
        if (input == null) return null;
        
        String sanitized = input;
        sanitized = sanitizeHtml(sanitized);
        sanitized = sanitizeSql(sanitized);
        sanitized = sanitizeXss(sanitized);
        
        return sanitized;
    }
}
