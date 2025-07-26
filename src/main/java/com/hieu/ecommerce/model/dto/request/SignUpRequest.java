package com.hieu.ecommerce.model.dto.request;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.hieu.ecommerce.utils.Gender;
import com.hieu.ecommerce.validator.GenderSubset;
import com.hieu.ecommerce.validator.PhoneNumber;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import java.util.Date;

import static com.hieu.ecommerce.utils.Gender.*;

@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignUpRequest {
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

    @GenderSubset(anyOf = {MALE, FEMALE, OTHER})
    private Gender gender;

    private String avatarUrl;

    @NotBlank(message = "password is required")
    @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank(message = "Confirm password is required")
    private String confirmPassword;

    @AssertTrue(message = "Password and confirm password must match")
    public boolean isPasswordMatching() {
        return password != null && password.equals(confirmPassword);
    }
}
