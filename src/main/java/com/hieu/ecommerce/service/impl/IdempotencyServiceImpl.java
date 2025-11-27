package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.IdempotencyStatus;
import com.hieu.ecommerce.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

/**
 * Implementation của IdempotencyService sử dụng Redis
 * 
 * @author hieu
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class IdempotencyServiceImpl implements IdempotencyService {

    private final RedisTemplate<String, String> redisTemplate;
    private static final String PROCESSED_PREFIX = "PROCESSED:";

    @Override
    public boolean reserveKey(String key, Duration ttl, String prefix) {
        String redisKey = normalizePrefix(prefix) + key;
        Boolean success = redisTemplate.opsForValue().setIfAbsent(
                redisKey, IdempotencyStatus.RESERVED.name(), ttl
        );
        if (Boolean.TRUE.equals(success)) {
            log.debug("Idempotency key reserved: {}", key);
        }
        return Boolean.TRUE.equals(success);
    }

    @Override
    public void markAsProcessed(String key, String responseJson, Duration ttl, String prefix) {
        String redisKey = normalizePrefix(prefix) + key;
        String value = PROCESSED_PREFIX + responseJson;
        redisTemplate.opsForValue().set(redisKey, value, ttl);
        log.debug("Idempotency key marked as processed: {}", key);
    }

    @Override
    public Optional<String> getProcessedResponse(String key, String prefix) {
        String redisKey = normalizePrefix(prefix) + key;
        String value = redisTemplate.opsForValue().get(redisKey);

        if (value != null && value.startsWith(PROCESSED_PREFIX)) {
            String responseJson = value.substring(PROCESSED_PREFIX.length());
            log.debug("Found processed response for idempotency key: {}", key);
            return Optional.of(responseJson);
        }

        return Optional.empty();
    }

    @Override
    public boolean isReserved(String key, String prefix) {
        String redisKey = normalizePrefix(prefix) + key;
        String value = redisTemplate.opsForValue().get(redisKey);
        return IdempotencyStatus.RESERVED.name().equals(value);
    }

    @Override
    public void markAsFailed(String key, Duration ttl, String prefix) {
        String redisKey = normalizePrefix(prefix) + key;
        redisTemplate.opsForValue().set(redisKey, IdempotencyStatus.FAILED.name(), ttl);
        log.debug("Idempotency key marked as failed: {}", key);
    }

    @Override
    public void deleteKey(String key, String prefix) {
        String redisKey = normalizePrefix(prefix) + key;
        redisTemplate.delete(redisKey);
        log.debug("Idempotency key deleted: {}", key);
    }
    
    private String normalizePrefix(String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            return "";
        }
        return prefix.endsWith(":") ? prefix : prefix + ":";
    }
}
