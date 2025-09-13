package com.kintai.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * レート制限フィルター
 */
public class RateLimitingFilter implements Filter {
    
    private final Map<String, List<Long>> clientRequestTimes = new ConcurrentHashMap<>();
    private static final int MAX_REQUESTS = 100; // 1時間あたり
    private static final long TIME_WINDOW = 3600000; // 1時間（ミリ秒）
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String clientIp = getClientIP(httpRequest);
        long currentTime = System.currentTimeMillis();
        
        List<Long> requestTimes = clientRequestTimes.computeIfAbsent(clientIp, 
            k -> new ArrayList<>());
        
        // 古いリクエストを削除
        requestTimes.removeIf(time -> currentTime - time > TIME_WINDOW);
        
        if (requestTimes.size() >= MAX_REQUESTS) {
            httpResponse.setStatus(429); // Too Many Requests
            httpResponse.setContentType("application/json");
            httpResponse.getWriter().write("{\"error\":\"Too Many Requests\",\"message\":\"レート制限に達しました。しばらく時間をおいてから再度お試しください。\"}");
            return;
        }
        
        requestTimes.add(currentTime);
        chain.doFilter(request, response);
    }
    
    private String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");
        if (xfHeader == null) {
            return request.getRemoteAddr();
        }
        return xfHeader.split(",")[0];
    }
}
