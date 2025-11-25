package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;

@Service
@RequiredArgsConstructor
@Slf4j
public class TokenBlacklistServiceImpl implements TokenBlacklistService {
    private static final String BLACKLIST_PREFIX = "blacklist:token:";

    private final RedisTemplate<String, String> redisTemplate;
    private final JwtDecoder jwtDecoder;

    @Override
    public void blacklistToken(String token) {
        Jwt jwt = jwtDecoder.decode(token);
        String jti = jwt.getId(); // JWT ID
        Instant expiresAt = jwt.getExpiresAt();

        if (expiresAt != null) {
            long ttl = Duration.between(Instant.now(), expiresAt).getSeconds();
            if (ttl > 0) {
                String key = BLACKLIST_PREFIX + jti;
                redisTemplate.opsForValue().set(key, "blacklisted", Duration.ofSeconds(ttl));
                log.info("Token blacklisted with JTI: {}, TTL: {} seconds", jti, ttl);
            }
        }
    }

    @Override
    public boolean isJtiBlacklisted(String jti) {
        String key = BLACKLIST_PREFIX + jti;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
