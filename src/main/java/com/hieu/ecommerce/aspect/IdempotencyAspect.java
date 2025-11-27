package com.hieu.ecommerce.aspect;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hieu.ecommerce.annotation.Idempotent;
import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.service.IdempotencyService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;


@Aspect
@Component
@RequiredArgsConstructor
@Slf4j
@Order(1)
public class IdempotencyAspect {
    
    private final IdempotencyService idempotencyService;
    private final ObjectMapper objectMapper;
    
    @Around("@annotation(idempotent)")
    public Object handleIdempotency(ProceedingJoinPoint joinPoint, Idempotent idempotent) throws Throwable {

        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Class<?> returnType = signature.getReturnType();
        Object[] args = joinPoint.getArgs();
        java.lang.reflect.Method method = signature.getMethod();

        String idempotencyKey = extractKeyFromParameters(method, args);
        
        
        if (idempotencyKey == null || idempotencyKey.isEmpty()) {
            throw new AppException(ErrorCode.INVALID_REQUEST, 
                "Idempotency key is required");
        }
        
        String prefix = idempotent.prefix() + ":";
        Duration ttl = Duration.ofHours(idempotent.ttl());
        Duration failedTtl = Duration.ofMinutes(idempotent.failedTtl());
        
        Optional<String> cachedResponse = idempotencyService.getProcessedResponse(idempotencyKey, prefix);
        if (cachedResponse.isPresent()) {
            log.info("Returning cached response for idempotency key: {}", idempotencyKey);
            return objectMapper.readValue(cachedResponse.get(), returnType);
        }
        
        if (idempotencyService.isReserved(idempotencyKey, prefix)) {
            log.warn("Idempotency key is being processed: {}", idempotencyKey);
            throw new AppException(ErrorCode.CONFLICT, 
                "Request is being processed. Please retry after a moment.");
        }

        if (!idempotencyService.reserveKey(idempotencyKey, ttl, prefix)) {
            log.debug("Idempotency key exists but not processed, retrying: {}", idempotencyKey);
            idempotencyService.deleteKey(idempotencyKey, prefix);
            if (!idempotencyService.reserveKey(idempotencyKey, ttl, prefix)) {
                throw new AppException(ErrorCode.CONFLICT, 
                    "Request is being processed. Please retry after a moment.");
            }
        }
        
        try {
            Object result = joinPoint.proceed();
            
            if (result != null) {
                try {
                    String responseJson = objectMapper.writeValueAsString(result);
                    idempotencyService.markAsProcessed(idempotencyKey, responseJson, ttl, prefix);
                    log.debug("Idempotency key marked as processed: {}", idempotencyKey);
                } catch (Exception e) {
                    log.warn("Failed to cache response to Redis for key: {}", idempotencyKey, e);
                }
            }
            
            return result;
            
        } catch (Exception e) {
            idempotencyService.markAsFailed(idempotencyKey, failedTtl, prefix);
            log.error("Request failed for idempotency key: {}", idempotencyKey, e);
            throw e;
        }
    }
    
  
    private String extractKeyFromParameters(java.lang.reflect.Method method, Object[] args) {
        try {
            java.lang.reflect.Parameter[] parameters = method.getParameters();
            
            for (int i = 0; i < parameters.length && i < args.length; i++) {
                String paramName = parameters[i].getName();
                if ("idempotencyKey".equalsIgnoreCase(paramName) && args[i] instanceof String) {
                    String key = (String) args[i];
                    if (key != null && !key.trim().isEmpty()) {
                        log.debug("Extracted idempotency key from parameter '{}': {}", paramName, key);
                        return key.trim();
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Failed to extract idempotency key from parameters", e);
        }
        return null;
    }

}

