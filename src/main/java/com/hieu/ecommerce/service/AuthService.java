package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.LoginRequest;
import com.hieu.ecommerce.model.dto.request.RefreshTokenRequest;
import com.hieu.ecommerce.model.dto.response.LoginResult;

public interface AuthService {
    LoginResult login(LoginRequest loginRequest);
    LoginResult refreshToken(RefreshTokenRequest refreshTokenRequest);
    void logout(String refreshToken);
}
