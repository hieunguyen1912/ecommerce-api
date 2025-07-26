package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.model.dto.request.SignUpRequest;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    @PostMapping("/register")
    public ResponseEntity<Map<String, Object>> register(@Valid @RequestBody SignUpRequest signUpRequest, HttpServletRequest request) {
        // Debug logs
        System.out.println("Content-Type: " + request.getContentType());
        System.out.println("Request body received");
        System.out.println("SignUpRequest = " + signUpRequest);
        System.out.println("firstName: " + signUpRequest.getFirstName());
        System.out.println("email: " + signUpRequest.getEmail());

        Map<String, Object> response = new HashMap<>();
        response.put("success", true);
        response.put("message", "User registered successfully");
        response.put("data", signUpRequest);

        return ResponseEntity.ok(response);
    }

}
