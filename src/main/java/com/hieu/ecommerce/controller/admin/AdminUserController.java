package com.hieu.ecommerce.controller.admin;

import com.hieu.ecommerce.model.dto.request.SignUpRequest;
import com.hieu.ecommerce.model.dto.request.UserUpdateRequest;
import com.hieu.ecommerce.model.dto.response.PageResponse;
import com.hieu.ecommerce.model.dto.response.UserResponse;
import com.hieu.ecommerce.model.dto.response.UserSignUpResponse;
import com.hieu.ecommerce.service.UserService;
import com.hieu.ecommerce.common.annotation.ResponseMessage;
import jakarta.validation.Valid;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("api/v1/users")
public class AdminUserController {

    private final UserService userService;

    public AdminUserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    @ResponseMessage("User registered successfully")
    public ResponseEntity<UserSignUpResponse> register(@Valid @RequestBody SignUpRequest signUpRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(userService.createUser(signUpRequest));
    }

    @GetMapping()
    @ResponseMessage("Get all users successfully")
    @PreAuthorize("hasRole('USER')")
    public ResponseEntity<PageResponse<UserResponse>> getAllUsers(
            @PageableDefault(page = 0, size = 10, sort = "firstName", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        System.out.println(pageable.getSort());
        List<UserResponse> users = userService.getAllUsers(pageable);
        return ResponseEntity
                .ok(new PageResponse<>(
                        users,
                        pageable.getPageNumber() + 1,
                        pageable.getPageSize(),
                        userService.countUsers()));
    }

    @GetMapping("/{id}")
    @ResponseMessage("Get user by ID successfully")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Long id) {
        return ResponseEntity.ok(userService.getUserById(id));
    }

    @PutMapping("/{id}")
    @ResponseMessage("Update user successfully")
    public ResponseEntity<UserResponse> updateUser(
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
}
