package com.example.campussystem.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

/**
 * JWT工具类
 */
@Component
public class JwtUtil {

    private static final Logger logger = LoggerFactory.getLogger(JwtUtil.class);

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private Long expiration;

    @Value("${jwt.header}")
    private String header;

    @Value("${jwt.prefix}")
    private String prefix;

    /**
     * 生成JWT token（默认 USER 角色）
     */
    public String generateToken(Long userId, String username) {
        return generateToken(userId, username, "USER");
    }

    /**
     * 生成带角色的 JWT token
     */
    public String generateToken(Long userId, String username, String role) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", role == null || role.isBlank() ? "USER" : role.toUpperCase());
        return createToken(claims, username);
    }

    /**
     * 生成管理员JWT token
     */
    public String generateAdminToken(Long userId, String username) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("userId", userId);
        claims.put("username", username);
        claims.put("role", "ADMIN");
        
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expiration);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(subject)
                .setIssuedAt(now)
                .setExpiration(expiryDate)
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public Long getUserIdFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? Long.valueOf(claims.get("userId").toString()) : null;
    }

    public String getUsernameFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getSubject() : null;
    }

    public String getRoleFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? (String) claims.get("role") : null;
    }

    public Date getExpirationDateFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        return claims != null ? claims.getExpiration() : null;
    }

    private Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            logger.warn("JWT token已过期: {}", e.getMessage());
            return null;
        } catch (JwtException e) {
            logger.warn("JWT token解析失败: {}", e.getMessage());
            return null;
        } catch (IllegalArgumentException e) {
            logger.warn("JWT token参数错误: {}", e.getMessage());
            return null;
        }
    }

    public Boolean isTokenExpired(String token) {
        Date expiration = getExpirationDateFromToken(token);
        return expiration != null && expiration.before(new Date());
    }

    public Boolean validateToken(String token) {
        try {
            Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token);
            return !isTokenExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            logger.warn("JWT token验证失败: {}", e.getMessage());
            return false;
        }
    }

    public String getTokenFromHeader(String authHeader) {
        if (authHeader == null || authHeader.isBlank()) {
            return null;
        }
        String p = prefix == null ? "Bearer" : prefix.trim();
        String bearerPrefix = p + " ";
        if (authHeader.regionMatches(true, 0, bearerPrefix, 0, bearerPrefix.length())) {
            return authHeader.substring(bearerPrefix.length()).trim();
        }
        return null;
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        
        if (keyBytes.length < 32) {
            byte[] newKeyBytes = new byte[32];
            System.arraycopy(keyBytes, 0, newKeyBytes, 0, keyBytes.length);
            for (int i = keyBytes.length; i < 32; i++) {
                newKeyBytes[i] = (byte) (keyBytes[i % keyBytes.length] ^ i);
            }
            keyBytes = newKeyBytes;
        }
        
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String refreshToken(String token) {
        Claims claims = getClaimsFromToken(token);
        if (claims == null) return null;

        Long userId = Long.valueOf(claims.get("userId").toString());
        String username = claims.getSubject();
        String role = (String) claims.get("role");

        if ("ADMIN".equals(role)) {
            return generateAdminToken(userId, username);
        } else {
            return generateToken(userId, username);
        }
    }

    public String getHeader() { return header; }
    public String getPrefix() { return prefix; }
    public Long getExpiration() { return expiration; }
}
