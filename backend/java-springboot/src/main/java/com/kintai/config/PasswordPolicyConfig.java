package com.kintai.config;

import org.passay.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * パスワードポリシー設定
 */
@Configuration
public class PasswordPolicyConfig {
    
    @Bean
    public PasswordValidator passwordValidator() {
        List<Rule> rules = new ArrayList<>();
        
        // 長さチェック (8-20文字)
        rules.add(new LengthRule(8, 20));
        
        // 文字種チェック
        rules.add(new CharacterRule(EnglishCharacterData.UpperCase, 1));
        rules.add(new CharacterRule(EnglishCharacterData.LowerCase, 1)); 
        rules.add(new CharacterRule(EnglishCharacterData.Digit, 1));
        rules.add(new CharacterRule(EnglishCharacterData.Special, 1));
        
        // 連続同一文字禁止
        rules.add(new RepeatCharacterRegexRule(3, false));
        
        // 辞書攻撃対策（基本的な禁止ワード）
        // 注意: 本格的な辞書攻撃対策には外部辞書ファイルが必要
        
        // 空白文字禁止
        rules.add(new WhitespaceRule());
        
        return new PasswordValidator(rules);
    }
}
