package com.hieu.ecommerce.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hieu.ecommerce.model.dto.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class CustomAccessDeniedHandler implements AccessDeniedHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response,
                       AccessDeniedException accessDeniedException) throws IOException {
        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_FORBIDDEN);

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setMessage("Forbidden");
        apiResponse.setSuccess(false);
        apiResponse.setErrors(accessDeniedException.getMessage());
        apiResponse.setTimestamp(java.time.LocalDateTime.now().toString());
        apiResponse.setPath(request.getRequestURI());
        objectMapper.writeValue(response.getWriter(), apiResponse);
    }
}

