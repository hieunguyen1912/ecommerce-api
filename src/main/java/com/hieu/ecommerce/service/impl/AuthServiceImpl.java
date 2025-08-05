package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.exception.RefreshTokenException;
import com.hieu.ecommerce.model.dto.request.LoginRequest;
import com.hieu.ecommerce.model.dto.request.RefreshTokenRequest;
import com.hieu.ecommerce.model.dto.response.LoginResult;
import com.hieu.ecommerce.model.dto.response.UserInfo;
import com.hieu.ecommerce.model.entity.RefreshToken;
import com.hieu.ecommerce.model.entity.User;
import com.hieu.ecommerce.repository.RefreshTokenRepository;
import com.hieu.ecommerce.repository.UserRepository;
import com.hieu.ecommerce.service.AuthService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.JwsHeader;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class AuthServiceImpl implements AuthService {

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
        UsernamePasswordAuthenticationToken authenticationToken = new UsernamePasswordAuthenticationToken(
                loginRequest.getEmail(),
                loginRequest.getPassword()
        );
        Authentication authentication = authenticationManager.authenticate(authenticationToken);
        System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        SecurityContextHolder.getContext().setAuthentication(authentication);
        System.out.println("Authentication: " + SecurityContextHolder.getContext().getAuthentication().getPrincipal());


        User user = userRepository.findByEmail(loginRequest.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String accessToken = generateAccessToken(loginRequest.getEmail());
        String refreshToken = generateRefreshToken(loginRequest.getEmail());

        // Lưu refresh token vào database
        saveRefreshToken(user, refreshToken);

        LoginResult loginResult = new LoginResult();
        loginResult.setAccessToken(accessToken);
        loginResult.setRefreshToken(refreshToken);
        loginResult.setUserInfo(createUserInfo(user));

        return loginResult;
    }

    @Override
    @Transactional
    public LoginResult refreshToken(RefreshTokenRequest refreshTokenRequest) {
        String refreshToken = refreshTokenRequest.getRefreshToken();
        
        // Kiểm tra refresh token trong database
        RefreshToken storedRefreshToken = refreshTokenRepository.findByToken(refreshToken)
                .orElseThrow(() -> new RefreshTokenException("Refresh token not found"));

        // Kiểm tra token có hết hạn chưa
        if (storedRefreshToken.getExpiryDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(storedRefreshToken);
            throw new RefreshTokenException("Refresh token has expired");
        }

        // Decode JWT để lấy thông tin user
        String email;
        try {
            var jwt = jwtDecoder.decode(refreshToken);
            email = jwt.getSubject();
        } catch (Exception e) {
            refreshTokenRepository.delete(storedRefreshToken);
            throw new RefreshTokenException("Invalid refresh token");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RefreshTokenException("User not found"));

        // Tạo access token mới
        String newAccessToken = generateAccessToken(email);
        String newRefreshToken = generateRefreshToken(email);

        // Xóa refresh token cũ và lưu token mới
        refreshTokenRepository.delete(storedRefreshToken);
        saveRefreshToken(user, newRefreshToken);

        LoginResult loginResult = new LoginResult();
        loginResult.setAccessToken(newAccessToken);
        loginResult.setRefreshToken(newRefreshToken);
        loginResult.setUserInfo(createUserInfo(user));

        return loginResult;
    }

    @Override
    @Transactional
    public void logout(String refreshToken) {
        refreshTokenRepository.findByToken(refreshToken)
                .ifPresent(refreshTokenRepository::delete);
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
    }

    private UserInfo createUserInfo(User user) {
        UserInfo userInfo = new UserInfo();
        userInfo.setId(user.getId());
        userInfo.setEmail(user.getEmail());
        userInfo.setFullName(user.getFirstName() + " " + user.getLastName());
        return userInfo;
    }

    public String generateAccessToken(String email) {
        Instant now = Instant.now();
        Instant validity = now.plus(this.accessTokenExpiration, ChronoUnit.SECONDS);

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuedAt(now)
                .expiresAt(validity)
                .subject(email)
                .build();

        JwsHeader jwsHeader = JwsHeader.with(JWT_ALGORITHM).build();
        return this.jwtEncoder.encode(JwtEncoderParameters.from(jwsHeader, claims)).getTokenValue();
    }

    public String generateRefreshToken(String email) {
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
