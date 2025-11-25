package com.hieu.ecommerce.model.dto.response;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hieu.ecommerce.constant.Gender;
import com.hieu.ecommerce.constant.UserStatus;
import lombok.Data;

import java.time.Instant;
import java.util.Date;

@Data
public class UserResponse {

    private Long id;
    private String fullName;
    private String email;
    private String phoneNumber;

    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfBirth;

    private UserStatus status;
    private Gender gender;
    private String avatarUrl;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant createdAt;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", timezone = "UTC")
    private Instant updatedAt;
}