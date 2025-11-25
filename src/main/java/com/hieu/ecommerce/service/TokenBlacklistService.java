package com.hieu.ecommerce.service;

public interface TokenBlacklistService {
    void blacklistToken(String token);
    boolean isJtiBlacklisted(String jti);
}
