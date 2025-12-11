package org.example.hrm.util;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Component
@Slf4j
public class JwtTokenUtil {
    
    @Value("${jwt.secret}")
    private String secret;
    
    @Value("${jwt.expiration}")
    private Long expiration;
    
    @Value("${jwt.token-prefix:Bearer }")
    private String tokenPrefix;
    
    @Value("${jwt.header:Authorization}")
    private String header;
    
    public String getUsernameFromToken(String token) {
        return getClaimFromToken(token, Claims::getSubject);
    }
    
    public Date getExpirationDateFromToken(String token) {
        return getClaimFromToken(token, Claims::getExpiration);
    }
    
    public <T> T getClaimFromToken(String token, Function<Claims, T> claimsResolver) {
        final Claims claims = getAllClaimsFromToken(token);
        return claimsResolver.apply(claims);
    }
    
    private Claims getAllClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSecretKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.error("Token已过期", e);
            throw e;
        } catch (UnsupportedJwtException e) {
            log.error("不支持的Token格式", e);
            throw e;
        } catch (MalformedJwtException e) {
            log.error("Token格式错误", e);
            throw e;
        } catch (SignatureException e) {
            log.error("签名验证失败", e);
            throw e;
        } catch (Exception e) {
            log.error("Token解析失败", e);
            throw new JwtException("Token解析失败: " + e.getMessage());
        }
    }
    
    private SecretKey getSecretKey() {
        log.debug("使用密钥生成SecretKey，密钥长度: {}", secret.length());
        try {
            // 确保密钥长度足够
            byte[] keyBytes = secret.getBytes();
            int requiredLength = 64; // HS512需要至少64字节
            if (keyBytes.length < requiredLength) {
                byte[] newKeyBytes = new byte[requiredLength];
                System.arraycopy(keyBytes, 0, newKeyBytes, 0, Math.min(keyBytes.length, requiredLength));
                keyBytes = newKeyBytes;
            }
            return Keys.hmacShaKeyFor(keyBytes);
        } catch (Exception e) {
            log.error("生成SecretKey失败", e);
            throw e;
        }
    }
    
    private Boolean isTokenExpired(String token) {
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date());
    }
    
    public String generateToken(UserDetails userDetails, Integer roleType, Long userId) {
        log.info("开始生成Token，用户: {}, 角色类型: {}, 用户ID: {}", 
            userDetails.getUsername(), roleType, userId);
        
        Map<String, Object> claims = new HashMap<>();
        claims.put("roleType", roleType);
        claims.put("userId", userId);
        claims.put("authorities", userDetails.getAuthorities());
        
        return doGenerateToken(claims, userDetails.getUsername());
    }
    
    private String doGenerateToken(Map<String, Object> claims, String subject) {
        log.debug("生成Token，主题: {}, 声明: {}", subject, claims);
        
        final Date createdDate = new Date();
        final Date expirationDate = new Date(createdDate.getTime() + expiration);
        
        try {
            String token = Jwts.builder()
                    .setClaims(claims)
                    .setSubject(subject)
                    .setIssuedAt(createdDate)
                    .setExpiration(expirationDate)
                    .signWith(getSecretKey(), SignatureAlgorithm.HS512)
                    .compact();
            
            log.debug("Token生成成功，长度: {}", token.length());
            return token;
        } catch (Exception e) {
            log.error("生成Token失败", e);
            throw new RuntimeException("生成Token失败: " + e.getMessage(), e);
        }
    }
    
    public Boolean validateToken(String token, UserDetails userDetails) {
        final String username = getUsernameFromToken(token);
        return (username.equals(userDetails.getUsername()) && !isTokenExpired(token));
    }
    
    public String getTokenPrefix() {
        return tokenPrefix;
    }
    
    public String getHeader() {
        return header;
    }
}