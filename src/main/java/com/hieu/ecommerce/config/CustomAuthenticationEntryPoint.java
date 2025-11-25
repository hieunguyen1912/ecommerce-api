package com.hieu.ecommerce.config;

import java.io.IOException;
import java.time.LocalDateTime;

import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hieu.ecommerce.model.dto.response.ApiResponse;
import com.hieu.ecommerce.constant.ErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
@RequiredArgsConstructor
public class CustomAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private final ObjectMapper objectMapper;
    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response,
            AuthenticationException authException) throws IOException, ServletException {

        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        ApiResponse<Object> apiResponse = ApiResponse.error(
            buildClientMessage(authException)
        );

        objectMapper.writeValue(response.getWriter(), apiResponse);
    }

    private String buildClientMessage(AuthenticationException ex) {
        if (ex.getClass().getSimpleName().contains("Jwt")) {
            return "Invalid or expired JWT token";
        }
        if (ex instanceof BadCredentialsException) {
            return "Invalid username or password";
        }
        return "Unauthorized";
    }
    
}
