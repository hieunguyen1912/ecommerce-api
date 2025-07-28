package com.hieu.ecommerce.model.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;

import java.time.LocalDateTime;

@Setter
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private boolean success;
    private String message;
    private T data;
    private Object errors;
    private String timestamp;
    private String path;

    public ApiResponse() {
        this.timestamp = LocalDateTime.now().toString();
    }

    public static <T> ApiResponse<T> success(T data) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = "Success";
        return response;
    }

    public static <T> ApiResponse<T> success(T data, String message) {
        ApiResponse<T> response = new ApiResponse<>();
        response.success = true;
        response.data = data;
        response.message = message;
        return response;
    }

    public static ApiResponse<?> error(String message) {
        ApiResponse<?> response = new ApiResponse<>();
        response.success = false;
        response.message = message;
        return response;
    }

    public static ApiResponse<?> error(String message, Object errors) {
        ApiResponse<?> response = error(message);
        response.errors = errors;
        return response;
    }
}

