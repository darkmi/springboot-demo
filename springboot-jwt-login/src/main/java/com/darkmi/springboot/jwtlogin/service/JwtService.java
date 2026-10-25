package com.darkmi.springboot.jwtlogin.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    private final SecretKey key;
    private final long expireMillis;

    public JwtService(@Value("${jwt.secret}") String secret,
                      @Value("${jwt.expire-millis}") long expireMillis) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.expireMillis = expireMillis;
    }

    /**
     * 签发 token：subject 放用户标识，过期时间由服务端控制
     */
    public String issue(String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())   // jti：token 唯一标识，登出黑名单、防重放都靠它
                .subject(username)
                .issuedAt(new Date(now))
                .expiration(new Date(now + expireMillis))
                .signWith(key, Jwts.SIG.HS256)      // 显式指定算法，避免密钥变长后自动升级导致算法漂移
                .compact();
    }

    /**
     * 解析 token：验签与过期校验一步完成
     * 过期抛 ExpiredJwtException，签名不匹配、格式错误抛 JwtException 的其他子类
     */
    public Claims parse(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
