package com.kintai.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new SessionTimeoutInterceptor())
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
    }
    
    /**
     * セッションタイムアウト制御インターセプター
     */
    public static class SessionTimeoutInterceptor extends HandlerInterceptorAdapter {
        
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, 
                               Object handler) throws Exception {
            
            HttpSession session = request.getSession(false);
            if (session == null || session.getAttribute("employeeId") == null) {
                response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                response.setContentType("application/json");
                response.setCharacterEncoding("UTF-8");
                response.getWriter().write(
                    "{\"success\":false,\"errorCode\":\"SESSION_TIMEOUT\"," +
                    "\"message\":\"セッションがタイムアウトしました\"}"
                );
                return false;
            }
            
            // セッションを延長
            session.setMaxInactiveInterval(600); // 10分
            return true;
        }
    }
}