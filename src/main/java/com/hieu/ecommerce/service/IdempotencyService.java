package com.hieu.ecommerce.service;

import java.time.Duration;
import java.util.Optional;

public interface IdempotencyService {

    boolean reserveKey(String key, Duration ttl, String prefix);
    
    void markAsProcessed(String key, String responseJson, Duration ttl, String prefix);

    Optional<String> getProcessedResponse(String key, String prefix);
    
    boolean isReserved(String key, String prefix);

    void markAsFailed(String key, Duration ttl, String prefix);
    
    void deleteKey(String key, String prefix);
}
