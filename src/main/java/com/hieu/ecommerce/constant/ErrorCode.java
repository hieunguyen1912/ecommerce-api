package com.hieu.ecommerce.constant;

import lombok.Getter;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

@Getter
public enum ErrorCode {
    UNCATEGORIZED_EXCEPTION(9999, "Uncategorized error", HttpStatus.INTERNAL_SERVER_ERROR),
    UNAUTHENTICATED(1001, "Unauthenticated", HttpStatus.UNAUTHORIZED),
    
    // Validation errors (400)
    INVALID_REQUEST(2001, "Invalid request", HttpStatus.BAD_REQUEST),
    VALIDATION_ERROR(2002, "Validation error", HttpStatus.BAD_REQUEST),
    
    // Resource not found (404)
    RESOURCE_NOT_FOUND(3001, "Resource not found", HttpStatus.NOT_FOUND),
    
    // Business logic errors (400)
    DUPLICATE_RESOURCE(4001, "Duplicate resource", HttpStatus.BAD_REQUEST),
    INVALID_OPERATION(4002, "Invalid operation", HttpStatus.BAD_REQUEST),
    CONFLICT(4003, "Request is being processed", HttpStatus.CONFLICT);


    private final int code;
    private final String message;
    private final HttpStatusCode statusCode;

    ErrorCode(int code, String message, HttpStatusCode statusCode) {
        this.code = code;
        this.message = message;
        this.statusCode = statusCode;
    }
}
