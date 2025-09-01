package com.kintai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@Configuration
@EnableJpaRepositories(basePackages = "com.kintai.repository")
@EnableTransactionManagement
public class DatabaseConfig {
    // JPA設定はapplication.ymlで管理
}
