package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.exception.RefreshTokenException;
import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.model.dto.request.LoginRequest;
import com.hieu.ecommerce.model.dto.request.RefreshTokenRequest;
import com.hieu.ecommerce.model.dto.response.LoginResult;
import com.hieu.ecommerce.model.dto.response.UserInfo;
import com.hieu.ecommerce.model.entity.Permission;
import com.hieu.ecommerce.model.entity.RefreshToken;
import com.hieu.ecommerce.model.entity.Role;
import com.hieu.ecommerce.model.entity.User;
import com.hieu.ecommerce.repository.RefreshTokenRepository;
import com.hieu.ecommerce.repository.UserRepository;
import com.hieu.ecommerce.service.AuthService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
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

    public AuthServiceImpl(AuthenticationManager authenticationManager, 
                         JwtEncoder jwtEncoder, 
                         JwtDecoder jwtDecoder,
                         RefreshTokenRepository refreshTokenRepository,
                         UserRepository userRepository) {
        this.authenticationManager = authenticationManager;
        this.jwtEncoder = jwtEncoder;
        this.jwtDecoder = jwtDecoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public LoginResult login(LoginRequest loginRequest) {
        
        logger.info("Attempting login for user: {}", loginRequest.getEmail());
        
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        SecurityContextHolder.getContext().setAuthentication(authentication);
        
        logger.debug("Authentication successful for user: {}", loginRequest.getEmail());
        logger.info("Authentication details: {}", authentication);

        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + loginRequest.getEmail()));

        String accessToken = generateAccessToken(user);
        String refreshToken = generateRefreshToken(loginRequest.getEmail());

        // Lưu refresh token vào database
        saveRefreshToken(user, refreshToken);

        LoginResult loginResult = new LoginResult();
        loginResult.setAccessToken(accessToken);
        loginResult.setRefreshToken(refreshToken);
        loginResult.setUserInfo(createUserInfo(user));

        logger.info("Login successful for user: {}", loginRequest.getEmail());
        return loginResult;
    }

    @Override
    @Transactional
    public LoginResult refreshToken(RefreshTokenRequest refreshTokenRequest) {
        
        String refreshToken = refreshTokenRequest.getRefreshToken();
        logger.debug("Attempting to refresh token");
        
        // Kiểm tra refresh token trong database
        RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));

        // Kiểm tra token có hết hạn chưa
        if (storedRefreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedRefreshToken);
            logger.warn("Refresh token expired for user: {}", storedRefreshToken.getUser().getEmail());
            throw new RefreshTokenException("Refresh token has expired");
        }

        // Decode JWT để lấy thông tin user
        String email;
        try {
            var jwt = jwtDecoder.decode(refreshToken);
            email = jwt.getSubject();
        } catch (Exception e) {
            refreshTokenRepository.delete(storedRefreshToken);
            logger.error("Invalid refresh token provided", e);
            throw new RefreshTokenException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RefreshTokenException("User not found"));

        // Tạo access token mới
        String newAccessToken = generateAccessToken(user);
        String newRefreshToken = generateRefreshToken(email);

        // Xóa refresh token cũ và lưu token mới
        refreshTokenRepository.delete(storedRefreshToken);
        saveRefreshToken(user, newRefreshToken);

        LoginResult loginResult = new LoginResult();
        loginResult.setAccessToken(newAccessToken);
        loginResult.setRefreshToken(newRefreshToken);
        loginResult.setUserInfo(createUserInfo(user));

        logger.info("Token refreshed successfully for user: {}", email);
        return loginResult;
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        // Validate input
        if (refreshToken == null || refreshToken.trim().isEmpty()) {
            logger.error("Invalid logout request: null or empty refresh token");
            throw new IllegalArgumentException("Refresh token cannot be null or empty");
        }
        
        logger.debug("Attempting logout with refresh token");
        
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(token -> {
                    refreshTokenRepository.delete(token);
                    logger.info("User logged out successfully: {}", token.getUser().getEmail());
                });
    }

    /**
     * Clean up expired refresh tokens from database
     * This method should be called periodically (e.g., via scheduled task)
     */
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
        // Xóa refresh token cũ nếu có
        refreshTokenRepository.findByUser(user).ifPresent(refreshTokenRepository::delete);

        // Tạo refresh token mới
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setToken(token);
        refreshToken.setExpiryDate(Instant.now().plus(refreshTokenExpiration, ChronoUnit.SECONDS));
        
        refreshTokenRepository.save(refreshToken);
        logger.debug("Refresh token saved for user: {}", user.getEmail());
    }

    private UserInfo createUserInfo(User user) {
        if (user == null) {
            logger.error("Cannot create user info: user is null");
            throw new IllegalArgumentException("User cannot be null");
        }
        
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        
        // Handle null names gracefully
        String firstName = user.getFirstName() != null ? user.getFirstName() : "";
        String lastName = user.getLastName() != null ? user.getLastName() : "";
        userInfo.setFullName((firstName + " " + lastName).trim());
        
        return userInfo;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        // Optimize roles and permissions extraction
        // Lấy roles và permissions của user
        Set<String> roles = user.getRoles().stream()
                .map(Role::getName)
                .collect(Collectors.toSet());

        Set<String> permissions = user.getRoles().stream()
                .flatMap(role -> role.getPermissions().stream())
                .map(Permission::getName)
                .collect(Collectors.toSet());

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(user.getEmail())
                .claim("roles", roles)
                .claim("permissions", permissions)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String generateRefreshToken(String email) {
        if (email == null || email.trim().isEmpty()) {
            logger.error("Cannot generate refresh token: email is null or empty");
            throw new IllegalArgumentException("Email cannot be null or empty");
        }
        
        Instant now = Instant.now();
        Instant validity = now.plus(this.refreshTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .id(UUID.randomUUID().toString())
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }
}
