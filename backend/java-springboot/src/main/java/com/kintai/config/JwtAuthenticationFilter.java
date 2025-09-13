package com.kintai.config;

import com.kintai.entity.Employee;
import com.kintai.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT認証フィルター
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtUtil jwtUtil;
    
    @Override
    protected void doFilterInternal(HttpServletRequest request, 
                                  HttpServletResponse response, 
                                  FilterChain filterChain) throws ServletException, IOException {
        
        String token = extractToken(request);
        
        if (token != null && jwtUtil.validateToken(token)) {
            try {
                Employee employee = jwtUtil.getEmployeeFromToken(token);
                
                // 退職者チェック
                if ("retired".equals(employee.getEmploymentStatus().getValue())) {
                    response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                    response.getWriter().write("{\"success\":false,\"errorCode\":\"ACCESS_DENIED\",\"message\":\"アクセス権限がありません\"}");
                    return;
                }
                
                // Spring Security認証情報設定
                List<GrantedAuthority> authorities = List.of(
                        new SimpleGrantedAuthority("ROLE_" + employee.getEmployeeRole().getValue().toUpperCase())
                );
                
                UsernamePasswordAuthenticationToken auth = 
                        new UsernamePasswordAuthenticationToken(employee, null, authorities);
                SecurityContextHolder.getContext().setAuthentication(auth);
                
            } catch (Exception e) {
                log.error("Cannot set employee authentication: {}", e.getMessage());
            }
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
