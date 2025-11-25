package com.hieu.ecommerce.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hieu.ecommerce.annotation.GenderSubset;
import com.hieu.ecommerce.annotation.PhoneNumber;
import com.hieu.ecommerce.constant.Gender;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import static com.hieu.ecommerce.constant.Gender.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class UpdateUserRequest {
    @NotBlank(message = "firstName is required")
    @Size(max = 50, message = "First name must not exceed 50 characters")
    private String firstName;

    @NotBlank(message = "lastName is required")
    @Size(max = 50, message = "Last name must not exceed 50 characters")
    private String lastName;

    @NotBlank(message = "Email is required")
    @Email(message = "Email should be valid")
    private String email;

    @NotBlank(message = "Phone number is required")
    @PhoneNumber
    private String phone;

    @NotNull(message = "dateOfBirth is required")
    @Past(message = "Date of birth must be in the past")
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @JsonFormat(pattern = "yyyy-MM-dd")
    private Date dateOfBirth;

    @GenderSubset(anyOf = { MALE, FEMALE, OTHER})
    private Gender gender;

    private String avatarUrl;
}

