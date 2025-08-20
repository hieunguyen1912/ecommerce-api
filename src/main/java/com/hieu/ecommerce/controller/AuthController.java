package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.common.annotation.ResponseMessage;
import com.hieu.ecommerce.model.dto.request.LoginRequest;
import com.hieu.ecommerce.model.dto.request.RefreshTokenRequest;
import com.hieu.ecommerce.model.dto.response.LoginResult;
import com.hieu.ecommerce.model.dto.response.ResponseLogin;
import com.hieu.ecommerce.service.AuthService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.CookieValue;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @ResponseMessage("Login successful")
    public ResponseEntity<ResponseLogin> login(
            @Valid @RequestBody LoginRequest loginRequest
    ) {
        LoginResult loginResult = authService.login(loginRequest);

        ResponseLogin responseLogin = new ResponseLogin();
        responseLogin.setAccessToken(loginResult.getAccessToken());
        responseLogin.setUserInfo(loginResult.getUserInfo());

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResult.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .sameSite("Strict")
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(responseLogin);
    }

    @PostMapping("/refresh")
    @ResponseMessage("Token refreshed successfully")
    public ResponseEntity<ResponseLogin> refreshToken(@CookieValue(name = "refreshToken", defaultValue = "") String refreshToken) {
        RefreshTokenRequest refreshTokenRequest = new RefreshTokenRequest(refreshToken);
        LoginResult loginResult = authService.refreshToken(refreshTokenRequest);

        ResponseLogin responseLogin = new ResponseLogin();
        responseLogin.setAccessToken(loginResult.getAccessToken());
        responseLogin.setUserInfo(loginResult.getUserInfo());

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", loginResult.getRefreshToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(60 * 60 * 24 * 7)
                .sameSite("Strict")
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .header("Set-Cookie", refreshTokenCookie.toString())
                .body(responseLogin);
    }

    @PostMapping("/logout")
    @ResponseMessage("Logout successful")
    public ResponseEntity<Void> logout(HttpServletRequest request) {
        Cookie[] cookies = request.getCookies();
        if (cookies != null) {
            for (Cookie cookie : cookies) {
                if ("refreshToken".equals(cookie.getName())) {
                    authService.logout(cookie.getValue());
                    break;
                }
            }
        }

        ResponseCookie refreshTokenCookie = ResponseCookie.from("refreshToken", "")
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .header("Set-Cookie", refreshTokenCookie.toString())
                .build();
    }
}
