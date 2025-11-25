package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.LoginRequest;
import com.hieu.ecommerce.model.dto.response.LoginResponse;
import com.hieu.ecommerce.model.dto.response.RefreshTokenResponse;

public interface AuthService {
    LoginResponse login(LoginRequest loginRequest);
    RefreshTokenResponse refreshToken(String token);
    void logout(String refreshToken, String accessToken);
}
