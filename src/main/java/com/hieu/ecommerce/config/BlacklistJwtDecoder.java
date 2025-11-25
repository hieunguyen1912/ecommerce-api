package com.hieu.ecommerce.config;

import com.hieu.ecommerce.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtException;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class BlacklistJwtDecoder implements JwtDecoder {
    private final JwtDecoder jwtDecoder;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    public Jwt decode(String token) throws JwtException {
        Jwt jwt = jwtDecoder.decode(token);

        String jti = jwt.getId();
        if (tokenBlacklistService.isJtiBlacklisted(jti)) {
            log.warn("Token with JTI {} is blacklisted", jti);
            throw new JwtException("Token has been revoked");
        }
        return jwt;
    }
}
