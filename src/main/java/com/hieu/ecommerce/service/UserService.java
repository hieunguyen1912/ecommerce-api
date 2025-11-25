package com.hieu.ecommerce.service;

import com.hieu.ecommerce.model.dto.request.SignUpRequest;
import com.hieu.ecommerce.model.dto.request.UpdateUserRequest;
import com.hieu.ecommerce.model.dto.response.UserResponse;
import com.hieu.ecommerce.model.dto.response.UserSignUpResponse;
import com.hieu.ecommerce.model.entity.User;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface UserService {
    List<UserResponse> getAllUsers(Pageable pageable);
    UserResponse getUserById(Long id);
    User getUser(Long id);
    UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest);
    UserSignUpResponse createUser(SignUpRequest signUpRequest);
    void deleteUser(Long id);
    long countUsers(); // Method to count total users
}
