package com.kintai;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class KintaiApplication {
    public static void main(String[] args) {
        SpringApplication.run(KintaiApplication.class, args);
    }
}