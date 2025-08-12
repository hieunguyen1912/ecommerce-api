package com.hieu.ecommerce.exception;

import com.hieu.ecommerce.model.dto.response.ApiResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(value = {
            ResourceNotFoundException.class,
            EmailExistsException.class,
            IllegalArgumentException.class
    })
    public ResponseEntity<ApiResponse<Object>> handleResourceNotFoundException(RuntimeException ex) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ex.getMessage());
        return ResponseEntity.status(404)
                .body(apiResponse);
    }

    @ExceptionHandler(RefreshTokenException.class)
    public ResponseEntity<ApiResponse<Object>> handleRefreshTokenException(RefreshTokenException ex) {
        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ex.getMessage());
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                .body(apiResponse);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Object>> handleJsonParseError(HttpMessageNotReadableException ex) {
        Map<String, String> error = new HashMap<>();
        error.put("error", "Invalid input format: " + ex.getMostSpecificCause().getMessage());

        ApiResponse<Object> apiResponse = new ApiResponse<>();
        apiResponse.setMessage(ex.getMessage());
        apiResponse.setErrors(error);

        return ResponseEntity.badRequest().body(apiResponse);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Object>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errorMap = new HashMap<>();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errorMap.put(error.getField(), error.getDefaultMessage());
        });

        ApiResponse<Object> res = new ApiResponse<>();
        res.setMessage("Validation failed");
        res.setErrors(errorMap);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(res);
    }
}
