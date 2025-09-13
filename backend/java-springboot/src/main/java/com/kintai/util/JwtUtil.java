package com.kintai.util;

import com.kintai.entity.Employee;
import com.kintai.exception.BusinessException;
import com.kintai.repository.EmployeeRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWTトークン管理ユーティリティ
 */
@Component
@Slf4j
public class JwtUtil {
    
    @Value("${jwt.secret}")
    private String jwtSecret;
    
    @Value("${jwt.expiration:600}")  // 10分 = 600秒
    private int jwtExpirationInSec;
    
    private final EmployeeRepository employeeRepository;
    
    public JwtUtil(EmployeeRepository employeeRepository) {
        this.employeeRepository = employeeRepository;
    }
    
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
    
    /**
     * JWTトークン生成
     */
    public String generateToken(Employee employee) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpirationInSec * 1000);
        
        return Jwts.builder()
                .subject(employee.getEmployeeCode())
                .issuedAt(now)
                .expiration(expiryDate)
                .claim("employeeId", employee.getEmployeeId())
                .claim("role", employee.getEmployeeRole())
                .claim("employmentStatus", employee.getEmploymentStatus())
                .signWith(getSigningKey())
                .compact();
    }
    
    /**
     * トークン検証
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (ExpiredJwtException e) {
            log.warn("Expired JWT token: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.warn("Unsupported JWT token: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.warn("Invalid JWT token: {}", e.getMessage());
        } catch (SecurityException e) {
            log.warn("Invalid JWT signature: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.warn("JWT token compact of handler are invalid: {}", e.getMessage());
        }
        return false;
    }
    
    /**
     * トークンから社員情報取得
     */
    public Employee getEmployeeFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
        
        String employeeCode = claims.getSubject();
        return employeeRepository.findByEmployeeCode(employeeCode)
                .orElseThrow(() -> new BusinessException("AUTH_FAILED", "認証に失敗しました"));
    }
    
    /**
     * トークン有効期限チェック
     */
    public boolean isTokenExpired(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            
            return claims.getExpiration().before(new Date());
        } catch (Exception e) {
            return true;
        }
    }
}
