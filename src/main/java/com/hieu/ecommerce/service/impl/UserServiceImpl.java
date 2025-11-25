package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.constant.ErrorCode;
import com.hieu.ecommerce.exception.AppException;
import com.hieu.ecommerce.mapper.UserMapper;
import com.hieu.ecommerce.model.dto.request.SignUpRequest;
import com.hieu.ecommerce.model.dto.request.UpdateUserRequest;
import com.hieu.ecommerce.model.dto.response.UserResponse;
import com.hieu.ecommerce.model.dto.response.UserSignUpResponse;
import com.hieu.ecommerce.model.entity.Role;
import com.hieu.ecommerce.model.entity.User;
import com.hieu.ecommerce.repository.RoleRepository;
import com.hieu.ecommerce.repository.UserRepository;
import com.hieu.ecommerce.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final RoleRepository roleRepository;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper, PasswordEncoder passwordEncoder, RoleRepository roleRepository) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.roleRepository = roleRepository;
    }

    @Override
    public List<UserResponse> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable)
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Override
    public UserResponse getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "User not found with id: " + id));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "User not found with id: " + id));
    }

    @Override
    public UserResponse updateUser(Long id, UpdateUserRequest updateUserRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new AppException(ErrorCode.RESOURCE_NOT_FOUND,
                        "User not found with id: " + id));

        if (userRepository.findByEmail(updateUserRequest.getEmail()).isPresent() &&
                !user.getEmail().equals(updateUserRequest.getEmail())) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Email already exists");
        }

        if (userRepository.findByPhone(updateUserRequest.getPhone()).isPresent() &&
                !user.getPhone().equals(updateUserRequest.getPhone())) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Phone number already exists");
        }

        userMapper.updateUser(user, updateUserRequest);

        return userMapper.toResponseDTO(userRepository.save(user));
    }

    @Override
    public UserSignUpResponse createUser(SignUpRequest signUpRequest) {
        if (userRepository.findByEmail(signUpRequest.getEmail()).isPresent()) {
            throw new AppException(ErrorCode.DUPLICATE_RESOURCE, "Email already exists");
        }

        User user = userMapper.toEntity(signUpRequest);
        user.setPassword(passwordEncoder.encode(signUpRequest.getPassword()));
        return userMapper.toSignUpResponse(userRepository.save(user));
    }

    @Override
    public void deleteUser(Long id) {
        userRepository.deleteById(id);
    }

    @Override
    public long countUsers() {
        return userRepository.count();
    }
}
