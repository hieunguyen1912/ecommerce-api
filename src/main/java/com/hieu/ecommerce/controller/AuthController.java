package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.LoginRequest;
import com.hieu.ecommerce.model.dto.response.LoginResponse;
import com.hieu.ecommerce.model.dto.response.RefreshTokenResponse;
import com.hieu.ecommerce.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.Cookie;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Authentication", description = "API endpoints for user authentication")
public class AuthController {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final String BEARER_PREFIX = "Bearer ";

    private final AuthService authService;

    @Value("${app.jwt.refresh-token-expiration}")
    private Long refreshTokenExpiration;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(summary = "User login", description = "Authenticate user and return JWT tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Login successful"),
            @ApiResponse(responseCode = "401", description = "Invalid credentials")
    })
    @ResponseMessage("Login successful")
    public ResponseEntity<LoginResponse> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResponse loginResponse = authService.login(loginRequest);

        ResponseCookie refreshTokenCookie = refreshTokenCookie(loginResponse.getRefreshToken(), refreshTokenExpiration);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(loginResponse);
    }

    @PostMapping("/refresh")
    @Operation(summary = "Refresh token", description = "Refresh access token using refresh token from cookie")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Token refreshed successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid or expired refresh token")
    })
    @ResponseMessage("Token refreshed successfully")
    public ResponseEntity<RefreshTokenResponse> refreshToken(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, defaultValue = "") String refreshToken
    ) {
        RefreshTokenResponse response = authService.refreshToken(refreshToken);

        ResponseCookie refreshTokenCookie = refreshTokenCookie(response.getRefreshToken(), refreshTokenExpiration);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, refreshTokenCookie.toString())
                .body(response);
    }

    @PostMapping("/logout")
    @Operation(summary = "User logout", description = "Logout user and invalidate tokens")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Logout successful")
    })
    @ResponseMessage("Logout successful")
    public ResponseEntity<Void> logout(
            @CookieValue(name = REFRESH_TOKEN_COOKIE_NAME, defaultValue = "") String refreshToken,
            @RequestHeader(value = "Authorization", required = false) String authorizationHeader
    ) {
        if (authorizationHeader != null && authorizationHeader.startsWith(BEARER_PREFIX)) {
            String accessToken = authorizationHeader.substring(BEARER_PREFIX.length());
            authService.logout(refreshToken, accessToken);
        } else {
            authService.logout(refreshToken, null);
        }

        ResponseCookie deleteCookie = refreshTokenCookie(refreshToken, (long) 0);

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, deleteCookie.toString())
                .build();
    }

    private ResponseCookie refreshTokenCookie(String token, Long maxAge) {
        ResponseCookie.ResponseCookieBuilder builder = ResponseCookie
                .from(REFRESH_TOKEN_COOKIE_NAME, token)
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(refreshTokenExpiration)
                .sameSite(Cookie.SameSite.STRICT.toString());

        return builder.build();
    }
}
