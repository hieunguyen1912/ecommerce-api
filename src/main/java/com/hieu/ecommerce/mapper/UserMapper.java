package com.hieu.ecommerce.mapper;

import com.hieu.ecommerce.model.dto.request.SignUpRequest;
import com.hieu.ecommerce.model.dto.request.UserUpdateRequest;
import com.hieu.ecommerce.model.dto.response.UserResponseDTO;
import com.hieu.ecommerce.model.dto.response.UserSignUpResponse;
import com.hieu.ecommerce.model.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingTarget;

@Mapper(componentModel = "spring")
public interface UserMapper {
    @Mapping(target = "id", ignore = true)
    User toEntity(SignUpRequest signUpRequest);

    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserSignUpResponse toSignUpResponse(User user);

    @Mapping(target = "fullName", expression = "java(user.getFirstName() + \" \" + user.getLastName())")
    UserResponseDTO toResponseDTO(User user);

    void updateUser(@MappingTarget User user, UserUpdateRequest userUpdateRequest);
}
