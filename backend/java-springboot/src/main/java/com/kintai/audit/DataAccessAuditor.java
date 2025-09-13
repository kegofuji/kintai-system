package com.kintai.audit;

import com.kintai.entity.Employee;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * データアクセス監査
 * JPA監査機能でデータ変更者を自動記録
 */
@Configuration
@EnableJpaAuditing
public class DataAccessAuditor {
    
    @Component
    public static class AuditorAwareImpl implements AuditorAware<String> {
        
        @Override
        public Optional<String> getCurrentAuditor() {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            if (authentication == null || !authentication.isAuthenticated()) {
                return Optional.of("SYSTEM");
            }
            
            if (authentication.getPrincipal() instanceof Employee) {
                Employee employee = (Employee) authentication.getPrincipal();
                return Optional.of(employee.getEmployeeCode());
            }
            
            return Optional.of(authentication.getName());
        }
    }
}
