package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.model.dto.request.SignUpRequest;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {


    @PostMapping("/register")
    public ResponseEntity<Void> register(@Valid @RequestBody SignUpRequest signUpRequest) {

        return ResponseEntity.ok(null);
    }

}
