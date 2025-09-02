package com.kintai.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.jdbc.config.annotation.web.http.EnableJdbcHttpSession;

@Configuration
@EnableJdbcHttpSession(maxInactiveIntervalInSeconds = 600) // 10分
public class DatabaseConfig {
    
    // JDBCセッション設定は@EnableJdbcHttpSessionで自動構成
}