package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.constant.RoleName;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.mapper.UserMapper;
import com.hieu.ecommerce.model.dto.request.LoginRequest;
import com.hieu.ecommerce.model.dto.response.LoginResponse;
import com.hieu.ecommerce.model.dto.response.RefreshTokenResponse;
import com.hieu.ecommerce.model.entity.RefreshToken;
import com.hieu.ecommerce.model.entity.Role;
import com.hieu.ecommerce.model.entity.User;
import com.hieu.ecommerce.repository.RefreshTokenRepository;
import com.hieu.ecommerce.repository.UserRepository;
import com.hieu.ecommerce.service.AuthService;

import com.hieu.ecommerce.service.TokenBlacklistService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private static final Logger logger = LoggerFactory.getLogger(AuthServiceImpl.class);
    public static final MacAlgorithm JWT_ALGORITHM = MacAlgorithm.HS256;

    @Value("${app.jwt.secret-key}")
    protected String jwtSigningKey;

    @Value("${app.jwt.access-token-expiration}")
    private Long accessTokenExpiration;

    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    private final AuthenticationManager authenticationManager;
    private final JwtEncoder jwtEncoder;
    private final JwtDecoder jwtDecoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final TokenBlacklistService tokenBlacklistService;

    @Override
    @Transactional
    public LoginResponse login(LoginRequest loginRequest) {
        
        logger.info("Attempting login for user: {}", loginRequest.getPassword());
        
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "User not found with email: " + loginRequest.getEmail()));

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(loginRequest.getEmail());

        saveRefreshToken(user, refreshToken);

        logger.info("Login successful for user: {}", loginRequest.getEmail());
        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .user(userMapper.toResponseDTO(user))
                .build();
    }

    @Override
    @Transactional
    public RefreshTokenResponse refreshToken(String refreshToken) {

        RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new AppException(ErrorCode.UNAUTHENTICATED, "Refresh token not found"));

        if (storedRefreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedRefreshToken);
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Refresh token has expired");
        }

        String email;
        try {
            Jwt jwt = jwtDecoder.decode(refreshToken);
            email = jwt.getSubject();
        } catch (Exception e) {
            refreshTokenRepository.delete(storedRefreshToken);
            throw new AppException(ErrorCode.UNAUTHENTICATED, "Invalid refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND, "User not found"));

        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(email);

        refreshTokenRepository.delete(storedRefreshToken);
        saveRefreshToken(user, newRefreshToken);

        logger.info("Token refreshed successfully for user: {}", email);

        return RefreshTokenResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRefreshToken)
                .build();
    }

    @Override
    @Transactional
    public void logout(String refreshToken, String accessToken) {
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.error("Invalid logout request: null or empty refresh token");
            throw new AppException(ErrorCode.INVALID_REQUEST, "Refresh token cannot be null or empty");
        }
        
        logger.debug("Attempting logout with refresh token");
        
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    refreshTokenRepository.delete(token);
                    logger.info("User logged out successfully: {}", token.getUser().getEmail());
                });

        if (accessToken != null && !accessToken.trim().isEmpty()) {
            tokenBlacklistService.blacklistToken(accessToken);
            logger.info("Access token blacklisted");
        }
    }

    @Transactional
    public void cleanupExpiredRefreshTokens() {
        Instant now = Instant.now();
        List<RefreshToken> expiredTokens = refreshTokenRepository.findByExpiryDateBefore(now);
        
        if (!expiredTokens.isEmpty()) {
            refreshTokenRepository.deleteAll(expiredTokens);
            logger.info("Cleaned up {} expired refresh tokens", expiredTokens.size());
        }
    }

    private void saveRefreshToken(User user, String token) {
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plus(refreshTokenExpiration, ChronoUnit.SECONDS));
        
        refreshTokenRepository.save(refreshToken);
        logger.debug("Refresh token saved for user: {}", user.getEmail());
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant expiresAt = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        Set<RoleName> roles = user.getRoles().stream()
                .map(Role::getRoleName)
                .collect(Collectors.toSet());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(user.getEmail())
                .id(UUID.randomUUID().toString())
                .claim("userId", user.getId())
                .claim("roles", roles)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String generateRefreshToken(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.error("Cannot generate refresh token: email is null or empty");
            throw new AppException(ErrorCode.INVALID_REQUEST, "Email cannot be null or empty");
        }
        
        Instant now = Instant.now();
        Instant expiresAt = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(expiresAt)
                .subject(email)
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
