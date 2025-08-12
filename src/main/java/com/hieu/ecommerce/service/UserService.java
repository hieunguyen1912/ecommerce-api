package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.SignUpRequest;
import com.hieu.ecommerce.model.dto.request.UserUpdateRequest;
import com.hieu.ecommerce.model.dto.response.UserResponse;
import com.hieu.ecommerce.model.dto.response.UserSignUpResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    UserResponse updateUser(Long id, UserUpdateRequest userUpdateRequest);
    UserSignUpResponse createUser(SignUpRequest signUpRequest); // Method to create a new user
    void deleteUser(Long id);
    long countUsers(); // Method to count total users
}
