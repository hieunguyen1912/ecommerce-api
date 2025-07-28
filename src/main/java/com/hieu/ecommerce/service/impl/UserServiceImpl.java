package com.hieu.ecommerce.service.impl;

import com.hieu.ecommerce.exception.ResourceNotFoundException;
import com.hieu.ecommerce.mapper.UserMapper;
import com.hieu.ecommerce.model.dto.request.UserUpdateRequest;
import com.hieu.ecommerce.model.dto.response.UserResponseDTO;
import com.hieu.ecommerce.model.entity.User;
import com.hieu.ecommerce.repository.UserRepository;
import com.hieu.ecommerce.service.UserService;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {
    private final UserRepository userRepository;
    private final UserMapper userMapper;

    public UserServiceImpl(UserRepository userRepository, UserMapper userMapper) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
    }

    @Override
    public List<UserResponseDTO> getAllUsers(Pageable pageable) {
         return userRepository.findAll(pageable)
                .stream()
                .map(userMapper::toResponseDTO)
                .toList();
    }

    @Override
    public UserResponseDTO getUserById(Long id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
        return userMapper.toResponseDTO(user);
    }

    @Override
    public UserResponseDTO updateUser(Long id, UserUpdateRequest userUpdateRequest) {
        User user = userRepository.findById(id)
                .orElseThrow(() ->
            new ResourceNotFoundException("User not found with id: " + id));

        if (userRepository.findByEmail(userUpdateRequest.getEmail()).isPresent() &&
                !user.getEmail().equals(userUpdateRequest.getEmail())) {
            throw new IllegalArgumentException("Email already exists");
        }

        userMapper.updateUser(user, userUpdateRequest);

        return userMapper.toResponseDTO(userRepository.save(user));
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
