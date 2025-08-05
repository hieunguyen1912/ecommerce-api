package com.hieu.ecommerce.controller;

import com.hieu.ecommerce.model.dto.request.SignUpRequest;
import com.hieu.ecommerce.model.dto.request.UserUpdateRequest;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.UserResponseDTO;
import com.hieu.ecommerce.model.dto.response.UserSignUpResponse;
import com.hieu.ecommerce.service.UserService;
import com.hieu.ecommerce.common.anotation.ResponseMessage;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseMessage("User registered successfully")
    public ResponseEntity<UserSignUpResponse> register(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(signUpRequest));
    }

    @GetMapping()
    @ResponseMessage("Get all users successfully")
    public ResponseEntity<PageResponse<UserResponseDTO>> getAllUsers(Pageable pageable) {
        List<UserResponseDTO> users = userService.getAllUsers(pageable);
        return ResponseEntity
                .ok(new PageResponse<>(
                        users,
                        pageable.getPageNumber() + 1,
                        pageable.getPageSize(),
                        userService.countUsers()));
    }

    @GetMapping("/{id}")
    @ResponseMessage("Get user by ID successfully")
    public ResponseEntity<UserResponseDTO> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @ResponseMessage("Update user successfully")
    public ResponseEntity<UserResponseDTO> updateUser(
            @PathVariable Long id,
            @Valid @RequestBody UserUpdateRequest userUpdateRequest) {
        return ResponseEntity.ok( userService.updateUser(id, userUpdateRequest));
    }

    @DeleteMapping("/{id}")
    @ResponseMessage("Delete user successfully")
    public ResponseEntity<Void> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok().build();
    }

    @GetMapping("/test-token")
    @ResponseMessage("Token is valid")
    public ResponseEntity<String> testToken() {
        return ResponseEntity.ok("Token is still valid!");
    }
}
