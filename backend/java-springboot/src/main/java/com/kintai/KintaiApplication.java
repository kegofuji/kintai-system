package com.kintai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 勤怠管理システム メインアプリケーション
 * Spring Boot アプリケーションのエントリーポイント
 */
@SpringBootApplication
public class KintaiApplication {

    public static void main(String[] args) {
        SpringApplication.run(KintaiApplication.class, args);
    }
}
